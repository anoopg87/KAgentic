## Test Patterns Reference

Comprehensive testing patterns observed in KAgentic's test suite.

---

## Test Structure Pattern

### Standard Test Class Structure

```kotlin
package [package_name]

import org.junit.jupiter.api.Test
import kotlin.test.*
import kotlinx.coroutines.runBlocking

class ComponentNameTest {
    // 1. Instantiation tests
    // 2. Basic functionality tests
    // 3. Edge case tests
    // 4. Error handling tests
    // 5. Integration tests
}
```

### Naming Convention

**Pattern**: `test[MethodName]_[Scenario]_[ExpectedResult]`

**Examples**:
- `testCanHandle_ValidInput_ReturnsTrue`
- `testGenerate_WithAPIKey_ReturnsResponse`
- `testStore_MultipleValues_PersistsAll`
- `testBuild_EmptyGraph_ThrowsException`

---

## Testing Tools (ToolHandler Interface)

**Source**: `/home/user/KAgentic/agentic-library/src/test/kotlin/tools/`

### Pattern 1: Basic Tool Test

```kotlin
import tools.CalculatorTool
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class CalculatorToolTest {
    @Test
    fun testCanHandleMathExpression() {
        val tool = CalculatorTool()
        assertTrue(tool.canHandle("2+2"))
    }

    @Test
    fun testHandleReturnsResult() {
        val tool = CalculatorTool()
        val result = runBlocking { tool.handle("2+2") }
        assertTrue(result.contains("Result"))
    }
}
```

### Pattern 2: Comprehensive Tool Test

```kotlin
class ToolTest {
    // Test canHandle() - positive case
    @Test
    fun testCanHandle_ValidInput_ReturnsTrue() { }

    // Test canHandle() - negative case
    @Test
    fun testCanHandle_InvalidInput_ReturnsFalse() { }

    // Test score() - high score
    @Test
    fun testScore_MatchingInput_ReturnsHighScore() { }

    // Test score() - low score
    @Test
    fun testScore_NonMatchingInput_ReturnsLowScore() { }

    // Test handle() - basic execution
    @Test
    fun testHandle_ValidInput_ReturnsFormattedResult() { }

    // Test handle() - edge cases
    @Test
    fun testHandle_EmptyInput_HandlesGracefully() { }
}
```

---

## Testing LLM Providers (LLMProvider Interface)

**Source**: `/home/user/KAgentic/agentic-library/src/test/kotlin/llm/LLMProviderTest.kt`

### Pattern 1: Instantiation Tests

```kotlin
class LLMProviderTest {
    @Test
    fun testGeminiLLMInstantiation() {
        val gemini = GeminiLLM(
            apiKey = "test-api-key",
            model = GeminiLLM.Model.GEMINI_2_5_PRO_EXPERIMENTAL
        )
        assertNotNull(gemini)
        assertTrue(gemini.apiKey == "test-api-key")
    }

    @Test
    fun testOpenAILLMInstantiation() {
        val openai = OpenAILLM(
            apiKey = "test-api-key",
            model = "gpt-4"
        )
        assertNotNull(openai)
        assertTrue(openai.apiKey == "test-api-key")
    }
}
```

### Pattern 2: Mock LLM Provider Test

```kotlin
@Test
fun testCustomLLMProvider() {
    val customLLM = object : LLMProvider {
        override suspend fun generate(input: String): String {
            return "Custom response: $input"
        }
    }
    val response = runBlocking { customLLM.generate("test") }
    assertTrue(response.contains("Custom response"))
    assertTrue(response.contains("test"))
}
```

### Pattern 3: Behavioral Mock

```kotlin
@Test
fun testLLMWithConditionalResponse() {
    val smartMock = object : LLMProvider {
        override suspend fun generate(input: String): String {
            return when {
                input.contains("calculate") -> "Calculation result"
                input.contains("search") -> "Search results"
                else -> "General response"
            }
        }
    }

    val calc = runBlocking { smartMock.generate("calculate 5+5") }
    val search = runBlocking { smartMock.generate("search kotlin") }

    assertTrue(calc.contains("Calculation"))
    assertTrue(search.contains("Search"))
}
```

---

## Testing Agent Framework

**Source**: `/home/user/KAgentic/agentic-library/src/test/kotlin/core/AgentFrameworkTest.kt`

### Pattern: Agent with Mock Dependencies

