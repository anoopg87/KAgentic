/**
 * OpenAILLM provides access to OpenAI's GPT models via HTTP API.
 * Supports multiple models, robust error handling, and logging.
 *
 * Usage Example:
 * ```kotlin
 * val logger = ConsoleLogger()
 * val openai = OpenAILLM(
 *     apiKey = System.getenv("OPENAI_API_KEY"),
 *     model = OpenAILLM.Model.GPT_5,
 *     logger = logger,
 *     logEnabled = true
 * )
 * val response = runBlocking { openai.generate("Hello GPT!") }
 * println(response)
 * ```
 *
 * @property apiKey OpenAI API key (must be provided by consumer).
 * @property model OpenAI model to use for generation.
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

class OpenAILLM(
    val apiKey: String,
    val model: Model = Model.GPT_5,
    private val logger: Logger? = null,
    private val logEnabled: Boolean = false
) : LLMProvider {
    enum class Model(val modelName: String) {
        GPT_5("gpt-5"),
        O3("o3"),
        O4_MINI("o4-mini")
    }

    private val client = HttpClient()

    override suspend fun generate(input: String): String {
        val url = API_URL
        val payload = buildJsonObject {
            put("model", model.modelName)
            put("messages", Json.encodeToJsonElement(listOf(mapOf("role" to "user", "content" to input))))
        }
        if (logEnabled) logger?.log("OpenAILLM request: $input to $url")
        return try {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                setBody(Json.encodeToJsonElement(payload))
            }
            val responseBody = response.bodyAsText()
            if (logEnabled) logger?.log("OpenAILLM response: $responseBody")
            responseBody
        } catch (e: Exception) {
            if (logEnabled) logger?.log("OpenAILLM error: ${e.message}\nStackTrace: ${e.stackTraceToString()}")
            "Error: ${e.message}"
        }
    }

    companion object {
        private const val API_URL = "https://api.openai.com/v1/chat/completions"
        private const val AUTH_HEADER = "Authorization"
        private const val AUTH_PREFIX = "Bearer "
        private const val CONTENT_TYPE = "application/json"
    }
}