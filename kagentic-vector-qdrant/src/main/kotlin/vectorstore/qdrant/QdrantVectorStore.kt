package vectorstore.qdrant

import core.Logger
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import llm.retryWithBackoff
import vectorstore.VectorDocument
import vectorstore.VectorSearchResult
import vectorstore.VectorStore

/**
 * Production-ready Qdrant vector store implementation using the Qdrant REST API v1.
 *
 * Supports upsert, batch upsert, semantic search with payload filtering, single delete,
 * and full collection clear — all with automatic retry on transient failures.
 *
 * ## Setup
 *
 * 1. Run Qdrant locally or on Qdrant Cloud: https://cloud.qdrant.io
 * 2. Create a collection with a vector size matching your embedding model output
 *    (e.g., 1536 for OpenAI text-embedding-3-small)
 * 3. Point [host] at your Qdrant instance (e.g., "http://localhost:6333")
 *
 * ## Usage Example
 *
 * ```kotlin
 * val store = QdrantVectorStore(
 *     host           = "http://localhost:6333",
 *     collectionName = "my-docs"
 * )
 *
 * runBlocking {
 *     store.upsert(VectorDocument(
 *         id       = "doc-1",
 *         vector   = embedder.embed("Hello from Qdrant"),
 *         text     = "Hello from Qdrant",
 *         metadata = mapOf("source" to "docs")
 *     ))
 *
 *     val results = store.search(
 *         query  = embedder.embed("Hello"),
 *         topK   = 5,
 *         filter = mapOf("source" to "docs")
 *     )
 *     results.forEach { println("${it.id} score=${it.score}: ${it.text}") }
 * }
 * ```
 *
 * @property host Qdrant base URL (e.g., "http://localhost:6333" or "https://xyz.aws.cloud.qdrant.io:6333").
 * @property collectionName Name of the Qdrant collection to use.
 * @property apiKey Optional Qdrant API key for authenticated clusters (Qdrant Cloud).
 * @property logger Optional logger for debugging.
 * @property logEnabled Enables detailed request/response logging if true.
 */
class QdrantVectorStore(
    private val host: String,
    private val collectionName: String,
    private val apiKey: String? = null,
    private val logger: Logger? = null,
    private val logEnabled: Boolean = false
) : VectorStore {

    private val client = HttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    private val baseUrl get() = "${host.trimEnd('/')}/collections/$collectionName"

    override suspend fun upsert(document: VectorDocument) {
        upsertAll(listOf(document))
    }

    override suspend fun upsertAll(documents: List<VectorDocument>) {
        if (documents.isEmpty()) return

        val points = buildJsonArray {
            documents.forEach { doc ->
                add(buildJsonObject {
                    put("id", doc.id)
                    put("vector", buildJsonArray { doc.vector.forEach { add(it) } })
                    put("payload", buildJsonObject {
                        doc.metadata.forEach { (k, v) -> put(k, v) }
                        doc.text?.let { put(TEXT_PAYLOAD_KEY, it) }
                    })
                })
            }
        }

        val body = buildJsonObject { put("points", points) }

        if (logEnabled) logger?.log("QdrantVectorStore: upserting ${documents.size} points into '$collectionName'")

        retryWithBackoff(maxRetries = 3) {
            val response = client.put("$baseUrl/points") {
                applyAuth()
                contentType(ContentType.Application.Json)
                setBody(body.toString())
            }
            checkResponse(response, "upsert")
        }
    }

    override suspend fun search(
        query: List<Float>,
        topK: Int,
        filter: Map<String, String>
    ): List<VectorSearchResult> {
        require(topK > 0) { "topK must be greater than 0" }

        val body = buildJsonObject {
            put("vector", buildJsonArray { query.forEach { add(it) } })
            put("limit", topK)
            put("with_payload", true)
            if (filter.isNotEmpty()) {
                put("filter", buildJsonObject {
                    put("must", buildJsonArray {
                        filter.forEach { (k, v) ->
                            add(buildJsonObject {
                                put("key", k)
                                put("match", buildJsonObject { put("value", v) })
                            })
                        }
                    })
                })
            }
        }

        if (logEnabled) logger?.log("QdrantVectorStore: searching topK=$topK filter=$filter in '$collectionName'")

        return retryWithBackoff(maxRetries = 3) {
            val response = client.post("$baseUrl/points/search") {
                applyAuth()
                contentType(ContentType.Application.Json)
                setBody(body.toString())
            }
            checkResponse(response, "search")
            parseSearchResponse(response.bodyAsText())
        }
    }

    override suspend fun delete(id: String) {
        val body = buildJsonObject {
            put("points", buildJsonArray { add(id) })
        }

        if (logEnabled) logger?.log("QdrantVectorStore: deleting point id=$id from '$collectionName'")

        retryWithBackoff(maxRetries = 3) {
            val response = client.post("$baseUrl/points/delete") {
                applyAuth()
                contentType(ContentType.Application.Json)
                setBody(body.toString())
            }
            checkResponse(response, "delete")
        }
    }

    override suspend fun deleteAll() {
        // Delete all points by using a filter that matches everything (no conditions = match all)
        val body = buildJsonObject {
            put("filter", buildJsonObject {
                put("must", buildJsonArray {})
            })
        }

        if (logEnabled) logger?.log("QdrantVectorStore: deleting all points from '$collectionName'")

        retryWithBackoff(maxRetries = 3) {
            val response = client.post("$baseUrl/points/delete") {
                applyAuth()
                contentType(ContentType.Application.Json)
                setBody(body.toString())
            }
            checkResponse(response, "deleteAll")
        }
    }

    // --- Private helpers ---

    private fun HttpRequestBuilder.applyAuth() {
        apiKey?.let { header("api-key", it) }
    }

    private suspend fun checkResponse(response: HttpResponse, operation: String) {
        if (!response.status.isSuccess()) {
            val body = response.bodyAsText()
            throw IllegalStateException("Qdrant $operation failed [${response.status}]: $body")
        }
    }

    private fun parseSearchResponse(responseBody: String): List<VectorSearchResult> {
        return try {
            val root = json.parseToJsonElement(responseBody).jsonObject
            root["result"]?.jsonArray?.map { item ->
                val obj = item.jsonObject
                val id = obj["id"]?.jsonPrimitive?.content
                    ?: throw IllegalStateException("Missing 'id' in Qdrant result")
                val score = obj["score"]?.jsonPrimitive?.float ?: 0f
                val payload = obj["payload"]?.jsonObject
                val text = payload?.get(TEXT_PAYLOAD_KEY)?.jsonPrimitive?.content
                val metaMap = payload
                    ?.filterKeys { it != TEXT_PAYLOAD_KEY }
                    ?.mapValues { (_, v) -> v.jsonPrimitive.content }
                    ?: emptyMap()
                VectorSearchResult(id = id, score = score, text = text, metadata = metaMap)
            } ?: emptyList()
        } catch (e: Exception) {
            throw IllegalStateException("Failed to parse Qdrant search response: ${e.message}", e)
        }
    }

    companion object {
        /**
         * Payload key used internally to store the document text alongside the vector.
         * Changing this will break retrieval of text from existing points.
         */
        const val TEXT_PAYLOAD_KEY = "__text__"
    }
}
