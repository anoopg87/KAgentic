---
name: add-test-with-mock
description: Write comprehensive tests for KAgentic components using JUnit 5, Kotlin Test, and mock objects. Use when adding test coverage for tools, LLM providers, agents, or any custom components.
version: "1.0.0"
---

# Add Test with Mock

Write effective tests for the KAgentic framework using JUnit 5, Kotlin Test, and mock objects.

## When to Use This Skill

Trigger when you need to:
- "Write tests for my custom tool"
- "Add test coverage for the new LLM provider"
- "Test my agent implementation"
- "Create unit tests with mocks"
- "How do I test asynchronous code?"

## Overview

KAgentic uses:
- **JUnit 5** for test framework
- **Kotlin Test** for assertions (`kotlin.test.*`)
- **kotlinx.coroutines.runBlocking** for testing suspend functions
- **Mock objects** (object expressions) for LLMs, tools, and external dependencies

This skill helps you:
1. Write unit tests following project conventions
2. Create mock objects for dependencies
3. Test asynchronous/coroutine code
4. Achieve good test coverage
5. Follow best practices from existing tests

## Test Structure

### Basic Test Pattern

```kotlin
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking

class MyComponentTest {
    @Test
    fun testMethodName_Scenario_ExpectedResult() {
        // Arrange: Set up test data and mocks
        val input = "test input"
        val expected = "expected output"
        
        // Act: Execute the code under test
        val result = myComponent.process(input)
        
        // Assert: Verify the results
        assertEquals(expected, result)
    }
}
```

## Testing Different Components

### 1. Testing Tools

Tools implement `ToolHandler` interface with 3 methods: `canHandle()`, `score()`, `handle()`.

**Example**: Testing Calculator Tool

```kotlin
import tools.CalculatorTool
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class CalculatorToolTest {
    @Test
    fun testCanHandleMathExpression() {
        val tool = CalculatorTool()
        assertTrue(tool.canHandle("2+2"))
    }

    @Test
    fun testCannotHandleNonMathInput() {
        val tool = CalculatorTool()
        assertTrue(!tool.canHandle("hello world"))
    }

    @Test
    fun testScoreReturnsTenForMath() {
        val tool = CalculatorTool()
        val score = tool.score("5 * 3")
        assertEquals(10, score)
    }

    @Test
    fun testHandleReturnsResult() {
        val tool = CalculatorTool()
        val result = runBlocking { tool.handle("2+2") }
        assertTrue(result.contains("Result"))
    }

    @Test
    fun testHandleComplexExpression() {
        val tool = CalculatorTool()
        val result = runBlocking { tool.handle("(10 + 5) * 2") }
        assertTrue(result.contains("30"))
    }
}
```

**Key Points:**
- Test `canHandle()` with both valid and invalid inputs
- Test `score()` returns correct priority
- Test `handle()` with `runBlocking` for suspend functions
- Verify output format/content

### 2. Testing LLM Providers

LLM providers implement `LLMProvider` interface with `generate()` method.

**Pattern 1: Instantiation Tests**

```kotlin
import llm.OpenAILLM
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MyLLMProviderTest {
    @Test
    fun testInstantiation() {
        val llm = OpenAILLM(
            apiKey = "test-api-key",
            model = "gpt-4"
        )
        assertNotNull(llm)
        assertTrue(llm.apiKey == "test-api-key")
    }
}
```

**Pattern 2: Mock LLM Provider**

```kotlin
import llm.LLMProvider
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class MyComponentTest {
    @Test
    fun testWithMockLLM() {
        // Create mock LLM
        val mockLLM = object : LLMProvider {
            override suspend fun generate(input: String): String {
                return "Mock response: $input"
            }
        }
        
        // Use mock in test
        val response = runBlocking { mockLLM.generate("test") }
        assertTrue(response.contains("Mock response"))
        assertTrue(response.contains("test"))
    }
}
```

**Pattern 3: Testing Error Handling**

