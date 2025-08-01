package llm

import core.Logger
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement

/**
 * GeminiLLM provides access to Google's Gemini language models via HTTP API.
 * Supports multiple Gemini models, robust error handling, and logging.
 *
 * Usage Example:
 * ```kotlin
 * val logger = ConsoleLogger()
 * val gemini = GeminiLLM(
 *     apiKey = System.getenv("GEMINI_API_KEY"),
 *     model = GeminiLLM.Model.GEMINI_2_5_PRO_EXPERIMENTAL,
 *     logger = logger,
 *     logEnabled = true
 * )
 * val response = runBlocking { gemini.generate("Hello Gemini!") }
 * println(response)
 * ```
 *
 * @property apiKey Gemini API key (must be provided by consumer).
 * @property model Gemini model to use for generation.
 * @property logger Optional logger for request/response/error logging.
 * @property logEnabled Enables detailed logging if true.
 */
class GeminiLLM(
    val apiKey: String,
    val model: Model = Model.GEMINI_2_5_PRO_EXPERIMENTAL,
    private val logger: Logger? = null,
    private val logEnabled: Boolean = false
) : LLMProvider {
    enum class Model(val modelName: String) {
        GEMINI_2_5_PRO_EXPERIMENTAL("gemini-2.5-pro-experimental"),
        GEMINI_2_5_FLASH_002("gemini-2.5-flash-002"),
        GEMINI_ULTRA_SERIES("gemini-ultra-series")
    }

    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta2/models/"
        private const val URL_SUFFIX = ":generateContent?key="
        private const val CONTENT_TYPE = "application/json"
    }

    private val client = HttpClient()

    override suspend fun generate(input: String): String {
        val url = BASE_URL + model.modelName + URL_SUFFIX + apiKey
        val payload = buildJsonObject {
            put("contents", Json.encodeToJsonElement(listOf(mapOf("parts" to listOf(mapOf("text" to input))))))
        }
        if (logEnabled) logger?.log("GeminiLLM request: $input to $url")
        return try {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToJsonElement(payload))
            }
            val responseBody = response.bodyAsText()
            if (logEnabled) logger?.log("GeminiLLM response: $responseBody")
            responseBody
        } catch (e: Exception) {
            if (logEnabled) logger?.log("GeminiLLM error: ${e.message}\nStackTrace: ${e.stackTraceToString()}")
            "Error: ${e.message}"
        }
    }
}