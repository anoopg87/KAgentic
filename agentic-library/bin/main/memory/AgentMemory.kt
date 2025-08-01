/**
 * AgentMemory is the base interface for agentic memory systems.
 * Supports thread-safe storage and retrieval of conversation history.
 *
 * Usage Example:
 * ```kotlin
 * val memory = ConversationMemory()
 * runBlocking { memory.store("user_input", "Hello!") }
 * val history = runBlocking { memory.retrieve("history") }
 * println(history)
 * ```
 */
/**
 * ConversationMemory is a thread-safe implementation of AgentMemory.
 * Uses a Mutex for safe concurrent access.
 *
 * Usage Example:
 * ```kotlin
 * val memory = ConversationMemory()
 * runBlocking { memory.store("user_input", "Hello!") }
 * runBlocking { memory.store("agent_response", "Hi!") }
 * val history = runBlocking { memory.getHistory() }
 * println(history)
 * ```
 */


package memory

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class Exchange(val userInput: String, val agentResponse: String)

interface AgentMemory {
    suspend fun store(key: String, value: String)
    suspend fun retrieve(key: String): String?
    suspend fun getHistory(): List<Exchange>
}

class ConversationMemory : AgentMemory {
    private val history = mutableListOf<Exchange>()
    private val mutex = Mutex()

    override suspend fun store(key: String, value: String) {
        mutex.withLock {
            if (key == "user_input") {
                history.add(Exchange(value, ""))
            } else if (key == "agent_response" && history.isNotEmpty()) {
                val last = history.last()
                history[history.lastIndex] = last.copy(agentResponse = value)
            } else {
                // No action for other keys
            }
        }
    }

    override suspend fun retrieve(key: String): String? {
        return mutex.withLock {
            when (key) {
                "history" -> history.joinToString("\n") { "User: ${it.userInput}\nAgent: ${it.agentResponse}" }
                else -> null
            }
        }
    }

    override suspend fun getHistory(): List<Exchange> = mutex.withLock { history.toList() }
}
