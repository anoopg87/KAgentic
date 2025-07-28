/**
 * SimpleChatModelProvider is a dummy implementation of ChatModelProvider.
 * Returns the last message for demonstration purposes.
 *
 * Usage Example:
 * ```kotlin
 * val chatModel = SimpleChatModelProvider()
 * val response = runBlocking { chatModel.chat(listOf("Hello", "How are you?")) }
 * println(response)
 * ```
 */
package llm

class SimpleChatModelProvider : ChatModelProvider {
    override suspend fun chat(messages: List<String>): String {
        // Simple echo for demonstration: returns last message
        return messages.lastOrNull() ?: ""
    }
}
