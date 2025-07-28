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

class DeepSeekLLM(
    val apiKey: String,
    val model: Model = Model.DEEPSEEK_V3_0324,
    private val logger: Logger? = null,
    private val logEnabled: Boolean = false
) : LLMProvider {
    enum class Model(val modelName: String) {
        DEEPSEEK_V3_0324("deepseek-v3-0324"),
        DEEPSEEK_V3_OPEN_SOURCE("deepseek-v3-open-source"),
        NEMOTRON("nemotron")
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
            if (logEnabled) logger?.log("DeepSeekLLM request: $payload\nResponse: $responseBody")
            responseBody
        } catch (e: Exception) {
            if (logEnabled) logger?.log("DeepSeekLLM error: ${e.message}")
            "Error: ${e.message}"
        }
    }

    companion object {
        private const val API_URL = "https://api.deepseek.com/v1/chat/completions"
        private const val AUTH_HEADER = "Authorization"
        private const val AUTH_PREFIX = "Bearer "
        private const val CONTENT_TYPE = "application/json"
    }
}
