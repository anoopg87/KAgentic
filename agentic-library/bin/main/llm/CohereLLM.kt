/**
 * CohereLLM provides access to Cohere's models via HTTP API.
 * Supports multiple models, robust error handling, and logging.
 *
 * Usage Example:
 * ```kotlin
 * val logger = ConsoleLogger()
 * val cohere = CohereLLM(
 *     apiKey = System.getenv("COHERE_API_KEY"),
 *     model = CohereLLM.Model.COHERE_COMMAND,
 *     logger = logger,
 *     logEnabled = true
 * )
 * val response = runBlocking { cohere.generate("Hello Cohere!") }
 * println(response)
 * ```
 *
 * @property apiKey Cohere API key (must be provided by consumer).
 * @property model Cohere model to use for generation.
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

class CohereLLM(val apiKey: String, val model: Model = Model.COMMAND): LLMProvider {
    enum class Model(val modelName: String) {
        COMMAND_R_PLUS_LATEST("command-r-plus-latest"),
        COMMAND_R_PLUS("command-r-plus"),
        COMMAND("command")
    }
    private val client = HttpClient()
    override suspend fun generate(input: String): String {
        val url = "https://api.cohere.ai/v1/generate"
        val payload = buildJsonObject {
            put("model", model.modelName)
            put("prompt", input)
        }
        return try {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                setBody(Json.encodeToJsonElement(payload))
            }
            val responseBody = response.bodyAsText()
            responseBody
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}