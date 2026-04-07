package memory

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Custom Memory Strategy Template
 *
 * TODO: Rename this class to match your use case
 * Examples: PersistentMemory, RedisMemory, LimitedMemory, etc.
 *
 * This template provides a thread-safe memory implementation.
 * Customize the storage mechanism and behavior as needed.
 */
class CustomMemoryStrategy : AgentMemory {
    // ========================================
    // Storage
    // ========================================

    private val history = mutableListOf<Exchange>()
    private val storage = mutableMapOf<String, String>()

    // Thread safety
    private val mutex = Mutex()

    // TODO: Add custom storage (database, file, Redis, etc.)
    // Example:
    // private val database: Database
    // private val redis: Jedis
    // private val file: File

    // ========================================
    // Constructor
    // ========================================

    // TODO: Add constructor parameters for configuration
    // Examples:
    // constructor(maxSize: Int)
    // constructor(filePath: String)
    // constructor(redisHost: String, redisPort: Int)

    init {
        // TODO: Initialize custom storage
        // Examples:
        // - Load from file
        // - Connect to database
        // - Connect to Redis
    }

    // ========================================
    // Interface Implementation
    // ========================================

    override suspend fun store(key: String, value: String) {
        mutex.withLock {
            when (key) {
                "user_input" -> {
                    // Add new exchange to history
                    history.add(Exchange(value, ""))
                    storage[key] = value

                    // TODO: Add custom persistence logic
                    // Examples:
                    // - Save to file
                    // - Insert into database
                    // - Push to Redis
                }

                "agent_response" -> {
                    // Update last exchange with response
                    if (history.isNotEmpty()) {
                        val last = history.last()
                        history[history.lastIndex] = last.copy(agentResponse = value)
                    }
                    storage[key] = value

                    // TODO: Add custom persistence logic
                }

                else -> {
                    // Store custom key
                    storage[key] = value

                    // TODO: Add custom logic for other keys
                }
            }

            // TODO: Add custom features
            // Examples:
            // - Enforce size limits
            // - Apply TTL
            // - Trigger callbacks
        }
    }

    override suspend fun retrieve(key: String): String? {
        return mutex.withLock {
            when (key) {
                "history" -> {
                    // Format full conversation history
                    history.joinToString("\n") {
                        "User: ${it.userInput}\nAgent: ${it.agentResponse}"
                    }
                }

                else -> {
                    // Retrieve custom key
                    storage[key]

                    // TODO: Add custom retrieval logic
                    // Examples:
                    // - Load from database
                    // - Get from Redis
                    // - Check TTL before returning
                }
            }
        }
    }

    override suspend fun getHistory(): List<Exchange> {
        return mutex.withLock {
            // Return copy, not mutable reference
            history.toList()

            // TODO: For external storage, fetch from source
            // Examples:
            // - Load from database
            // - Retrieve from Redis
        }
    }

    // ========================================
    // Custom Methods (Optional)
    // ========================================

    /**
     * Clear all stored data
     */
    suspend fun clear() {
        mutex.withLock {
            history.clear()
            storage.clear()

            // TODO: Clear external storage
            // Examples:
            // - Delete file
            // - Truncate database table
            // - Clear Redis keys
        }
    }

    /**
     * Get current size of history
     */
    suspend fun size(): Int {
        return mutex.withLock {
            history.size
        }
    }

    /**
     * TODO: Add custom methods as needed
     *
     * Examples:
     *
     * suspend fun search(query: String): List<Exchange> {
     *     return mutex.withLock {
     *         history.filter {
     *             it.userInput.contains(query) ||
     *             it.agentResponse.contains(query)
     *         }
     *     }
     * }
     *
     * suspend fun getMetrics(): Map<String, Int> {
     *     return mutex.withLock {
     *         mapOf(
     *             "total_exchanges" to history.size,
     *             "stored_keys" to storage.size
     *         )
     *     }
     * }
     *
     * suspend fun export(filePath: String) {
     *     mutex.withLock {
     *         File(filePath).writeText(
     *             history.joinToString("\n") {
     *                 "${it.userInput}|${it.agentResponse}"
     *             }
     *         )
     *     }
     * }
     */
}

// ========================================
// Usage Example
// ========================================

/**
 * Example usage with AgentFramework
 */
