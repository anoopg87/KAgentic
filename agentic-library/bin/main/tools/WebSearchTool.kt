/**
 * WebSearchTool enables web search queries for agentic workflows.
 * Uses DuckDuckGo API for demonstration.
 *
 * Usage Example:
 * ```kotlin
 * val search = WebSearchTool()
 * val canHandle = search.canHandle("search Kotlin") // true
 * val result = runBlocking { search.handle("search Kotlin") }
 * ```
 */
package tools

import java.net.HttpURLConnection
import java.net.URL

class WebSearchTool: ToolHandler {
    override fun canHandle(input: String): Boolean {
        // Simple check: contains 'search' or 'find'
        return input.contains("search", ignoreCase = true) || input.contains("find", ignoreCase = true)
    }
    
    override fun score(input: String): Int {
        // High score for explicit search, medium for 'find', low otherwise
        return when {
            input.contains("search", ignoreCase = true) -> 10
            input.contains("find", ignoreCase = true) -> 7
            else -> 1
        }
    }

    override suspend fun handle(input: String): String {
        return try {
            val query = input.replace(" ", "+")
            val url = URL("https://api.duckduckgo.com/?q=$query&format=json")
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
