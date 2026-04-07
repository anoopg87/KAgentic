## Mock Examples Reference

Comprehensive mock object examples for testing KAgentic components.

---

## Mock LLM Providers

### Example 1: Simple Mock LLM

```kotlin
val simpleMock = object : LLMProvider {
    override suspend fun generate(input: String): String {
        return "Mock response: $input"
    }
}

// Usage in test
@Test
fun testWithSimpleMock() = runBlocking {
    val response = simpleMock.generate("test")
    assertTrue(response.contains("Mock response"))
}
```

### Example 2: Conditional Mock LLM

```kotlin
val conditionalMock = object : LLMProvider {
    override suspend fun generate(input: String): String {
        return when {
            input.contains("calculate") -> "The calculation result is 42"
            input.contains("search") -> "Here are the search results"
            input.contains("error") -> throw IOException("Mock API error")
            else -> "I don't understand"
        }
    }
}

// Usage in test
@Test
fun testConditionalResponses() = runBlocking {
    assertEquals("The calculation result is 42", conditionalMock.generate("calculate 2+2"))
    assertEquals("Here are the search results", conditionalMock.generate("search kotlin"))
}
```

### Example 3: Stateful Mock LLM

```kotlin
class StatefulMockLLM : LLMProvider {
    var callCount = 0
    val inputHistory = mutableListOf<String>()
    var shouldFail = false

    override suspend fun generate(input: String): String {
        callCount++
        inputHistory.add(input)

        if (shouldFail) {
            throw IOException("Mock failure")
        }

        return "Response #$callCount: $input"
    }

    fun reset() {
        callCount = 0
        inputHistory.clear()
        shouldFail = false
    }
}

// Usage in test
@Test
fun testCallTracking() = runBlocking {
    val mock = StatefulMockLLM()
    
    mock.generate("first")
    mock.generate("second")
    
    assertEquals(2, mock.callCount)
    assertEquals(listOf("first", "second"), mock.inputHistory)
}
```

### Example 4: Delay Simulation Mock

```kotlin
import kotlinx.coroutines.delay

class SlowMockLLM(private val delayMs: Long = 100) : LLMProvider {
    override suspend fun generate(input: String): String {
        delay(delayMs)
        return "Slow response: $input"
    }
}

// Usage in test
@Test
fun testWithTimeout() = runBlocking {
    val slowMock = SlowMockLLM(delayMs = 50)
    
    withTimeout(1000) {
        val response = slowMock.generate("test")
        assertNotNull(response)
    }
}
```

### Example 5: Mock with Response Sequence

```kotlin
class SequenceMockLLM(private val responses: List<String>) : LLMProvider {
    private var currentIndex = 0

    override suspend fun generate(input: String): String {
        val response = responses[currentIndex % responses.size]
        currentIndex++
        return response
    }
}

// Usage in test
@Test
fun testSequenceResponses() = runBlocking {
    val mock = SequenceMockLLM(listOf("First", "Second", "Third"))
    
    assertEquals("First", mock.generate("any"))
    assertEquals("Second", mock.generate("any"))
    assertEquals("Third", mock.generate("any"))
    assertEquals("First", mock.generate("any"))  // Wraps around
}
```

---

## Mock Tools

### Example 1: Simple Mock Tool

```kotlin
val simpleTool = object : ToolHandler {
    override fun canHandle(input: String) = input.contains("test")
    override fun score(input: String) = if (canHandle(input)) 10 else 1
    override suspend fun handle(input: String) = "Handled: $input"
}

// Usage in test
@Test
fun testSimpleTool() = runBlocking {
    assertTrue(simpleTool.canHandle("test input"))
    assertEquals(10, simpleTool.score("test"))
    assertTrue(simpleTool.handle("test").contains("Handled"))
}
```

### Example 2: Tracking Mock Tool

```kotlin
class TrackingMockTool : ToolHandler {
    val handledInputs = mutableListOf<String>()
    var handleCount = 0

    override fun canHandle(input: String) = true

    override fun score(input: String) = 10

    override suspend fun handle(input: String): String {
        handleCount++
        handledInputs.add(input)
        return "Handled #$handleCount: $input"
    }

    fun reset() {
        handledInputs.clear()
        handleCount = 0
    }
}

// Usage in test
@Test
fun testToolTracking() = runBlocking {
    val tool = TrackingMockTool()
    
    tool.handle("first")
    tool.handle("second")
    
    assertEquals(2, tool.handleCount)
    assertEquals(listOf("first", "second"), tool.handledInputs)
}
```

### Example 3: Mock Tool with Configurable Behavior

