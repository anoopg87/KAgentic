---
name: add-memory-strategy
description: Create custom memory strategies for KAgentic agents by implementing the AgentMemory interface. Use when you need specialized memory behavior like persistence, caching, or advanced conversation tracking.
version: "1.0.0"
---

# Add Memory Strategy

Create custom memory implementations for KAgentic agents.

## When to Use This Skill

Trigger when you need to:
- "Create a custom memory strategy"
- "Implement persistent memory"
- "Add Redis-backed memory"
- "Create memory with size limits"
- "Implement custom memory behavior"

## Overview

Memory in KAgentic stores conversation history and state. The default `ConversationMemory` is in-memory only. Custom strategies enable:
- Persistent storage (database, file system)
- Distributed memory (Redis, Memcached)
- Advanced features (TTL, capacity limits, search)
- Specialized tracking (metrics, analytics)

This skill helps you:
1. Understand the AgentMemory interface
2. Implement custom memory strategies
3. Add thread safety with Mutex
4. Test memory implementations
5. Integrate with agents

## AgentMemory Interface

```kotlin
interface AgentMemory {
    suspend fun store(key: String, value: String)
    suspend fun retrieve(key: String): String?
    suspend fun getHistory(): List<Exchange>
}

data class Exchange(val userInput: String, val agentResponse: String)
```

**Methods:**
1. `store(key, value)` - Store a value
2. `retrieve(key)` - Retrieve a value (or null)
3. `getHistory()` - Get full conversation history

**Special Keys:**
- `"user_input"` - Current user input
- `"agent_response"` - Current agent response
- `"history"` - Full conversation formatted
- Custom keys for your use case

## Implementation Steps

### Step 1: Create Memory Class

```kotlin
package memory

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MyCustomMemory : AgentMemory {
    // Your storage implementation
    private val storage = mutableMapOf<String, String>()
    private val history = mutableListOf<Exchange>()
    private val mutex = Mutex()  // Thread safety
    
    // Implement interface methods
    override suspend fun store(key: String, value: String) {
        // Your implementation
    }
    
    override suspend fun retrieve(key: String): String? {
        // Your implementation
    }
    
    override suspend fun getHistory(): List<Exchange> {
        // Your implementation
    }
}
```

### Step 2: Implement store()

```kotlin
override suspend fun store(key: String, value: String) {
    mutex.withLock {  // Thread safety
        when (key) {
            "user_input" -> {
                // Add new exchange
                history.add(Exchange(value, ""))
                storage[key] = value
            }
            "agent_response" -> {
                // Update last exchange
                if (history.isNotEmpty()) {
                    val last = history.last()
                    history[history.lastIndex] = last.copy(agentResponse = value)
                }
                storage[key] = value
            }
            else -> {
                // Store custom key
                storage[key] = value
            }
        }
    }
}
```

### Step 3: Implement retrieve()

```kotlin
override suspend fun retrieve(key: String): String? {
    return mutex.withLock {
        when (key) {
            "history" -> {
                // Format full history
                history.joinToString("\n") { 
                    "User: ${it.userInput}\nAgent: ${it.agentResponse}" 
                }
            }
            else -> {
                // Retrieve custom key
                storage[key]
            }
        }
    }
}
```

### Step 4: Implement getHistory()

```kotlin
override suspend fun getHistory(): List<Exchange> {
    return mutex.withLock {
        history.toList()  // Return copy
    }
}
```

### Step 5: Add Custom Features

Add clear, size limits, persistence, etc.

```kotlin
suspend fun clear() {
    mutex.withLock {
        storage.clear()
        history.clear()
    }
}

suspend fun size(): Int {
    return mutex.withLock {
        history.size
    }
}
```

## Complete Examples

### Example 1: ConversationMemory (Default)

**Source**: `/home/user/KAgentic/agentic-library/src/main/kotlin/memory/AgentMemory.kt`

```kotlin
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
            }
        }
    }

    override suspend fun retrieve(key: String): String? {
        return mutex.withLock {
            when (key) {
                "history" -> history.joinToString("\n") { 
                    "User: ${it.userInput}\nAgent: ${it.agentResponse}" 
                }
                else -> null
            }
        }
    }

    override suspend fun getHistory(): List<Exchange> = 
        mutex.withLock { history.toList() }
}
```

