## Memory Strategy Patterns Reference

Common patterns and implementations for custom memory strategies in KAgentic.

**Source**: `/home/user/KAgentic/agentic-library/src/main/kotlin/memory/AgentMemory.kt`

---

## Interface Definition

```kotlin
package memory

data class Exchange(val userInput: String, val agentResponse: String)

interface AgentMemory {
    suspend fun store(key: String, value: String)
    suspend fun retrieve(key: String): String?
    suspend fun getHistory(): List<Exchange>
}
```

**Key Methods:**
- `store(key, value)` - Store a key-value pair
- `retrieve(key)` - Retrieve a value by key
- `getHistory()` - Get full conversation history

**Special Keys:**
- `"user_input"` - Triggers new Exchange creation
- `"agent_response"` - Updates last Exchange
- `"history"` - Returns formatted conversation
- Custom keys - Application-specific storage

---

## Pattern 1: Basic In-Memory Storage (Default)

### ConversationMemory Implementation

```kotlin
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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

**Characteristics:**
- ✅ Simple and fast
- ✅ Thread-safe with Mutex
- ✅ No dependencies
- ❌ Not persistent
- ❌ No size limits
- ❌ Lost on restart

**Use When:**
- Simple applications
- Short-lived sessions
- Development/testing
- No persistence needed

---

## Pattern 2: Size-Limited Memory

### Sliding Window Pattern

```kotlin
class LimitedMemory(private val maxSize: Int = 100) : AgentMemory {
    private val history = mutableListOf<Exchange>()
    private val storage = mutableMapOf<String, String>()
    private val mutex = Mutex()