```kotlin
class ConfigurableMockTool(
    private val keyword: String,
    private val scoreValue: Int = 10,
    private val responsePrefix: String = "Result"
) : ToolHandler {
    override fun canHandle(input: String) = input.contains(keyword, ignoreCase = true)
    
    override fun score(input: String) = if (canHandle(input)) scoreValue else 1
    
    override suspend fun handle(input: String) = "$responsePrefix: $input"
}

// Usage in test
@Test
fun testConfigurableTool() = runBlocking {
    val tool = ConfigurableMockTool(keyword = "weather", scoreValue = 8)
    
    assertTrue(tool.canHandle("weather in London"))
    assertEquals(8, tool.score("weather"))
    assertTrue(tool.handle("weather").startsWith("Result"))
}
```

### Example 4: Mock Tool with Failure Simulation

```kotlin
class FailingMockTool(private val shouldFail: Boolean = false) : ToolHandler {
    override fun canHandle(input: String) = true
    
    override fun score(input: String) = 10
    
    override suspend fun handle(input: String): String {
        if (shouldFail) {
            throw RuntimeException("Tool execution failed")
        }
        return "Success: $input"
    }
}

// Usage in test
@Test
fun testToolFailure() {
    val tool = FailingMockTool(shouldFail = true)
    
    assertFailsWith<RuntimeException> {
        runBlocking { tool.handle("test") }
    }
}
```

---

## Mock Memory Strategies

### Example 1: Simple Mock Memory

```kotlin
class SimpleMockMemory : MemoryStrategy {
    private val storage = mutableMapOf<String, String>()

    override suspend fun store(key: String, value: String) {
        storage[key] = value
    }

    override suspend fun retrieve(key: String): String {
        return storage[key] ?: ""
    }

    override suspend fun clear() {
        storage.clear()
    }
}

// Usage in test
@Test
fun testMemoryOperations() = runBlocking {
    val memory = SimpleMockMemory()
    
    memory.store("key1", "value1")
    assertEquals("value1", memory.retrieve("key1"))
    
    memory.clear()
    assertEquals("", memory.retrieve("key1"))
}
```

### Example 2: Memory with Access Tracking

```kotlin
class TrackingMockMemory : MemoryStrategy {
    private val storage = mutableMapOf<String, String>()
    val storeLog = mutableListOf<Pair<String, String>>()
    val retrieveLog = mutableListOf<String>()

    override suspend fun store(key: String, value: String) {
        storage[key] = value
        storeLog.add(key to value)
    }

    override suspend fun retrieve(key: String): String {
        retrieveLog.add(key)
        return storage[key] ?: ""
    }

    override suspend fun clear() {
        storage.clear()
    }
}

// Usage in test
@Test
fun testMemoryTracking() = runBlocking {
    val memory = TrackingMockMemory()
    
    memory.store("k1", "v1")
    memory.retrieve("k1")
    memory.retrieve("k2")
    
    assertEquals(1, memory.storeLog.size)
    assertEquals(2, memory.retrieveLog.size)
}
```

### Example 3: Memory with Capacity Limit

```kotlin
class LimitedMockMemory(private val maxSize: Int = 100) : MemoryStrategy {
    private val storage = mutableMapOf<String, String>()

    override suspend fun store(key: String, value: String) {
        if (storage.size >= maxSize) {
            // Remove oldest entry
            storage.remove(storage.keys.first())
        }
        storage[key] = value
    }

    override suspend fun retrieve(key: String) = storage[key] ?: ""

    override suspend fun clear() {
        storage.clear()
    }
}

// Usage in test
@Test
fun testMemoryLimit() = runBlocking {
    val memory = LimitedMockMemory(maxSize = 2)
    
    memory.store("k1", "v1")
    memory.store("k2", "v2")
    memory.store("k3", "v3")  // Should evict k1
    
    assertEquals("", memory.retrieve("k1"))
    assertEquals("v2", memory.retrieve("k2"))
    assertEquals("v3", memory.retrieve("k3"))
}
```

---

## Mock Agents

### Example 1: Simple Mock Agent

```kotlin
class MockAgent(private val response: String) {
    suspend fun chat(input: String): String {
        return response
    }
}

// Usage in test
@Test
fun testMockAgent() = runBlocking {
    val agent = MockAgent("Fixed response")
    assertEquals("Fixed response", agent.chat("any input"))
}
```

### Example 2: Agent with Mock Dependencies

```kotlin
fun createMockAgent(): AgentFramework {
    val mockLLM = object : LLMProvider {
        override suspend fun generate(input: String) = "LLM: $input"
    }
    
    val mockTool = object : ToolHandler {
        override fun canHandle(input: String) = true
        override fun score(input: String) = 10
        override suspend fun handle(input: String) = "Tool: $input"
    }
    
    return AgentFramework(
        llm = mockLLM,
        tools = listOf(mockTool),
        memory = ConversationMemory()
    )
}

// Usage in test
@Test
fun testAgentWithMocks() = runBlocking {
    val agent = createMockAgent()
    val response = agent.chat("test")
    assertNotNull(response)
}
```