**Features:**
- In-memory storage
- Thread-safe with Mutex
- Conversation tracking
- Simple and fast

### Example 2: Memory with Size Limit

```kotlin
package memory

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LimitedMemory(private val maxSize: Int = 100) : AgentMemory {
    private val history = mutableListOf<Exchange>()
    private val storage = mutableMapOf<String, String>()
    private val mutex = Mutex()

    override suspend fun store(key: String, value: String) {
        mutex.withLock {
            when (key) {
                "user_input" -> {
                    // Remove oldest if at capacity
                    if (history.size >= maxSize) {
                        history.removeAt(0)
                    }
                    history.add(Exchange(value, ""))
                    storage[key] = value
                }
                "agent_response" -> {
                    if (history.isNotEmpty()) {
                        val last = history.last()
                        history[history.lastIndex] = last.copy(agentResponse = value)
                    }
                    storage[key] = value
                }
                else -> {
                    storage[key] = value
                }
            }
        }
    }

    override suspend fun retrieve(key: String): String? {
        return mutex.withLock {
            when (key) {
                "history" -> history.joinToString("\n") {
                    "User: ${it.userInput}\nAgent: ${it.agentResponse}"
                }
                else -> storage[key]
            }
        }
    }

    override suspend fun getHistory(): List<Exchange> = 
        mutex.withLock { history.toList() }

    suspend fun getCurrentSize(): Int = mutex.withLock { history.size }
}
```

**Usage:**
```kotlin
val memory = LimitedMemory(maxSize = 10)  // Keep last 10 exchanges
val agent = AgentFramework(llm, tools, memory)
```

### Example 3: File-Persisted Memory

```kotlin
package memory

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class SerializableExchange(val userInput: String, val agentResponse: String)

class FilePersistedMemory(private val filePath: String) : AgentMemory {
    private val history = mutableListOf<Exchange>()
    private val storage = mutableMapOf<String, String>()
    private val mutex = Mutex()
    private val file = File(filePath)

    init {
        // Load from file if exists
        if (file.exists()) {
            val data = Json.decodeFromString<List<SerializableExchange>>(file.readText())
            history.addAll(data.map { Exchange(it.userInput, it.agentResponse) })
        }
    }

    override suspend fun store(key: String, value: String) {
        mutex.withLock {
            when (key) {
                "user_input" -> {
                    history.add(Exchange(value, ""))
                    storage[key] = value
                    persist()
                }
                "agent_response" -> {
                    if (history.isNotEmpty()) {
                        val last = history.last()
                        history[history.lastIndex] = last.copy(agentResponse = value)
                    }
                    storage[key] = value
                    persist()
                }
                else -> {
                    storage[key] = value
                }
            }
        }
    }

    override suspend fun retrieve(key: String): String? {
        return mutex.withLock {
            when (key) {
                "history" -> history.joinToString("\n") {
                    "User: ${it.userInput}\nAgent: ${it.agentResponse}"
                }
                else -> storage[key]
            }
        }
    }

    override suspend fun getHistory(): List<Exchange> = 
        mutex.withLock { history.toList() }

    private fun persist() {
        val data = history.map { SerializableExchange(it.userInput, it.agentResponse) }
        file.writeText(Json.encodeToString(data))
    }

    suspend fun clear() {
        mutex.withLock {
            history.clear()
            storage.clear()
            file.delete()
        }
    }
}
```

**Usage:**
```kotlin
val memory = FilePersistedMemory("/tmp/agent_history.json")
val agent = AgentFramework(llm, tools, memory)
// History persists across restarts
```

### Example 4: Redis-Backed Memory

```kotlin
package memory

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import redis.clients.jedis.Jedis
import kotlinx.serialization.json.Json

class RedisMemory(
    private val redisHost: String = "localhost",
    private val redisPort: Int = 6379,
    private val keyPrefix: String = "agent:"
) : AgentMemory {
    private val redis = Jedis(redisHost, redisPort)
    private val mutex = Mutex()

    override suspend fun store(key: String, value: String) {
        mutex.withLock {
            when (key) {
                "user_input" -> {
                    // Add to list
                    redis.rpush("$keyPrefix:inputs", value)
                    redis.rpush("$keyPrefix:responses", "")  // Placeholder
                }
                "agent_response" -> {
                    // Update last response
                    val count = redis.llen("$keyPrefix:responses")
                    if (count > 0) {
                        redis.lset("$keyPrefix:responses", count - 1, value)
                    }
                }
                else -> {
                    redis.set("$keyPrefix:$key", value)
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

    override suspend fun getHistory(): List<Exchange> {
        return mutex.withLock {
            val inputs = redis.lrange("$keyPrefix:inputs", 0, -1)
            val responses = redis.lrange("$keyPrefix:responses", 0, -1)
            inputs.zip(responses).map { (input, response) ->
                Exchange(input, response)
            }
        }
    }

    suspend fun clear() {
        mutex.withLock {
            redis.del("$keyPrefix:inputs", "$keyPrefix:responses")
        }
    }
}
```

