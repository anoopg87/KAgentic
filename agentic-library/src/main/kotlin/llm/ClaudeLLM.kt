/**
 * ClaudeLLM provides access to Anthropic's Claude models via HTTP API.
 * Supports multiple models, robust error handling, and logging.
 *
 * Usage Example:
 * ```kotlin
 * val logger = ConsoleLogger()
 * val claude = ClaudeLLM(
 *     apiKey = System.getenv("CLAUDE_API_KEY"),
 *     model = ClaudeLLM.Model.CLAUDE_OPUS_4,
 *     logger = logger,
 *     logEnabled = true
 * )
 * val response = runBlocking { claude.generate("Hello Claude!") }
 * println(response)
 * ```
 *
 * @property apiKey Claude API key (must be provided by consumer).
 * @property model Claude model to use for generation.
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

class ClaudeLLM(
    val apiKey: String,
    val model: Model = Model.CLAUDE_OPUS_4,
    private val logger: Logger? = null,
    private val logEnabled: Boolean = false
) : LLMProvider {
    enum class Model(val modelName: String) {
        CLAUDE_OPUS_4("claude-opus-4"),
        CLAUDE_SONNET_4("claude-sonnet-4"),
        CLAUDE_3_5_SERIES("claude-3.5-series")
    }

    companion object {
        private const val API_URL = "https://api.anthropic.com/v1/messages"
        private const val API_KEY_HEADER = "x-api-key"
        private const val VERSION_HEADER = "anthropic-version"
        private const val VERSION = "2023-06-01"
        private const val CONTENT_TYPE = "application/json"
    }

    private val client = HttpClient()

    override suspend fun generate(input: String): String {
        val url = API_URL
        val payload = buildJsonObject {
            put("model", model.modelName)
            put("max_tokens", 1024)
            put("messages", Json.encodeToJsonElement(listOf(mapOf("role" to "user", "content" to input))))
        }
        if (logEnabled) logger?.log("ClaudeLLM request: $input to $url")
        return try {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                header(API_KEY_HEADER, apiKey)
                header(VERSION_HEADER, VERSION)
                setBody(Json.encodeToJsonElement(payload))
            }
            val responseBody = response.bodyAsText()
            if (logEnabled) logger?.log("ClaudeLLM response: $responseBody")
            responseBody
        } catch (e: Exception) {
            if (logEnabled) logger?.log("ClaudeLLM error: ${e.message}\nStackTrace: ${e.stackTraceToString()}")
            "Error: ${e.message}"
        }
    }
}