---

## Mock HTTP Clients (for API Tools)

### Example 1: Mock HTTP Response

```kotlin
class MockHttpClient : HttpClient {
    var responseBody: String = "{}"
    var responseCode: Int = 200

    override suspend fun get(url: String): HttpResponse {
        return HttpResponse(
            code = responseCode,
            body = responseBody
        )
    }

    override suspend fun post(url: String, body: String): HttpResponse {
        return HttpResponse(
            code = responseCode,
            body = responseBody
        )
    }
}

// Usage in test
@Test
fun testAPICallWithMock() = runBlocking {
    val mockClient = MockHttpClient()
    mockClient.responseBody = """{"result": "success"}"""
    mockClient.responseCode = 200
    
    val response = mockClient.get("https://api.example.com")
    assertEquals(200, response.code)
    assertTrue(response.body.contains("success"))
}
```

---

## Mock Factories

### Example: Mock Factory Pattern

```kotlin
object MockFactory {
    fun createMockLLM(response: String = "mock") = object : LLMProvider {
        override suspend fun generate(input: String) = response
    }

    fun createMockTool(keyword: String = "test") = object : ToolHandler {
        override fun canHandle(input: String) = input.contains(keyword)
        override fun score(input: String) = if (canHandle(input)) 10 else 1
        override suspend fun handle(input: String) = "Handled: $input"
    }

    fun createMockMemory() = object : MemoryStrategy {
        private val storage = mutableMapOf<String, String>()
        override suspend fun store(key: String, value: String) { storage[key] = value }
        override suspend fun retrieve(key: String) = storage[key] ?: ""
        override suspend fun clear() { storage.clear() }
    }

    fun createMockAgent() = AgentFramework(
        llm = createMockLLM(),
        tools = listOf(createMockTool()),
        memory = createMockMemory()
    )
}

// Usage in tests
@Test
fun testWithMockFactory() = runBlocking {
    val agent = MockFactory.createMockAgent()
    val response = agent.chat("test")
    assertNotNull(response)
}
```

---

## Verification Patterns

### Pattern 1: Verify Method Called

```kotlin
class VerifiableMock : LLMProvider {
    var generateWasCalled = false
    var lastInput: String? = null

    override suspend fun generate(input: String): String {
        generateWasCalled = true
        lastInput = input
        return "response"
    }
}

@Test
fun testMethodWasCalled() = runBlocking {
    val mock = VerifiableMock()
    mock.generate("test")
    
    assertTrue(mock.generateWasCalled)
    assertEquals("test", mock.lastInput)
}
```

### Pattern 2: Verify Call Count

```kotlin
class CallCountMock : LLMProvider {
    var callCount = 0

    override suspend fun generate(input: String): String {
        callCount++
        return "response $callCount"
    }
}

@Test
fun testCallCount() = runBlocking {
    val mock = CallCountMock()
    
    mock.generate("test1")
    mock.generate("test2")
    mock.generate("test3")
    
    assertEquals(3, mock.callCount)
}
```

### Pattern 3: Verify Call Order

```kotlin
class OrderTrackingMock : ToolHandler {
    val callOrder = mutableListOf<String>()

    override fun canHandle(input: String): Boolean {
        callOrder.add("canHandle")
        return true
    }

    override fun score(input: String): Int {
        callOrder.add("score")
        return 10
    }

    override suspend fun handle(input: String): String {
        callOrder.add("handle")
        return "result"
    }
}

@Test
fun testCallOrder() = runBlocking {
    val mock = OrderTrackingMock()
    
    mock.canHandle("test")
    mock.score("test")
    mock.handle("test")
    
    assertEquals(listOf("canHandle", "score", "handle"), mock.callOrder)
}
```

---

## Advanced Mock Patterns

### Pattern 1: Mock with Realistic Behavior

```kotlin
class RealisticMockLLM : LLMProvider {
    override suspend fun generate(input: String): String {
        // Simulate processing delay
        delay(10)
        
        // Simulate token-by-token streaming (simplified)
        val response = buildString {
            append("I understand you want to ")
            when {
                input.contains("calculate") -> append("perform calculations")
                input.contains("search") -> append("search for information")
                else -> append("help you with that")
            }
        }
        
        return response
    }
}
```

### Pattern 2: Mock with Error Injection

