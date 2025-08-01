/**
 * FileReaderTool reads local text files for agentic workflows.
 *
 * Usage Example:
 * ```kotlin
 * val reader = FileReaderTool()
 * val canHandle = reader.canHandle("read file example.txt") // true
 * val result = runBlocking { reader.handle("example.txt") }
 * ```
 */
package tools

import java.io.File

class FileReaderTool: ToolHandler {
    override fun canHandle(input: String): Boolean {
        // Simple check: contains 'read file' or ends with .txt/.md/.csv
        return input.contains("read file", ignoreCase = true) || input.matches(Regex(".*\\.(txt|md|csv)$"))
    }

    override fun score(input: String): Int {
        // High score for explicit file read/open, low otherwise
        return when {
            input.contains("read file", ignoreCase = true) -> 10
            input.matches(Regex(".*\\.(txt|md|csv)$")) -> 8
            else -> 1
        }
    }

    override suspend fun handle(input: String): String {
        return try {
            val file = File(input)
            if (file.exists()) file.readText() else "File not found: $input"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
