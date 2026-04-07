package embeddings

import core.Logger
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import llm.EmbeddingProvider
import llm.retryWithBackoff

/**
 * Production-ready OpenAI embedding provider using the Embeddings API.
 *
 * Supported models:
 * - [Model.TEXT_EMBEDDING_3_SMALL] — fast, cost-efficient (default, 1536 dimensions)
 * - [Model.TEXT_EMBEDDING_3_LARGE] — highest accuracy (3072 dimensions)
 * - [Model.TEXT_EMBEDDING_ADA_002] — legacy model (1536 dimensions)
 *
 * Usage Example:
 * ```kotlin
 * val embedder = OpenAIEmbeddingProvider(
 *     apiKey = System.getenv("OPENAI_API_KEY"),
 *     model = OpenAIEmbeddingProvider.Model.TEXT_EMBEDDING_3_SMALL
 * )
 * val vector = runBlocking { embedder.embed("Hello world") }
 * println("Dimensions: ${vector.size}")
 * ```
 *
 * @property apiKey OpenAI API key.
 * @property model Embedding model to use (default: text-embedding-3-small).
 * @property logger Optional logger for debugging.
 * @property logEnabled Enables detailed request/response logging if true.
 */
class OpenAIEmbeddingProvider(
    private val apiKey: String,
    val model: Model = Model.TEXT_EMBEDDING_3_SMALL,
    private val logger: Logger? = null,
    private val logEnabled: Boolean = false
) : EmbeddingProvider {

    enum class Model(val modelId: String) {
        TEXT_EMBEDDING_3_SMALL("text-embedding-3-small"),
        TEXT_EMBEDDING_3_LARGE("text-embedding-3-large"),
        TEXT_EMBEDDING_ADA_002("text-embedding-ada-002")
    }

    private val client = HttpClient()

    override suspend fun embed(input: String): List<Float> {
        if (logEnabled) logger?.log("OpenAIEmbeddingProvider: embedding input (${input.length} chars) with ${model.modelId}")

        return try {
            retryWithBackoff(maxRetries = 3) {
                val response: HttpResponse = client.post(API_URL) {
                    header("Authorization", "Bearer $apiKey")
                    contentType(ContentType.Application.Json)
                    setBody(buildJsonObject {
                        put("input", input)
                        put("model", model.modelId)
                    }.toString())
                }

                val body = response.bodyAsText()
                if (logEnabled) logger?.log("OpenAIEmbeddingProvider: response received")

                parseEmbedding(body)
            }
        } catch (e: Exception) {
            if (logEnabled) logger?.log("OpenAIEmbeddingProvider error: ${e.message}")
            throw IllegalStateException("Failed to generate embedding via OpenAI: ${e.message}", e)
        }
    }

    private fun parseEmbedding(responseBody: String): List<Float> {
        return try {
            val json = Json.parseToJsonElement(responseBody).jsonObject
            json["data"]
                ?.jsonArray
                ?.get(0)
                ?.jsonObject
                ?.get("embedding")
                ?.jsonArray
                ?.map { it.jsonPrimitive.float }
                ?: throw IllegalStateException("Unexpected OpenAI embedding response structure")
        } catch (e: Exception) {
            throw IllegalStateException("Failed to parse OpenAI embedding response: ${e.message}", e)
        }
    }

    companion object {
        private const val API_URL = "https://api.openai.com/v1/embeddings"
    }
}
