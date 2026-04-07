package vectorstore.pinecone

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
 * Production-ready Pinecone vector store implementation using the Pinecone REST API v1.
 *
 * Supports upsert, batch upsert, semantic search with metadata filtering, single delete,
 * and full namespace clear — all with automatic retry on transient failures.
 *
 * ## Setup
 *
 * 1. Create a Pinecone index at https://app.pinecone.io
 * 2. Set the index dimension to match your embedding model output
 *    (e.g., 1536 for OpenAI text-embedding-3-small)
 * 3. Copy the index host URL from the Pinecone console
 *
 * ## Usage Example
 *
 * ```kotlin
 * val store = PineconeVectorStore(
 *     apiKey    = System.getenv("PINECONE_API_KEY"),
 *     indexHost = "https://my-index-abc123.svc.us-east1-gcp.pinecone.io"
 * )
 *
 * runBlocking {
 *     store.upsert(VectorDocument(
 *         id       = "doc-1",
 *         vector   = embedder.embed("Hello from Pinecone"),
 *         text     = "Hello from Pinecone",
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
 * @property apiKey Pinecone API key.
 * @property indexHost Full index host URL (e.g., https://my-index-xxx.svc.environment.pinecone.io).
 * @property namespace Pinecone namespace for logical data isolation (default: empty string = default namespace).
 * @property logger Optional logger for debugging.
 * @property logEnabled Enables detailed request/response logging if true.
 */
class PineconeVectorStore(
    private val apiKey: String,
    private val indexHost: String,
    private val namespace: String = "",
    private val logger: Logger? = null,
    private val logEnabled: Boolean = false
) : VectorStore {

    private val client = HttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun upsert(document: VectorDocument) {
        upsertAll(listOf(document))
    }

    override suspend fun upsertAll(documents: List<VectorDocument>) {
        if (documents.isEmpty()) return

        val vectors = buildJsonArray {
            documents.forEach { doc ->
                add(buildJsonObject {
                    put("id", doc.id)
                    put("values", buildJsonArray { doc.vector.forEach { add(it) } })
                    if (doc.metadata.isNotEmpty() || doc.text != null) {
                        put("metadata", buildJsonObject {
                            doc.metadata.forEach { (k, v) -> put(k, v) }
                            doc.text?.let { put(TEXT_METADATA_KEY, it) }
                        })
                    }
                })
            }
        }

        val body = buildJsonObject {
            put("vectors", vectors)
            if (namespace.isNotEmpty()) put("namespace", namespace)
        }

        if (logEnabled) logger?.log("PineconeVectorStore: upserting ${documents.size} vectors")

        retryWithBackoff(maxRetries = 3) {
            val response = client.post("$indexHost/vectors/upsert") {
                header("Api-Key", apiKey)
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
            put("topK", topK)
            put("includeMetadata", true)
            if (namespace.isNotEmpty()) put("namespace", namespace)
            if (filter.isNotEmpty()) {
                put("filter", buildJsonObject {
                    filter.forEach { (k, v) -> put(k, buildJsonObject { put("\$eq", v) }) }
                })
            }
        }

        if (logEnabled) logger?.log("PineconeVectorStore: searching topK=$topK filter=$filter")

        return retryWithBackoff(maxRetries = 3) {
            val response = client.post("$indexHost/query") {
                header("Api-Key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(body.toString())
            }
            checkResponse(response, "query")
            parseSearchResponse(response.bodyAsText())
        }
    }

    override suspend fun delete(id: String) {
        val body = buildJsonObject {
            put("ids", buildJsonArray { add(id) })
            if (namespace.isNotEmpty()) put("namespace", namespace)
        }

        if (logEnabled) logger?.log("PineconeVectorStore: deleting id=$id")

        retryWithBackoff(maxRetries = 3) {
            val response = client.post("$indexHost/vectors/delete") {
                header("Api-Key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(body.toString())
            }
            checkResponse(response, "delete")
        }
    }

    override suspend fun deleteAll() {
        val body = buildJsonObject {
            put("deleteAll", true)
            if (namespace.isNotEmpty()) put("namespace", namespace)
        }

        if (logEnabled) logger?.log("PineconeVectorStore: deleting all vectors in namespace='$namespace'")

        retryWithBackoff(maxRetries = 3) {
            val response = client.post("$indexHost/vectors/delete") {
                header("Api-Key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(body.toString())
            }
            checkResponse(response, "deleteAll")
        }
    }

    // --- Private helpers ---

    private suspend fun checkResponse(response: HttpResponse, operation: String) {
        if (!response.status.isSuccess()) {
            val body = response.bodyAsText()
            throw IllegalStateException("Pinecone $operation failed [${response.status}]: $body")
        }
    }

    private fun parseSearchResponse(responseBody: String): List<VectorSearchResult> {
        return try {
            val root = json.parseToJsonElement(responseBody).jsonObject
            root["matches"]?.jsonArray?.map { match ->
                val obj = match.jsonObject
                val id = obj["id"]?.jsonPrimitive?.content
                    ?: throw IllegalStateException("Missing 'id' in Pinecone match")
                val score = obj["score"]?.jsonPrimitive?.float ?: 0f
                val metadata = obj["metadata"]?.jsonObject
                val text = metadata?.get(TEXT_METADATA_KEY)?.jsonPrimitive?.content
                val metaMap = metadata
                    ?.filterKeys { it != TEXT_METADATA_KEY }
                    ?.mapValues { (_, v) -> v.jsonPrimitive.content }
                    ?: emptyMap()
                VectorSearchResult(id = id, score = score, text = text, metadata = metaMap)
            } ?: emptyList()
        } catch (e: Exception) {
            throw IllegalStateException("Failed to parse Pinecone search response: ${e.message}", e)
        }
    }

    companion object {
        /**
         * Metadata key used internally to store the document text alongside the vector.
         * Changing this will break retrieval of text from existing documents.
         */
        const val TEXT_METADATA_KEY = "__text__"
    }
}