```kotlin
class ErrorInjectingMock : LLMProvider {
    var failOnAttempt: Int? = null
    private var attemptCount = 0

    override suspend fun generate(input: String): String {
        attemptCount++
        
        if (failOnAttempt == attemptCount) {
            throw IOException("Injected failure on attempt $attemptCount")
        }
        
        return "Success on attempt $attemptCount"
    }
}

@Test
fun testRetryAfterFailure() = runBlocking {
    val mock = ErrorInjectingMock()
    mock.failOnAttempt = 1  // Fail on first attempt
    
    // First attempt fails
    assertFailsWith<IOException> {
        mock.generate("test")
    }
    
    // Second attempt succeeds
    val result = mock.generate("test")
    assertTrue(result.contains("Success on attempt 2"))
}
```

### Pattern 3: Mock with State Machine

```kotlin
class StateMachineMock : LLMProvider {
    enum class State { INITIAL, AUTHENTICATED, PROCESSING, COMPLETE }
    
    var state = State.INITIAL

    override suspend fun generate(input: String): String {
        return when (state) {
            State.INITIAL -> {
                if (input.contains("auth")) {
                    state = State.AUTHENTICATED
                    "Authentication successful"
                } else {
                    "Please authenticate first"
                }
            }
            State.AUTHENTICATED -> {
                state = State.PROCESSING
                "Processing your request"
            }
            State.PROCESSING -> {
                state = State.COMPLETE
                "Request completed"
            }
            State.COMPLETE -> {
                state = State.INITIAL
                "Ready for new request"
            }
        }
    }
}
```

---

## Testing with Multiple Mocks

### Pattern: Coordinated Mocks

```kotlin
@Test
fun testAgentWithMultipleMocks() = runBlocking {
    // Create coordinated mocks
    val mockLLM = object : LLMProvider {
        override suspend fun generate(input: String) = "Use calculator tool: 5+5"
    }

    val calculatorCallTracker = mutableListOf<String>()
    val mockCalculator = object : ToolHandler {
        override fun canHandle(input: String) = input.contains("+")
        override fun score(input: String) = 10
        override suspend fun handle(input: String): String {
            calculatorCallTracker.add(input)
            return "Result: 10"
        }
    }

    val mockMemory = SimpleMockMemory()

    // Create agent with all mocks
    val agent = AgentFramework(
        llm = mockLLM,
        tools = listOf(mockCalculator),
        memory = mockMemory
    )

    // Execute and verify
    val response = agent.chat("Calculate 5+5")
    
    assertNotNull(response)
    assertTrue(calculatorCallTracker.isNotEmpty())
}
```

---

## Best Practices for Mocks

1. **Keep mocks simple**: Only implement what's needed for the test
2. **Make behavior obvious**: Clear, predictable responses
3. **Use factories**: Create reusable mock factories
4. **Track interactions**: When verification is important
5. **Isolate tests**: Don't share mocks between tests
6. **Reset state**: Clean up between tests if reusing mocks
7. **Document behavior**: Comment complex mock logic
8. **Match real API**: Mock responses should be realistic
9. **Don't over-mock**: Use real objects when practical
10. **Test mocks**: Verify mock behavior matches reality

---

## When to Use Real vs Mock Objects

### Use Mocks:
- External APIs (LLM providers, web services)
- Database connections
- File system operations
- Network calls
- Time-dependent operations
- Expensive computations

### Use Real Objects:
- Pure functions
- Data classes
- Simple utilities
- Math operations
- String manipulation
- Collection operations

---

## Common Mock Mistakes to Avoid

❌ **Overly complex mocks**
```kotlin
// Too complex
class OverlyComplexMock : LLMProvider {
    // 200 lines of logic...
}
```

✅ **Simple, focused mocks**
```kotlin
// Simple and clear
val simple = object : LLMProvider {
    override suspend fun generate(input: String) = "response"
}
```

❌ **Shared mutable mocks**
```kotlin
// Bad: shared state between tests
val sharedMock = StatefulMock()

@Test fun test1() { sharedMock.generate("test1") }
@Test fun test2() { sharedMock.generate("test2") }  // Affected by test1!
```

✅ **Independent mocks**
```kotlin
// Good: fresh mock per test
@Test fun test1() {
    val mock = StatefulMock()
    mock.generate("test1")
}
```

❌ **Unrealistic mock behavior**
```kotlin
// Unrealistic: instant responses, perfect success
override suspend fun generate(input: String) = "perfect result"
```

✅ **Realistic mock behavior**
```kotlin
// More realistic: delays, occasional failures
override suspend fun generate(input: String): String {
    delay(10)  // Simulate network delay
    if (Random.nextInt(100) < 5) throw IOException("Network error")
    return "result"
}
```