```kotlin
@Test
fun testHandlesAPIError() {
    val mockLLM = object : LLMProvider {
        override suspend fun generate(input: String): String {
            throw IOException("API error")
        }
    }
    
    assertFailsWith<IOException> {
        runBlocking { mockLLM.generate("test") }
    }
}
```

### 3. Testing Agent Framework

Agents orchestrate LLMs, tools, and memory.

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
        // Mock LLM
        val mockLLM = object : LLMProvider {
            override suspend fun generate(input: String): String {
                return "LLM: $input"
            }
        }
        
        // Create agent
        val agent = AgentFramework(
            llm = mockLLM,
            tools = listOf(CalculatorTool()),
            memory = ConversationMemory()
        )
        
        // Test
        val response = runBlocking { agent.chat("2+2") }
        assertTrue(response.isNotEmpty())
    }

    @Test
    fun testAgentUsesTools() {
        val mockLLM = object : LLMProvider {
            override suspend fun generate(input: String) = "Calculate: 5+5"
        }
        
        val tool = CalculatorTool()
        val agent = AgentFramework(
            llm = mockLLM,
            tools = listOf(tool),
            memory = ConversationMemory()
        )
        
        val response = runBlocking { agent.chat("What is 5+5?") }
        assertNotNull(response)
    }
}
```

### 4. Testing Memory

Memory stores and retrieves conversation history.

```kotlin
import memory.ConversationMemory
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
    fun testClearMemory() {
        val memory = ConversationMemory()
        
        runBlocking {
            memory.store("key1", "value1")
            memory.clear()
            val result = memory.retrieve("key1")
            
            assertTrue(result.isEmpty() || result == "")
        }
    }
}
```

### 5. Testing Agent Graphs

Test both SimpleAgentGraph and ConditionalAgentGraph.

```kotlin
import graph.SimpleAgentGraphBuilder
import graph.ConditionalAgentGraphBuilder
import core.AgentFramework
import llm.LLMProvider
import memory.ConversationMemory
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AgentGraphTest {
    @Test
    fun testSimpleGraphExecution() {
        val mockLLM = object : LLMProvider {
            override suspend fun generate(input: String) = "Mock: $input"
        }
        
        val agent1 = AgentFramework(mockLLM, listOf(), ConversationMemory())
        val agent2 = AgentFramework(mockLLM, listOf(), ConversationMemory())
        
        val graph = SimpleAgentGraphBuilder()
            .addAgent(agent1)
            .addAgent(agent2)
            .build()
        
        val result = runBlocking { graph.run("test input") }
        assertNotNull(result)
        assertTrue(result.contains("Mock"))
    }

    @Test
    fun testConditionalGraphRouting() {
        val mockLLM = object : LLMProvider {
            override suspend fun generate(input: String) = "route to B"
        }
        
        val agentA = AgentFramework(mockLLM, listOf(), ConversationMemory())
        val agentB = AgentFramework(mockLLM, listOf(), ConversationMemory())
        
        val graph = ConditionalAgentGraphBuilder()
            .addNode("A", agentA)
            .addNode("B", agentB)
            .addEdge("A", "B") { it.contains("route to B") }
            .build("A")
        
        val result = runBlocking { graph.run("test") }
        assertTrue(result.contains("route to B"))
    }
}
```

## Complete Test Example

### Custom Tool Test (Weather Tool)

```kotlin
package tools

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class WeatherToolTest {
    
    @Test
    fun testCanHandleWeatherQuery() {
        val tool = WeatherTool()
        assertTrue(tool.canHandle("weather in London"))
        assertTrue(tool.canHandle("What's the weather like?"))
    }

    @Test
    fun testCannotHandleNonWeatherQuery() {
        val tool = WeatherTool()
        assertFalse(tool.canHandle("calculate 2+2"))
        assertFalse(tool.canHandle("search for Kotlin"))
    }

    @Test
    fun testScoreForWeatherQuery() {
        val tool = WeatherTool()
        val score = tool.score("weather forecast")
        assertEquals(10, score)
    }

    @Test
    fun testScoreForNonWeatherQuery() {
        val tool = WeatherTool()
        val score = tool.score("hello world")
        assertEquals(1, score)
    }

    @Test
    fun testHandleReturnsWeatherData() {
        val tool = WeatherTool()
        val result = runBlocking { 
            tool.handle("weather in San Francisco") 
        }
        assertTrue(result.contains("Weather"))
        assertTrue(result.contains("San Francisco"))
    }

    @Test
    fun testHandleWithInvalidLocation() {
        val tool = WeatherTool()
        val result = runBlocking { 
            tool.handle("weather in InvalidCity123") 
        }
        assertTrue(result.contains("error") || result.contains("not found"))
    }

    @Test
    fun testHandleAsyncExecution() {
        val tool = WeatherTool()
        
        // Test that it works with coroutines
        runBlocking {
            val result1 = tool.handle("weather in Tokyo")
            val result2 = tool.handle("weather in Paris")
            
            assertTrue(result1.contains("Tokyo"))
            assertTrue(result2.contains("Paris"))
        }
    }
}
```

## Mock Objects Pattern Library

### Mock LLM Provider

```kotlin
// Simple mock
val mockLLM = object : LLMProvider {
    override suspend fun generate(input: String) = "Mock: $input"
}