**Usage:**
```kotlin
val memory = RedisMemory(redisHost = "localhost", redisPort = 6379)
val agent = AgentFramework(llm, tools, memory)
// Distributed memory across instances
```

## Thread Safety with Mutex

### Why Mutex?

AgentFramework can be called from multiple coroutines. Memory must be thread-safe.

### Pattern

```kotlin
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MyMemory : AgentMemory {
    private val mutex = Mutex()
    private val data = mutableMapOf<String, String>()

    override suspend fun store(key: String, value: String) {
        mutex.withLock {  // Acquire lock
            data[key] = value
        }  // Release lock
    }

    override suspend fun retrieve(key: String): String? {
        return mutex.withLock {
            data[key]
        }
    }
}
```

**Benefits:**
- Prevents race conditions
- Safe concurrent access
- Coroutine-friendly (suspend functions)

### Without Mutex (UNSAFE)

```kotlin
// ❌ BAD: Not thread-safe
class UnsafeMemory : AgentMemory {
    private val data = mutableMapOf<String, String>()

    override suspend fun store(key: String, value: String) {
        data[key] = value  // Race condition!
    }

    override suspend fun retrieve(key: String) = data[key]
}
```

**Problems:**
- Concurrent modifications
- Data corruption
- Lost updates

## Testing Memory Strategies

### Basic Test

```kotlin
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking

class MyMemoryTest {
    @Test
    fun testStoreAndRetrieve() = runBlocking {
        val memory = MyCustomMemory()
        
        memory.store("user_input", "Hello")
        memory.store("agent_response", "Hi!")
        
        val history = memory.retrieve("history")
        assertEquals("User: Hello\nAgent: Hi!", history)
    }

    @Test
    fun testGetHistory() = runBlocking {
        val memory = MyCustomMemory()
        
        memory.store("user_input", "Test")
        memory.store("agent_response", "Response")
        
        val exchanges = memory.getHistory()
        assertEquals(1, exchanges.size)
        assertEquals("Test", exchanges[0].userInput)
        assertEquals("Response", exchanges[0].agentResponse)
    }
}
```

### Test Thread Safety

```kotlin
import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Test
fun testConcurrentAccess() = runBlocking {
    val memory = MyCustomMemory()
    
    // Launch 100 concurrent stores
    val jobs = List(100) { i ->
        launch {
            memory.store("key_$i", "value_$i")
        }
    }
    
    jobs.forEach { it.join() }
    
    // Verify all stored
    repeat(100) { i ->
        assertEquals("value_$i", memory.retrieve("key_$i"))
    }
}
```

## Best Practices

### ✅ DO

1. **Use Mutex for thread safety**
```kotlin
private val mutex = Mutex()
override suspend fun store(key: String, value: String) {
    mutex.withLock { /* ... */ }
}
```

2. **Return copies, not references**
```kotlin
override suspend fun getHistory() = mutex.withLock {
    history.toList()  // Copy, not reference
}
```

3. **Handle special keys**
```kotlin
when (key) {
    "user_input" -> // Handle
    "agent_response" -> // Handle
    "history" -> // Format
    else -> // Custom keys
}
```

4. **Add clear() method**
```kotlin
suspend fun clear() {
    mutex.withLock {
        storage.clear()
        history.clear()
    }
}
```

5. **Test with real usage**
```kotlin
@Test
fun testWithAgent() = runBlocking {
    val memory = MyMemory()
    val agent = AgentFramework(mockLLM, listOf(), memory)
    agent.chat("Test")
    assertEquals(1, memory.getHistory().size)
}
```

### ❌ DON'T

