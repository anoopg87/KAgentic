package tools

/**
 * [YourToolName] - Brief description of what this tool does
 *
 * Usage Example:
 * ```kotlin
 * val tool = YourToolName()
 * val result = runBlocking { tool.handle("input here") }
 * println(result)
 * ```
 */
class YourToolName : ToolHandler {

    override fun canHandle(input: String): Boolean {
        // TODO: Implement pattern matching logic
        // Examples:
        // - Keyword matching: input.contains("keyword", ignoreCase = true)
        // - Regex patterns: input.matches(Regex("pattern"))
        // - Multiple conditions: condition1 && condition2

        return input.contains("keyword", ignoreCase = true)
    }

    override fun score(input: String): Int {
        // TODO: Implement scoring logic (1-10)
        // Higher scores indicate better match priority

        return when {
            input.startsWith("exact match", ignoreCase = true) -> 10  // Highest priority
            input.contains("good match", ignoreCase = true) -> 7      // Medium priority
            input.contains("keyword", ignoreCase = true) -> 5          // Low-medium priority
            else -> 1                                                   // Minimum priority
        }
    }

    override suspend fun handle(input: String): String {
        // TODO: Implement your tool's core logic
        // This is a suspend function - you can make async calls

        return try {
            // Your implementation here
            val result = processInput(input)
            "Result: $result"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    private fun processInput(input: String): String {
        // TODO: Add your tool-specific logic
        return "processed: $input"
    }
}