```kotlin
import core.AgentFramework
import llm.LLMProvider
import memory.ConversationMemory
import tools.CalculatorTool
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class AgentFrameworkTest {
    @Test
    fun testChatReturnsResponse() {
        val memory = ConversationMemory()
        val tools = listOf(CalculatorTool())
        val llm = object : LLMProvider {
            override suspend fun generate(input: String): String {
                return "LLM: $input"
            }
        }
        val agent = AgentFramework(
            llm = llm,
            tools = tools,
            memory = memory
        )
        val response = runBlocking { agent.chat("2+2") }
        assertTrue(response.isNotEmpty())
    }
}
```

---

## Testing Memory Strategy

**Source**: `/home/user/KAgentic/agentic-library/src/test/kotlin/memory/ConversationMemoryTest.kt`

### Pattern: Memory Operations

```kotlin
class ConversationMemoryTest {
    @Test
    fun testStoreAndRetrieve() {
        val memory = ConversationMemory()
        
        runBlocking {
            memory.store("key1", "value1")
            val result = memory.retrieve("key1")
            assertEquals("value1", result)
        }
    }

    @Test
    fun testClear() {
        val memory = ConversationMemory()
        
        runBlocking {
            memory.store("key1", "value1")
            memory.clear()
            val result = memory.retrieve("key1")
            assertTrue(result.isEmpty())
        }
    }

    @Test
    fun testMultipleStoreOperations() {
        val memory = ConversationMemory()
        
        runBlocking {
            memory.store("key1", "value1")
            memory.store("key2", "value2")
            memory.store("key3", "value3")

            assertEquals("value1", memory.retrieve("key1"))
            assertEquals("value2", memory.retrieve("key2"))
            assertEquals("value3", memory.retrieve("key3"))
        }
    }
}
```

---

## Testing Agent Graphs

**Source**: `/home/user/KAgentic/agentic-library/src/test/kotlin/graph/SimpleAgentGraphTest.kt`

### Pattern 1: SimpleAgentGraph Test

```kotlin
import graph.SimpleAgentGraphBuilder
import core.AgentFramework
import llm.LLMProvider
import memory.ConversationMemory
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class SimpleAgentGraphTest {
    @Test
    fun testGraphExecution() {
        val mockLLM = object : LLMProvider {
            override suspend fun generate(input: String) = "Processed: $input"
        }

        val agent1 = AgentFramework(mockLLM, listOf(), ConversationMemory())
        val agent2 = AgentFramework(mockLLM, listOf(), ConversationMemory())

        val graph = SimpleAgentGraphBuilder()
            .addAgent(agent1)
            .addAgent(agent2)
            .build()

        val result = runBlocking { graph.run("test") }
        assertNotNull(result)
    }
}
```

### Pattern 2: ConditionalAgentGraph Test

```kotlin
@Test
fun testConditionalRouting() {
    val routingLLM = object : LLMProvider {
        override suspend fun generate(input: String) = "route to branch B"
    }

    val agentA = AgentFramework(routingLLM, listOf(), ConversationMemory())
    val agentB = AgentFramework(routingLLM, listOf(), ConversationMemory())

    val graph = ConditionalAgentGraphBuilder()
        .addNode("A", agentA)
        .addNode("B", agentB)
        .addEdge("A", "B") { it.contains("route to branch B") }
        .build("A")

    val result = runBlocking { graph.run("start") }
    assertTrue(result.contains("branch B"))
}
```

---

## Common Assertion Patterns

### Pattern 1: Equality Assertions

```kotlin
// Exact equality
assertEquals(expected, actual)
assertEquals(10, result.score)
assertEquals("expected value", result.value)

// Negative equality
assertNotEquals(unexpected, actual)
```

### Pattern 2: Boolean Assertions

```kotlin
// True/False
assertTrue(condition)
assertFalse(condition)

// Examples
assertTrue(tool.canHandle("input"))
assertFalse(list.isEmpty())
assertTrue(result.contains("keyword"))
```

### Pattern 3: Null Assertions

```kotlin
// Not null
assertNotNull(result)
assertNotNull(agent.llm)

// Null
assertNull(optionalValue)
```

### Pattern 4: Collection Assertions

```kotlin
// Size
assertEquals(3, list.size)
assertTrue(list.isEmpty())

// Contains
assertTrue(list.contains(item))
assertTrue(set.containsAll(listOf(item1, item2)))

// Empty
assertTrue(list.isEmpty())
assertFalse(list.isEmpty())
```

### Pattern 5: String Assertions

```kotlin
// Contains substring
assertTrue(result.contains("expected"))
assertTrue(result.contains("keyword", ignoreCase = true))

// Starts/Ends with
assertTrue(result.startsWith("prefix"))
assertTrue(result.endsWith("suffix"))

// Not empty
assertTrue(result.isNotEmpty())
assertTrue(result.isNotBlank())
```

