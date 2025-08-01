/**
 * OllamaLLM provides access to local LLMs via the Ollama HTTP API.
 * Supports multiple models, robust error handling, and logging.
 *
 * Usage Example:
 * ```kotlin
 * val logger = ConsoleLogger()
 * val ollama = OllamaLLM(
 *     model = OllamaLLM.Model.LLAMA2,
 *     endpoint = "http://localhost:11434",
 *     logger = logger,
 *     logEnabled = true
 * )
 * val response = runBlocking { ollama.generate("Hello Llama!") }
 * println(response)
 * ```
 *
 * @property model Ollama model to use for generation.
 * @property endpoint Ollama server endpoint URL.
 * @property logger Optional logger for request/response/error logging.
 * @property logEnabled Enables detailed logging if true.
 */
package llm

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.encodeToJsonElement

class OllamaLLM(
    val model: Model,
    val endpoint: String,
    private val logger: core.Logger? = null,
    private val logEnabled: Boolean = false
): LLMProvider {
    enum class Model(val modelName: String) {
        LLAMA2("llama2"),
        MISTRAL("mistral"),
        CODELLAMA("codellama")
    }
    private val client = HttpClient()

    override suspend fun generate(input: String): String {
        val url = "$endpoint/api/generate"
        val payload = buildJsonObject {
            put("model", model.modelName)
            put("prompt", input)
        }
        if (logEnabled) logger?.log("OllamaLLM request: $input to $url")
        return try {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToJsonElement(payload))
            }
            val responseBody = response.bodyAsText()
            if (logEnabled) logger?.log("OllamaLLM response: $responseBody")
            responseBody
        } catch (e: Exception) {
            if (logEnabled) logger?.log("OllamaLLM error: ${e.message}\nStackTrace: ${e.stackTraceToString()}")
            "Error: ${e.message}"
        }
    }
}