1. **Don't forget thread safety**
```kotlin
// Bad
override suspend fun store(key: String, value: String) {
    data[key] = value  // No mutex!
}
```

2. **Don't return mutable references**
```kotlin
// Bad
override suspend fun getHistory() = history  // Mutable!

// Good
override suspend fun getHistory() = history.toList()  // Copy
```

3. **Don't block in suspend functions**
```kotlin
// Bad
override suspend fun store(key: String, value: String) {
    Thread.sleep(1000)  // Blocking!
}

// Good
override suspend fun store(key: String, value: String) {
    delay(1000)  // Non-blocking
}
```

4. **Don't ignore errors**
```kotlin
// Bad
override suspend fun store(key: String, value: String) {
    try {
        database.save(key, value)
    } catch (e: Exception) {
        // Silent failure
    }
}

// Good
override suspend fun store(key: String, value: String) {
    try {
        database.save(key, value)
    } catch (e: Exception) {
        logger.error("Failed to store $key", e)
        throw e
    }
}
```

## Advanced Features

### Feature 1: TTL (Time-To-Live)

```kotlin
class TTLMemory(private val ttlMs: Long = 3600000) : AgentMemory {
    private data class Entry(val value: String, val timestamp: Long)
    private val storage = mutableMapOf<String, Entry>()
    private val mutex = Mutex()

    override suspend fun store(key: String, value: String) {
        mutex.withLock {
            storage[key] = Entry(value, System.currentTimeMillis())
        }
    }

    override suspend fun retrieve(key: String): String? {
        return mutex.withLock {
            val entry = storage[key] ?: return@withLock null
            if (System.currentTimeMillis() - entry.timestamp > ttlMs) {
                storage.remove(key)
                null
            } else {
                entry.value
            }
        }
    }
}
```

### Feature 2: Metrics Tracking

```kotlin
class MetricsMemory : AgentMemory {
    private val history = mutableListOf<Exchange>()
    private val mutex = Mutex()
    private var storeCount = 0
    private var retrieveCount = 0

    suspend fun getMetrics(): Map<String, Int> {
        return mutex.withLock {
            mapOf(
                "stores" to storeCount,
                "retrieves" to retrieveCount,
                "history_size" to history.size
            )
        }
    }

    override suspend fun store(key: String, value: String) {
        mutex.withLock {
            storeCount++
            // ... rest of implementation
        }
    }

    override suspend fun retrieve(key: String): String? {
        return mutex.withLock {
            retrieveCount++
            // ... rest of implementation
        }
    }
}
```

### Feature 3: Search Capability

```kotlin
class SearchableMemory : AgentMemory {
    private val history = mutableListOf<Exchange>()
    private val mutex = Mutex()

    suspend fun search(query: String): List<Exchange> {
        return mutex.withLock {
            history.filter {
                it.userInput.contains(query, ignoreCase = true) ||
                it.agentResponse.contains(query, ignoreCase = true)
            }
        }
    }

    // ... implement interface methods
}
```

## Integration with AgentFramework

```kotlin
val customMemory = MyCustomMemory()

val agent = AgentFramework(
    llm = OpenAILLM(apiKey),
    tools = listOf(CalculatorTool()),
    memory = customMemory  // Use custom memory
)

val response = agent.chat("Hello")
val history = customMemory.getHistory()
```

## Related Skills

- **add-test-with-mock**: Test your memory implementation
- **understand-agent-flow**: See how memory integrates with agents
- **add-custom-tool**: Tools can access memory

## Reference Files

- AgentMemory Interface: `/home/user/KAgentic/agentic-library/src/main/kotlin/memory/AgentMemory.kt`
- ConversationMemory: Same file (default implementation)
- Memory Template: `.claude/skills/add-memory-strategy/templates/memory-template.kt`
- Memory Patterns: `.claude/skills/add-memory-strategy/references/memory-patterns.md`

## Summary

**Key Points:**
1. Implement `AgentMemory` interface (3 methods)
2. Use `Mutex` for thread safety
3. Handle special keys: "user_input", "agent_response", "history"
4. Return copies, not mutable references
5. Test with concurrent access
6. Add custom features as needed (persistence, TTL, etc.)

**Common Use Cases:**
- Persistent storage (file, database)
- Distributed memory (Redis)
- Size limits (sliding window)
- TTL expiration
- Metrics and analytics
- Search and filtering
