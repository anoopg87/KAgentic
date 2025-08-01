/**
 * ToolHandler is the base interface for all agentic tools.
 * Each tool must implement canHandle, handle, and score methods.
 *
 * Usage Example:
 * ```kotlin
 * class MyTool : ToolHandler {
 *     override fun canHandle(input: String) = input.contains("mytool")
 *     override suspend fun handle(input: String) = "Handled: $input"
 *     override fun score(input: String) = if (canHandle(input)) 10 else 0
 * }
 * ```
 */
package tools

interface ToolHandler {
    fun canHandle(input: String): Boolean
    suspend fun handle(input: String): String
    fun score(input: String): Int
}