/*
fun main() = runBlocking {
    // Create custom memory
    val memory = CustomMemoryStrategy()

    // Create agent with custom memory
    val agent = AgentFramework(
        llm = OpenAILLM(apiKey = System.getenv("OPENAI_API_KEY")),
        tools = listOf(CalculatorTool()),
        memory = memory  // Use custom memory
    )

    // Use agent
    val response1 = agent.chat("Hello")
    val response2 = agent.chat("What is 2+2?")

    // Access memory
    val history = memory.getHistory()
    println("Conversation history:")
    history.forEach { exchange ->
        println("User: ${exchange.userInput}")
        println("Agent: ${exchange.agentResponse}")
        println("---")
    }

    // Use custom methods
    val size = memory.size()
    println("Total exchanges: $size")
}
*/

// ========================================
// Implementation Examples
// ========================================

/**
 * Example 1: Memory with Size Limit
 */
/*
class LimitedMemory(private val maxSize: Int = 100) : AgentMemory {
    private val history = mutableListOf<Exchange>()
    private val mutex = Mutex()

    override suspend fun store(key: String, value: String) {
        mutex.withLock {
            if (key == "user_input") {
                if (history.size >= maxSize) {
                    history.removeAt(0)  // Remove oldest
                }
                history.add(Exchange(value, ""))
            }
        }
    }
    // ... rest of implementation
}
*/

/**
 * Example 2: File-Persisted Memory
 */
/*
class FileMemory(private val filePath: String) : AgentMemory {
    private val history = mutableListOf<Exchange>()
    private val mutex = Mutex()
    private val file = File(filePath)

    init {
        // Load from file on startup
        if (file.exists()) {
            history.addAll(loadFromFile())
        }
    }

    private fun loadFromFile(): List<Exchange> {
        return file.readLines().map { line ->
            val (input, response) = line.split("|")
            Exchange(input, response)
        }
    }

    private fun saveToFile() {
        file.writeText(
            history.joinToString("\n") { "${it.userInput}|${it.agentResponse}" }
        )
    }

    override suspend fun store(key: String, value: String) {
        mutex.withLock {
            // ... store logic
            saveToFile()  // Persist after each change
        }
    }
    // ... rest of implementation
}
*/

/**
 * Example 3: Redis-Backed Memory
 */
/*
class RedisMemory(host: String, port: Int) : AgentMemory {
    private val redis = Jedis(host, port)
    private val mutex = Mutex()
    private val keyPrefix = "agent:"

    override suspend fun store(key: String, value: String) {
        mutex.withLock {
            when (key) {
                "user_input" -> {
                    redis.rpush("$keyPrefix:inputs", value)
                    redis.rpush("$keyPrefix:responses", "")
                }
                "agent_response" -> {
                    val count = redis.llen("$keyPrefix:responses")
                    if (count > 0) {
                        redis.lset("$keyPrefix:responses", count - 1, value)
                    }
                }
            }
        }
    }

    override suspend fun retrieve(key: String): String? {
        return mutex.withLock {
            when (key) {
                "history" -> {
                    val inputs = redis.lrange("$keyPrefix:inputs", 0, -1)
                    val responses = redis.lrange("$keyPrefix:responses", 0, -1)
                    inputs.zip(responses).joinToString("\n") { (input, response) ->
                        "User: $input\nAgent: $response"
                    }
                }
                else -> redis.get("$keyPrefix:$key")
            }
        }
    }
    // ... rest of implementation
}
*/

// ========================================
// Testing Template
// ========================================

/**
 * Test your custom memory implementation
 */
/*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking

class CustomMemoryTest {
    @Test
    fun testStoreAndRetrieve() = runBlocking {
        val memory = CustomMemoryStrategy()

        memory.store("user_input", "Hello")
        memory.store("agent_response", "Hi!")

        val history = memory.retrieve("history")
        assertEquals("User: Hello\nAgent: Hi!", history)
    }

    @Test
    fun testGetHistory() = runBlocking {
        val memory = CustomMemoryStrategy()

        memory.store("user_input", "Test")
        memory.store("agent_response", "Response")

        val exchanges = memory.getHistory()
        assertEquals(1, exchanges.size)
        assertEquals("Test", exchanges[0].userInput)
        assertEquals("Response", exchanges[0].agentResponse)
    }

    @Test
    fun testClear() = runBlocking {
        val memory = CustomMemoryStrategy()

        memory.store("user_input", "Test")
        memory.clear()

        assertEquals(0, memory.size())
    }
}
*/

// ========================================
// Tips & Best Practices
// ========================================

/**
 * 1. ALWAYS use Mutex for thread safety
 * 2. Return copies (toList()) not mutable references
 * 3. Handle special keys: "user_input", "agent_response", "history"
 * 4. Test with concurrent access
 * 5. Add error handling for I/O operations
 * 6. Consider performance implications
 * 7. Document custom behavior
 * 8. Provide clear() method
 * 9. Add metrics/logging if needed
 * 10. Test with real AgentFramework usage
 */