// Mock that simulates specific behavior
val smartMockLLM = object : LLMProvider {
    override suspend fun generate(input: String): String {
        return when {
            input.contains("calculate") -> "Result: 42"
            input.contains("search") -> "Found: information"
            else -> "Default response"
        }
    }
}

// Mock that counts calls
class CountingMockLLM : LLMProvider {
    var callCount = 0
    
    override suspend fun generate(input: String): String {
        callCount++
        return "Response $callCount"
    }
}
```

### Mock Tool

```kotlin
// Simple mock tool
val mockTool = object : ToolHandler {
    override fun canHandle(input: String) = input.contains("test")
    override fun score(input: String) = if (canHandle(input)) 10 else 1
    override suspend fun handle(input: String) = "Mock tool: $input"
}

// Mock tool that tracks invocations
class TrackingMockTool : ToolHandler {
    val invocations = mutableListOf<String>()
    
    override fun canHandle(input: String) = true
    override fun score(input: String) = 10
    override suspend fun handle(input: String): String {
        invocations.add(input)
        return "Handled: $input"
    }
}
```

## Testing Async Code

### Pattern 1: runBlocking

```kotlin
@Test
fun testAsyncOperation() {
    val result = runBlocking {
        myAsyncFunction()
    }
    assertEquals(expected, result)
}
```

### Pattern 2: Multiple async calls

```kotlin
@Test
fun testMultipleAsyncCalls() = runBlocking {
    val result1 = asyncFunction1()
    val result2 = asyncFunction2()
    
    assertTrue(result1.isNotEmpty())
    assertTrue(result2.isNotEmpty())
}
```

### Pattern 3: Testing delays/timeouts

```kotlin
@Test
fun testWithTimeout() = runBlocking {
    withTimeout(1000) {
        mySlowFunction()
    }
}
```

## Common Test Scenarios

### Scenario 1: Testing Tool Selection

```kotlin
@Test
fun testToolSelectionByScore() {
    val tool1 = object : ToolHandler {
        override fun canHandle(input: String) = true
        override fun score(input: String) = 5
        override suspend fun handle(input: String) = "Tool1"
    }
    
    val tool2 = object : ToolHandler {
        override fun canHandle(input: String) = true
        override fun score(input: String) = 10
        override suspend fun handle(input: String) = "Tool2"
    }
    
    val tools = listOf(tool1, tool2)
    val input = "test"
    
    // Higher score should be selected
    val selectedTool = tools.maxByOrNull { it.score(input) }
    val result = runBlocking { selectedTool!!.handle(input) }
    
    assertEquals("Tool2", result)
}
```

### Scenario 2: Testing Error Handling

```kotlin
@Test
fun testHandlesNetworkError() {
    val failingLLM = object : LLMProvider {
        override suspend fun generate(input: String): String {
            throw IOException("Network error")
        }
    }
    
    assertFailsWith<IOException> {
        runBlocking { failingLLM.generate("test") }
    }
}
```

### Scenario 3: Testing Memory Persistence

```kotlin
@Test
fun testMemoryPersistsAcrossCalls() = runBlocking {
    val memory = ConversationMemory()
    val agent = AgentFramework(mockLLM, listOf(), memory)
    
    agent.chat("My name is Alice")
    memory.store("user_name", "Alice")
    
    val stored = memory.retrieve("user_name")
    assertEquals("Alice", stored)
}
```

## Best Practices

### ✅ DO

1. **Test one thing per test**
```kotlin
@Test
fun testCanHandleValidInput() { /* ... */ }