### Pattern 6: Exception Assertions

```kotlin
// Expect exception
assertFailsWith<IllegalArgumentException> {
    functionThatThrows()
}

// Expect specific exception message
val exception = assertFailsWith<IOException> {
    networkCall()
}
assertTrue(exception.message!!.contains("timeout"))
```

---

## Async Testing Patterns

### Pattern 1: Basic runBlocking

```kotlin
@Test
fun testSuspendFunction() {
    val result = runBlocking {
        mySuspendFunction()
    }
    assertNotNull(result)
}
```

### Pattern 2: Multiple Async Calls

```kotlin
@Test
fun testMultipleAsyncCalls() = runBlocking {
    val result1 = asyncFunction1()
    val result2 = asyncFunction2()
    
    assertTrue(result1.isNotEmpty())
    assertTrue(result2.isNotEmpty())
}
```

### Pattern 3: With Timeout

```kotlin
import kotlinx.coroutines.withTimeout

@Test
fun testWithTimeout() = runBlocking {
    withTimeout(1000) {  // 1 second timeout
        longRunningFunction()
    }
}
```

### Pattern 4: Testing Delays

```kotlin
import kotlinx.coroutines.delay

@Test
fun testDelay() = runBlocking {
    val start = System.currentTimeMillis()
    delay(100)
    val elapsed = System.currentTimeMillis() - start
    assertTrue(elapsed >= 100)
}
```

---

## Mock Object Patterns

### Pattern 1: Simple Mock

```kotlin
val mock = object : Interface {
    override fun method() = "mock result"
}
```

### Pattern 2: Stateful Mock

```kotlin
class MockWithState : Interface {
    var callCount = 0
    val calls = mutableListOf<String>()

    override fun method(input: String): String {
        callCount++
        calls.add(input)
        return "result"
    }
}
```

### Pattern 3: Conditional Mock

```kotlin
val conditionalMock = object : Interface {
    override fun method(input: String): String {
        return when {
            input.contains("A") -> "Response A"
            input.contains("B") -> "Response B"
            else -> "Default"
        }
    }
}
```

---

## Testing Best Practices from KAgentic

1. **Always use runBlocking for suspend functions**
2. **Create mocks for external dependencies** (LLMs, APIs, databases)
3. **Test both positive and negative cases**
4. **Use descriptive test names** (follow naming convention)
5. **Keep tests independent** (no shared mutable state)
6. **Test edge cases** (empty, null, large inputs)
7. **Use meaningful assertion messages** when needed
8. **Group related tests** in same test class
9. **One logical assertion per test** (when possible)
10. **Follow Arrange-Act-Assert** pattern

---

## Test Organization

### By Component Type

```
test/
├── tools/
│   ├── CalculatorToolTest.kt
│   ├── WebSearchToolTest.kt
│   └── CustomToolTest.kt
├── llm/
│   ├── LLMProviderTest.kt
│   └── CustomLLMTest.kt
├── core/
│   └── AgentFrameworkTest.kt
├── memory/
│   └── ConversationMemoryTest.kt
└── graph/
    ├── SimpleAgentGraphTest.kt
    └── ConditionalAgentGraphTest.kt
```

### By Test Type

- **Unit tests**: Test single component in isolation
- **Integration tests**: Test components working together
- **Contract tests**: Verify interfaces implemented correctly
- **Edge case tests**: Test boundary conditions
- **Error handling tests**: Test exception scenarios

---

## Running Tests

```bash
# All tests
./gradlew test

# Specific test class
./gradlew test --tests "CalculatorToolTest"

# Specific test method
./gradlew test --tests "CalculatorToolTest.testCanHandle_ValidInput_ReturnsTrue"

# Tests in package
./gradlew test --tests "tools.*"

# With coverage
./gradlew test jacocoTestReport

# Continuous (watch mode)
./gradlew test --continuous
```

---

## Coverage Goals

Based on KAgentic standards:
- **Minimum**: 70% code coverage
- **Target**: 80%+ code coverage
- **Public APIs**: 100% coverage
- **Critical paths**: 100% coverage
- **Edge cases**: Well covered

---

## Common Test Scenarios

1. **Instantiation**: Component can be created
2. **Basic operation**: Core functionality works
3. **Valid inputs**: Handles expected inputs correctly
4. **Invalid inputs**: Handles unexpected inputs gracefully
5. **Edge cases**: Empty, null, large inputs
6. **Error scenarios**: Exceptions, timeouts, failures
7. **State management**: State persists/updates correctly
8. **Integration**: Works with other components
9. **Performance**: Handles load appropriately
10. **Thread safety**: Concurrent access (if applicable)
