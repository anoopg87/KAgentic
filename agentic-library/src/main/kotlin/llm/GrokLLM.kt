/**
 * GrokLLM provides access to Grok's models via HTTP API.
 * Supports multiple models, robust error handling, and logging.
 *
 * Usage Example:
 * ```kotlin
 * val logger = ConsoleLogger()
 * val grok = GrokLLM(
 *     apiKey = System.getenv("GROK_API_KEY"),
 *     model = GrokLLM.Model.GROK_4,
 *     logger = logger,
 *     logEnabled = true
 * )
 * val response = runBlocking { grok.generate("Hello Grok!") }
 * println(response)
 * ```
 *
 * @property apiKey Grok API key (must be provided by consumer).
 * @property model Grok model to use for generation.
 * @property logger Optional logger for request/response/error logging.
 * @property logEnabled Enables detailed logging if true.
 */
package llm

import core.Logger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.encodeToJsonElement

class GrokLLM(
    val apiKey: String,
    val model: Model = Model.GROK_4,
    private val logger: Logger? = null,
    private val logEnabled: Boolean = false
) : LLMProvider {
    enum class Model(val modelName: String) {
        GROK_4("grok-4"),
        GROK_3_SERIES("grok-3-series"),
        BABY_GROK("baby-grok")
    }

    companion object {
        private const val API_URL = "https://api.grok.com/v1/chat/completions"
        private const val AUTH_HEADER = "Authorization"
        private const val AUTH_PREFIX = "Bearer "
        private const val CONTENT_TYPE = "application/json"
    }

    private val client = HttpClient()

    override suspend fun generate(input: String): String {
        val url = API_URL
        val payload = buildJsonObject {
            put("model", model.modelName)
            put("messages", Json.encodeToJsonElement(listOf(mapOf("role" to "user", "content" to input))))
        }
        return try {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                header(AUTH_HEADER, AUTH_PREFIX + apiKey)
                setBody(Json.encodeToJsonElement(payload))
            }
            val responseBody = response.bodyAsText()
            if (logEnabled) logger?.log("GrokLLM request: $payload\nResponse: $responseBody")
            responseBody
        } catch (e: Exception) {
            if (logEnabled) logger?.log("GrokLLM error: ${e.message}")
            "Error: ${e.message}"
        }
    }
}
