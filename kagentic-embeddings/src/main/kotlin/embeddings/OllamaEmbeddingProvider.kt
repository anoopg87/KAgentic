package embeddings

import core.Logger
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import llm.EmbeddingProvider

/**
 * Production-ready Ollama embedding provider for locally-hosted embedding models.
 *
 * Runs entirely on-premise — no API keys or external network calls required.
 * Requires a running Ollama instance with the desired model pulled.
 *
 * Recommended embedding models:
 * - [Model.NOMIC_EMBED_TEXT] — fast, high quality (768 dimensions, default)
 * - [Model.MXBAI_EMBED_LARGE] — larger, higher accuracy (1024 dimensions)
 * - [Model.ALL_MINILM] — lightweight (384 dimensions)
 *
 * Setup:
 * ```bash
 * ollama pull nomic-embed-text
 * ollama serve
 * ```
 *
 * Usage Example:
 * ```kotlin
 * val embedder = OllamaEmbeddingProvider(
 *     model = OllamaEmbeddingProvider.Model.NOMIC_EMBED_TEXT,
 *     endpoint = "http://localhost:11434"
 * )
 * val vector = runBlocking { embedder.embed("Hello world") }
 * println("Dimensions: ${vector.size}")
 * ```
 *
 * @property model Ollama embedding model to use.
 * @property endpoint Base URL of the Ollama server (default: http://localhost:11434).
 * @property logger Optional logger for debugging.
 * @property logEnabled Enables detailed request/response logging if true.
 */
class OllamaEmbeddingProvider(
    private val model: Model = Model.NOMIC_EMBED_TEXT,
    private val endpoint: String = "http://localhost:11434",
    private val logger: Logger? = null,
    private val logEnabled: Boolean = false
) : EmbeddingProvider {

    enum class Model(val modelId: String) {
        NOMIC_EMBED_TEXT("nomic-embed-text"),
        MXBAI_EMBED_LARGE("mxbai-embed-large"),
        ALL_MINILM("all-minilm")
    }

    private val client = HttpClient()

    override suspend fun embed(input: String): List<Float> {
        val url = "$endpoint/api/embeddings"
        if (logEnabled) logger?.log("OllamaEmbeddingProvider: embedding with ${model.modelId} at $endpoint")

        return try {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("model", model.modelId)
                    put("prompt", input)
                }.toString())
            }

            val body = response.bodyAsText()
            if (logEnabled) logger?.log("OllamaEmbeddingProvider: response received")

            parseEmbedding(body)
        } catch (e: Exception) {
            if (logEnabled) logger?.log("OllamaEmbeddingProvider error: ${e.message}")
            throw IllegalStateException("Failed to generate embedding via Ollama at $endpoint: ${e.message}", e)
        }
    }

    private fun parseEmbedding(responseBody: String): List<Float> {
        return try {
            val json = Json.parseToJsonElement(responseBody).jsonObject
            json["embedding"]
                ?.jsonArray
                ?.map { it.jsonPrimitive.float }
                ?: throw IllegalStateException("Unexpected Ollama embedding response structure")
        } catch (e: Exception) {
            throw IllegalStateException("Failed to parse Ollama embedding response: ${e.message}", e)
        }
    }
}
