/**
 * ChatModelProvider defines the interface for multi-turn chat models.
 * Can be implemented using local or remote models.
 *
 * Usage Example:
 * ```kotlin
 * val chatModel = SimpleChatModelProvider()
 * val response = runBlocking { chatModel.chat(listOf("Hello", "How are you?")) }
 * println(response)
 * ```
 */
package llm

interface ChatModelProvider {
    suspend fun chat(messages: List<String>): String
}