@Test
fun testCanHandleInvalidInput() { /* ... */ }
```

2. **Use descriptive test names**
```kotlin
@Test
fun testHandleReturnsFormattedResult() { /* ... */ }
```

3. **Use runBlocking for suspend functions**
```kotlin
@Test
fun testAsyncFunction() {
    val result = runBlocking { asyncFunc() }
    assertNotNull(result)
}
```

4. **Create mock objects for dependencies**
```kotlin
val mockLLM = object : LLMProvider {
    override suspend fun generate(input: String) = "mock"
}
```

5. **Test edge cases**
```kotlin
@Test
fun testHandleEmptyInput() { /* ... */ }

@Test
fun testHandleNullInput() { /* ... */ }
```

### ❌ DON'T

1. **Don't test multiple things in one test**
```kotlin
// Bad
@Test
fun testEverything() {
    testCanHandle()
    testScore()
    testHandle()
}
```

2. **Don't use real API keys in tests**
```kotlin
// Bad
val llm = OpenAILLM(System.getenv("OPENAI_API_KEY"))

// Good
val mockLLM = object : LLMProvider { /* ... */ }
```

3. **Don't forget runBlocking for suspend functions**
```kotlin
// Bad - won't compile
@Test
fun test() {
    val result = suspendFunc()  // Error!
}

// Good
@Test
fun test() {
    val result = runBlocking { suspendFunc() }
}
```

4. **Don't rely on external services**
```kotlin
// Bad - flaky test
@Test
fun testRealAPI() {
    val result = callExternalAPI()  // May fail due to network
}

// Good - use mocks
@Test
fun testWithMock() {
    val mock = object : APIClient {
        override suspend fun call() = "mock response"
    }
}
```

## Test Coverage Goals

Aim for:
- ✅ 70%+ code coverage
- ✅ All public methods tested
- ✅ Edge cases covered
- ✅ Error paths tested
- ✅ Async code tested

## Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "MyComponentTest"

# Run tests with coverage
./gradlew test jacocoTestReport

# Run tests in watch mode
./gradlew test --continuous
```

## Troubleshooting

**Issue: Test hangs**
- Check for missing `runBlocking`
- Look for infinite loops
- Add timeout: `withTimeout(1000) { ... }`

**Issue: Flaky tests**
- Don't rely on external services
- Use mocks for all dependencies
- Avoid time-dependent tests

**Issue: Tests pass but code fails**
- Add integration tests
- Test with real (but test) data
- Verify mock behavior matches real behavior

## Related Skills

- **add-custom-tool**: Create tools (then use this skill to test them)
- **add-llm-provider**: Create LLM providers (then test them)
- **create-agent-graph**: Create graphs (then test them)

## Reference Files

- Test Template: `.claude/skills/add-test-with-mock/templates/test-template.kt`
- Test Patterns: `.claude/skills/add-test-with-mock/references/test-patterns.md`
- Mock Examples: `.claude/skills/add-test-with-mock/references/mock-examples.md`
- Existing Tests: `/home/user/KAgentic/agentic-library/src/test/kotlin/`
