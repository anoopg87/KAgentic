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
 * Production-ready Gemini embedding provider using the Google Generative Language API.
 *
 * Supported models:
 * - [Model.TEXT_EMBEDDING_004] — latest, recommended (768 dimensions, default)
 * - [Model.EMBEDDING_001] — legacy model (768 dimensions)
 *
 * Usage Example:
 * ```kotlin
 * val embedder = GeminiEmbeddingProvider(
 *     apiKey = System.getenv("GEMINI_API_KEY"),
 *     model = GeminiEmbeddingProvider.Model.TEXT_EMBEDDING_004
 * )
 * val vector = runBlocking { embedder.embed("Hello world") }
 * println("Dimensions: ${vector.size}")
 * ```
 *
 * @property apiKey Gemini API key.
 * @property model Embedding model to use (default: text-embedding-004).
 * @property logger Optional logger for debugging.
 * @property logEnabled Enables detailed request/response logging if true.
 */
class GeminiEmbeddingProvider(
    private val apiKey: String,
    private val model: Model = Model.TEXT_EMBEDDING_004,
    private val logger: Logger? = null,
    private val logEnabled: Boolean = false
) : EmbeddingProvider {

    enum class Model(val modelId: String) {
        TEXT_EMBEDDING_004("text-embedding-004"),
        EMBEDDING_001("embedding-001")
    }

    private val client = HttpClient()

    override suspend fun embed(input: String): List<Float> {
        val url = "$BASE_URL/${model.modelId}:embedContent?key=$apiKey"
        if (logEnabled) logger?.log("GeminiEmbeddingProvider: embedding input (${input.length} chars) with ${model.modelId}")

        return try {
            retryWithBackoff(maxRetries = 3) {
                val response: HttpResponse = client.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(buildJsonObject {
                        put("model", "models/${model.modelId}")
                        put("content", buildJsonObject {
                            put("parts", buildJsonArray {
                                add(buildJsonObject { put("text", input) })
                            })
                        })
                    }.toString())
                }

                val body = response.bodyAsText()
                if (logEnabled) logger?.log("GeminiEmbeddingProvider: response received")

                parseEmbedding(body)
            }
        } catch (e: Exception) {
            if (logEnabled) logger?.log("GeminiEmbeddingProvider error: ${e.message}")
            throw IllegalStateException("Failed to generate embedding via Gemini: ${e.message}", e)
        }
    }

    private fun parseEmbedding(responseBody: String): List<Float> {
        return try {
            val json = Json.parseToJsonElement(responseBody).jsonObject
            json["embedding"]
                ?.jsonObject
                ?.get("values")
                ?.jsonArray
                ?.map { it.jsonPrimitive.float }
                ?: throw IllegalStateException("Unexpected Gemini embedding response structure")
        } catch (e: Exception) {
            throw IllegalStateException("Failed to parse Gemini embedding response: ${e.message}", e)
        }
    }

    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    }
}