    override suspend fun store(key: String, value: String) {
        mutex.withLock {
            when (key) {
                "user_input" -> {
                    // Evict oldest if at capacity
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
    suspend fun getRemainingCapacity(): Int = mutex.withLock { maxSize - history.size }
}
```

**Characteristics:**
- ✅ Bounded memory usage
- ✅ Automatic eviction
- ✅ Predictable performance
- ❌ Loses old conversations
- ❌ Still in-memory only

**Use When:**
- Long-running agents
- Memory-constrained environments
- Need to prevent unbounded growth
- Recent history more important

**Variations:**
- FIFO (First In First Out) - Remove oldest
- LRU (Least Recently Used) - Remove least accessed
- TTL (Time To Live) - Remove expired

---

## Pattern 3: File-Persisted Memory

### JSON File Storage

```kotlin
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
        // Load from file on initialization
        if (file.exists()) {
            try {
                val data = Json.decodeFromString<List<SerializableExchange>>(file.readText())
                history.addAll(data.map { Exchange(it.userInput, it.agentResponse) })
            } catch (e: Exception) {
                println("Warning: Failed to load history from $filePath: ${e.message}")
            }
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
        try {
            val data = history.map { SerializableExchange(it.userInput, it.agentResponse) }
            file.writeText(Json.encodeToString(data))
        } catch (e: Exception) {
            println("Error: Failed to persist history: ${e.message}")
        }
    }

    suspend fun clear() {
        mutex.withLock {
            history.clear()
            storage.clear()
            if (file.exists()) {
                file.delete()
            }
        }
    }
}
```

**Characteristics:**
- ✅ Survives restarts
- ✅ Easy to inspect/export
- ✅ No external dependencies
- ⚠️ I/O on every write
- ❌ Not suitable for high-frequency writes
- ❌ Single-process only

**Use When:**
- Need persistence
- Low to moderate write frequency
- Single-instance deployment
- Human-readable format desired

**Optimizations:**
- Batch writes
- Async persistence
- Append-only log format
- Compression

---

## Pattern 4: Database-Backed Memory

### SQLite Implementation

```kotlin
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

class DatabaseMemory(private val dbPath: String) : AgentMemory {
    private val mutex = Mutex()
    private val connection: Connection = DriverManager.getConnection("jdbc:sqlite:$dbPath")

    init {
        // Create tables if not exist
        connection.createStatement().execute("""
            CREATE TABLE IF NOT EXISTS exchanges (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_input TEXT NOT NULL,
                agent_response TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
        """)
        connection.createStatement().execute("""
            CREATE TABLE IF NOT EXISTS storage (
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL
            )
        """)
    }

    override suspend fun store(key: String, value: String) {
        mutex.withLock {
            when (key) {
                "user_input" -> {
                    val stmt = connection.prepareStatement(
                        "INSERT INTO exchanges (user_input, agent_response, timestamp) VALUES (?, '', ?)"
                    )
                    stmt.setString(1, value)
                    stmt.setLong(2, System.currentTimeMillis())
                    stmt.executeUpdate()
                }
                "agent_response" -> {
                    val stmt = connection.prepareStatement(
                        "UPDATE exchanges SET agent_response = ? WHERE id = (SELECT MAX(id) FROM exchanges)"
                    )
                    stmt.setString(1, value)
                    stmt.executeUpdate()
                }
                else -> {
                    val stmt = connection.prepareStatement(
                        "INSERT OR REPLACE INTO storage (key, value) VALUES (?, ?)"
                    )
                    stmt.setString(1, key)
                    stmt.setString(2, value)
                    stmt.executeUpdate()
                }
            }
        }
    }

    override suspend fun retrieve(key: String): String? {
        return mutex.withLock {
            when (key) {
                "history" -> {
                    val stmt = connection.prepareStatement("SELECT user_input, agent_response FROM exchanges ORDER BY id")
                    val rs: ResultSet = stmt.executeQuery()
                    buildString {
                        while (rs.next()) {
                            appendLine("User: ${rs.getString("user_input")}")
                            appendLine("Agent: ${rs.getString("agent_response")}")
                        }
                    }
                }
                else -> {
                    val stmt = connection.prepareStatement("SELECT value FROM storage WHERE key = ?")
                    stmt.setString(1, key)
                    val rs = stmt.executeQuery()
                    if (rs.next()) rs.getString("value") else null
                }
            }
        }
    }

    override suspend fun getHistory(): List<Exchange> {
        return mutex.withLock {
            val stmt = connection.prepareStatement("SELECT user_input, agent_response FROM exchanges ORDER BY id")
            val rs = stmt.executeQuery()
            buildList {
                while (rs.next()) {
                    add(Exchange(rs.getString("user_input"), rs.getString("agent_response")))
                }
            }
        }
    }

    suspend fun clear() {
        mutex.withLock {
            connection.createStatement().execute("DELETE FROM exchanges")
            connection.createStatement().execute("DELETE FROM storage")
        }
    }

    fun close() {
        connection.close()
    }
}
```

**Characteristics:**
- ✅ Durable persistence
- ✅ Query capabilities
- ✅ Handles large datasets
- ✅ Transaction support
- ⚠️ More complex
- ⚠️ Requires DB setup

**Use When:**
- Production applications
- Large conversation histories
- Need to query history
- Multi-agent systems
- Analytics/reporting needed

---

## Pattern 5: Redis-Backed Memory (Distributed)

### Redis Implementation

```kotlin
import redis.clients.jedis.Jedis
import kotlinx.serialization.json.Json

class RedisMemory(
    private val host: String = "localhost",
    private val port: Int = 6379,
    private val keyPrefix: String = "agent:"
) : AgentMemory {
    private val redis = Jedis(host, port)
    private val mutex = Mutex()

    override suspend fun store(key: String, value: String) {
        mutex.withLock {
            when (key) {
                "user_input" -> {
                    redis.rpush("$keyPrefix:inputs", value)
                    redis.rpush("$keyPrefix:responses", "")  // Placeholder
                }
                "agent_response" -> {
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

    fun close() {
        redis.close()
    }
}
```

**Characteristics:**
- ✅ Distributed/shared state
- ✅ Fast performance
- ✅ Scalable
- ✅ TTL support
- ⚠️ External dependency
- ⚠️ Network overhead

**Use When:**
- Multi-instance deployment
- Need shared memory across servers
- High-performance requirements
- Microservices architecture
- Session sharing needed

---

## Pattern 6: TTL (Time-To-Live) Memory

### Automatic Expiration

```kotlin
import java.time.Instant

class TTLMemory(private val ttlSeconds: Long = 3600) : AgentMemory {
    private data class Entry(val value: String, val timestamp: Long)
    private val storage = mutableMapOf<String, Entry>()
    private val history = mutableListOf<Pair<Exchange, Long>>()
    private val mutex = Mutex()

    override suspend fun store(key: String, value: String) {
        mutex.withLock {
            val now = Instant.now().epochSecond
            when (key) {
                "user_input" -> {
                    history.add(Exchange(value, "") to now)
                    storage[key] = Entry(value, now)
                }
                "agent_response" -> {
                    if (history.isNotEmpty()) {
                        val (exchange, timestamp) = history.last()
                        history[history.lastIndex] = exchange.copy(agentResponse = value) to timestamp
                    }
                    storage[key] = Entry(value, now)
                }
                else -> {
                    storage[key] = Entry(value, now)
                }
            }
            cleanup()
        }
    }

    override suspend fun retrieve(key: String): String? {
        return mutex.withLock {
            cleanup()
            when (key) {
                "history" -> {
                    history.joinToString("\n") { (exchange, _) ->
                        "User: ${exchange.userInput}\nAgent: ${exchange.agentResponse}"
                    }
                }
                else -> storage[key]?.value
            }
        }
    }

    override suspend fun getHistory(): List<Exchange> {
        return mutex.withLock {
            cleanup()
            history.map { it.first }
        }
    }

    private fun cleanup() {
        val now = Instant.now().epochSecond
        val cutoff = now - ttlSeconds

        // Remove expired entries
        storage.entries.removeIf { (_, entry) -> entry.timestamp < cutoff }
        history.removeIf { (_, timestamp) -> timestamp < cutoff }
    }

    suspend fun clear() {
        mutex.withLock {
            storage.clear()
            history.clear()
        }
    }
}
```

**Characteristics:**
- ✅ Automatic cleanup
- ✅ Memory efficient
- ✅ Privacy-friendly (auto-delete)
- ⚠️ Data loss by design
- ⚠️ Cleanup overhead

**Use When:**
- Privacy requirements
- Temporary conversations
- Memory-constrained
- Need automatic expiration
- GDPR compliance

---

## Pattern 7: Metrics & Analytics Memory

### Tracking Usage

```kotlin
class MetricsMemory : AgentMemory {
    private val history = mutableListOf<Exchange>()
    private val storage = mutableMapOf<String, String>()
    private val mutex = Mutex()

    // Metrics
    private var storeCount = 0
    private var retrieveCount = 0
    private var totalInputChars = 0
    private var totalResponseChars = 0

    override suspend fun store(key: String, value: String) {
        mutex.withLock {
            storeCount++
            when (key) {
                "user_input" -> {
                    history.add(Exchange(value, ""))
                    storage[key] = value
                    totalInputChars += value.length
                }
                "agent_response" -> {
                    if (history.isNotEmpty()) {
                        val last = history.last()
                        history[history.lastIndex] = last.copy(agentResponse = value)
                    }
                    storage[key] = value
                    totalResponseChars += value.length
                }
                else -> {
                    storage[key] = value
                }
            }
        }
    }

    override suspend fun retrieve(key: String): String? {
        return mutex.withLock {
            retrieveCount++
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

    suspend fun getMetrics(): Map<String, Any> {
        return mutex.withLock {
            mapOf(
                "total_exchanges" to history.size,
                "store_operations" to storeCount,
                "retrieve_operations" to retrieveCount,
                "total_input_chars" to totalInputChars,
                "total_response_chars" to totalResponseChars,
                "avg_input_length" to if (history.isNotEmpty()) totalInputChars / history.size else 0,
                "avg_response_length" to if (history.isNotEmpty()) totalResponseChars / history.size else 0
            )
        }
    }
}
```

**Use When:**
- Analytics required
- Performance monitoring
- Usage tracking
- Billing/quota management
- Debugging

---

## Pattern 8: Searchable Memory

### Full-Text Search

```kotlin
class SearchableMemory : AgentMemory {
    private val history = mutableListOf<Exchange>()
    private val storage = mutableMapOf<String, String>()
    private val mutex = Mutex()

    // Implement interface methods...
    // (Similar to ConversationMemory)

    suspend fun search(query: String, caseSensitive: Boolean = false): List<Exchange> {
        return mutex.withLock {
            history.filter { exchange ->
                val input = if (caseSensitive) exchange.userInput else exchange.userInput.lowercase()
                val response = if (caseSensitive) exchange.agentResponse else exchange.agentResponse.lowercase()
                val searchQuery = if (caseSensitive) query else query.lowercase()

                input.contains(searchQuery) || response.contains(searchQuery)
            }
        }
    }

    suspend fun searchByDateRange(fromTimestamp: Long, toTimestamp: Long): List<Exchange> {
        // Would need to store timestamps with exchanges
        // Implementation depends on your needs
        TODO("Implement date range filtering")
    }

    suspend fun getStatistics(): Map<String, Any> {
        return mutex.withLock {
            mapOf(
                "total_exchanges" to history.size,
                "unique_words" to history.flatMap {
                    (it.userInput + " " + it.agentResponse).split("\\s+".toRegex())
                }.toSet().size
            )
        }
    }
}
```

**Use When:**
- Need to query history
- Implement chat search
- Build analytics dashboards
- Support user features (search conversations)

---

## Thread Safety Patterns

### Pattern A: Mutex with withLock

**Recommended Approach**

```kotlin
private val mutex = Mutex()

override suspend fun store(key: String, value: String) {
    mutex.withLock {
        // All modifications here
        data[key] = value
    }  // Automatically released
}
```

**Benefits:**
- Automatic lock release
- Exception-safe
- Coroutine-friendly
- Clear critical sections

### Pattern B: Without Mutex (UNSAFE)

**Don't Do This**

```kotlin
// ❌ NOT THREAD-SAFE
override suspend fun store(key: String, value: String) {
    data[key] = value  // Race condition!
}
```

**Problems:**
- Lost updates
- Data corruption
- Non-deterministic behavior
- Rare but serious bugs

---

## Common Mistakes

### Mistake 1: Returning Mutable References

```kotlin
// ❌ BAD: Caller can modify internal state
override suspend fun getHistory() = history

// ✅ GOOD: Return immutable copy
override suspend fun getHistory() = history.toList()
```

### Mistake 2: Forgetting Thread Safety

```kotlin
// ❌ BAD: No mutex
private val data = mutableMapOf<String, String>()
override suspend fun store(k: String, v: String) { data[k] = v }

// ✅ GOOD: With mutex
private val mutex = Mutex()
override suspend fun store(k: String, v: String) {
    mutex.withLock { data[k] = v }
}
```

### Mistake 3: Blocking I/O in Suspend Functions

```kotlin
// ❌ BAD: Blocks thread
override suspend fun store(k: String, v: String) {
    Thread.sleep(100)  // Blocking!
    file.writeText(v)
}

// ✅ GOOD: Non-blocking
override suspend fun store(k: String, v: String) {
    delay(100)  // Non-blocking
    withContext(Dispatchers.IO) {
        file.writeText(v)
    }
}
```

---

## Performance Comparison

| Pattern | Read Speed | Write Speed | Memory Usage | Persistence | Scalability |
|---------|-----------|-------------|--------------|-------------|-------------|
| In-Memory | ⚡ Very Fast | ⚡ Very Fast | ⚠️ High | ❌ No | ⚠️ Single Instance |
| Size-Limited | ⚡ Very Fast | ⚡ Very Fast | ✅ Bounded | ❌ No | ⚠️ Single Instance |
| File | ✅ Fast | ⚠️ Slow | ✅ Low | ✅ Yes | ⚠️ Single Instance |
| Database | ✅ Fast | ✅ Fast | ✅ Low | ✅ Yes | ✅ Good |
| Redis | ⚡ Very Fast | ⚡ Very Fast | ✅ External | ✅ Yes | ✅ Excellent |
| TTL | ✅ Fast | ✅ Fast | ✅ Bounded | ❌ Temporary | ⚠️ Single Instance |

---

## Summary

**Choose Your Pattern Based On:**

1. **Simple, stateless**: ConversationMemory (in-memory)
2. **Memory-constrained**: LimitedMemory (size-limited)
3. **Needs persistence**: FilePersistedMemory or DatabaseMemory
4. **Distributed/scalable**: RedisMemory
5. **Privacy requirements**: TTLMemory
6. **Analytics needs**: MetricsMemory
7. **Search features**: SearchableMemory

**Always Remember:**
- ✅ Use Mutex for thread safety
- ✅ Return copies, not references
- ✅ Handle special keys properly
- ✅ Test with concurrent access
- ✅ Consider performance implications
- ✅ Add error handling for I/O
- ✅ Document custom behavior
