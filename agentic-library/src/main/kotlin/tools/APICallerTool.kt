/**
 * APICallerTool enables HTTP API calls for agentic workflows.
 *
 * Usage Example:
 * ```kotlin
 * val api = APICallerTool()
 * val canHandle = api.canHandle("call api https://api.example.com/data") // true
 * val result = runBlocking { api.handle("https://api.example.com/data") }
 * ```
 */
package tools

import java.net.HttpURLConnection
import java.net.URL

class APICallerTool: ToolHandler {
    override fun canHandle(input: String): Boolean {
        // Simple check: contains 'call api' or starts with 'http'
        return input.contains("call api", ignoreCase = true) || input.startsWith("http")
    }

    override fun score(input: String): Int {
        // High score for explicit API call, medium for http, low otherwise
        return when {
            input.contains("call api", ignoreCase = true) -> 10
            input.startsWith("http") -> 8
            else -> 1
        }
    }

    override suspend fun handle(input: String): String {
        return try {
            val url = URL(input)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            val response = conn.inputStream.bufferedReader().readText()
            conn.disconnect()
            response
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
