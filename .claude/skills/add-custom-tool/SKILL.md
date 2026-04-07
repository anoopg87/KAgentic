---
name: add-custom-tool  
description: Create a custom tool for the KAgentic framework by implementing the ToolHandler interface. Use when the user wants to add a new tool capability like DateTime, Weather, Database query, or any custom functionality. This skill guides through creating the tool class, implementing canHandle/score/handle methods, writing tests, and integrating with AgentFramework.
version: "1.0.0"
---

# Add Custom Tool

Create production-ready custom tools for the KAgentic framework by implementing the ToolHandler interface.

## When to Use This Skill

Trigger when you need to:
- "Create a tool that can..."
- "I want to add a new tool for..."
- "How do I make a tool for..."
- "Build a custom tool to handle..."

## Overview

This skill helps you:
1. Implement the ToolHandler interface (3 required methods)
2. Follow KAgentic's tool selection patterns
3. Write comprehensive tests
4. Integrate with AgentFramework

## ToolHandler Interface

All tools implement this interface:

```kotlin
interface ToolHandler {
    fun canHandle(input: String): Boolean  // Pattern matching
    suspend fun handle(input: String): String  // Async execution
    fun score(input: String): Int  // Priority (1-10)
}
```

## Implementation Steps

### Step 1: Create Tool Class

Location: `/home/user/KAgentic/agentic-library/src/main/kotlin/tools/YourToolName.kt`

```kotlin
package tools

/**
 * Brief description of what this tool does.
 *
 * Usage Example:
 * ```kotlin
 * val tool = YourToolName()
 * val result = runBlocking { tool.handle("input here") }
 * println(result)
 * ```
 */
class YourToolName : ToolHandler {
    
    override fun canHandle(input: String): Boolean {
        // Return true if this tool can process the input
        // Use keywords, regex patterns, or format checks
        return input.contains("keyword", ignoreCase = true)
    }
    
    override fun score(input: String): Int {
        // Higher scores (1-10) indicate better match
        // Prioritizes when multiple tools can handle input
        return when {
            input.startsWith("exact match", ignoreCase = true) -> 10
            input.contains("good match", ignoreCase = true) -> 7
            input.contains("keyword", ignoreCase = true) -> 5
            else -> 1
        }
    }
    
    override suspend fun handle(input: String): String {
        // Implement tool logic here
        // This is a suspend function - async operations allowed
        return try {
            // Your implementation
            val result = processInput(input)
            "Result: $result"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
    
    private fun processInput(input: String): String {
        // Tool-specific logic
        return "processed $input"
    }
}
```

### Step 2: Design Tool Selection Logic

**canHandle** - Fast pattern matching:
```kotlin
// Keyword matching
override fun canHandle(input: String): Boolean {
    return input.contains("weather", ignoreCase = true)
}

// Regex patterns
override fun canHandle(input: String): Boolean {
    return input.matches(Regex("[\\d\\s\\+\\-\\*/\\(\\)\\.]+"))
}

// Multiple conditions
override fun canHandle(input: String): Boolean {
    return input.contains("read", ignoreCase = true) &&
           input.matches(Regex(".*\\.(txt|md|csv)$"))
}
```

**score** - Deterministic priority:
```kotlin
override fun score(input: String): Int {
    return when {
        input.startsWith("calculate", ignoreCase = true) -> 10  // Highest
        input.contains("math", ignoreCase = true) -> 7          // Medium
        input.matches(Regex("[\\d\\+\\-\\*/]+")) -> 5            // Low-medium
        else -> 1                                                 // Minimum
    }
}
```

### Step 3: Implement Handle Logic

**Pattern 1: API-Based Tool**
```kotlin
override suspend fun handle(input: String): String {
    return try {
        val url = URL("https://api.service.com/endpoint")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        val response = conn.inputStream.bufferedReader().readText()
        conn.disconnect()
        response
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}
```

**Pattern 2: Computation Tool**
```kotlin
override suspend fun handle(input: String): String {
    return try {
        val result = ExpressionBuilder(input).build().evaluate()
        "Result: $result"
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}
```

**Pattern 3: File-Based Tool**
```kotlin
override suspend fun handle(input: String): String {
    return try {
        val file = File(input)
        if (file.exists()) file.readText()
        else "File not found: $input"
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}
```

### Step 4: Write Tests

Location: `/home/user/KAgentic/agentic-library/src/test/kotlin/tools/YourToolNameTest.kt`

```kotlin
import tools.YourToolName
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.*

class YourToolNameTest {
    
    @Test
    fun testCanHandle_ValidInput_ReturnsTrue() {
        val tool = YourToolName()
        assertTrue(tool.canHandle("valid input pattern"))
    }
    
    @Test
    fun testCanHandle_InvalidInput_ReturnsFalse() {
        val tool = YourToolName()
        assertFalse(tool.canHandle("invalid pattern"))
    }
    
    @Test
    fun testScore_HighPriority_Returns10() {
        val tool = YourToolName()
        assertEquals(10, tool.score("exact match pattern"))
    }
    
    @Test
    fun testScore_MediumPriority_Returns7() {
        val tool = YourToolName()
        assertEquals(7, tool.score("good match pattern"))
    }
    
    @Test
    fun testHandle_ValidInput_ReturnsExpectedResult() = runBlocking {
        val tool = YourToolName()
        val result = tool.handle("test input")
        assertTrue(result.contains("Result:"))
    }
    
    @Test
    fun testHandle_ErrorCondition_ReturnsErrorMessage() = runBlocking {
        val tool = YourToolName()
        val result = tool.handle("invalid input")
        assertTrue(result.contains("Error:"))
    }
}
```

### Step 5: Integration with AgentFramework

```kotlin
import core.AgentFramework
import llm.OpenAILLM
import memory.ConversationMemory
import tools.YourToolName
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val tools = listOf(YourToolName())
    val agent = AgentFramework(
        llm = OpenAILLM(apiKey = System.getenv("OPENAI_API_KEY")),
        tools = tools,
        memory = ConversationMemory()
    )
    
    val response = agent.chat("Use your tool here")
    println(response)
}
```

### Step 6: Run Tests

```bash
cd /home/user/KAgentic
./gradlew test --tests "YourToolNameTest"
```

## Best Practices

### From CalculatorTool

- Simple regex pattern matching
- Safe evaluation with exp4j library
- Clear error messages
- Deterministic scoring

### From WebSearchTool

- API key management via environment variables
- HTTP error handling
- JSON response parsing (if applicable)
- Graceful degradation

### From FileReaderTool

- File existence validation
- Permission handling
- Descriptive error messages
- Support multiple file types

### From APICallerTool

- HTTP method flexibility
- Connection management (disconnect after use)
- Timeout handling
- Response status checking

## Common Patterns

**Scoring Strategy:**
- 10: Exact/explicit match (starts with command)
- 7-9: Strong match (contains key phrase)
- 4-6: Pattern match (regex/format)
- 1-3: Weak/default match

**Error Handling:**
- Always use try-catch
- Return string error messages (not exceptions)
- Include specific error details
- Validate inputs before processing

**KDoc Documentation:**
- Brief summary
- Usage example with runBlocking
- Parameter descriptions
- Expected output format

## Troubleshooting

**Tool not being selected:**
- Check canHandle logic (too restrictive?)
- Increase score for better priority
- Test with various input phrasings
- Ensure other tools don't have higher scores

**Tests failing:**
- Use `runBlocking` for suspend functions
- Check async operations complete
- Verify imports are correct
- Ensure test matches actual behavior

**Build errors:**
- Package declaration matches directory
- All imports are correct
- Kotlin version compatible (1.9.23)
- Dependencies available

## Related Skills

- **add-test-with-mock**: Advanced testing scenarios
- **understand-agent-flow**: Debug tool selection

## Reference Files

- Template: `.claude/skills/add-custom-tool/templates/tool-template.kt`
- Examples: `.claude/skills/add-custom-tool/references/tool-examples.md`
- Best Practices: `.claude/skills/add-custom-tool/references/best-practices.md`

## Existing Tools for Reference

- CalculatorTool: `/home/user/KAgentic/agentic-library/src/main/kotlin/tools/CalculatorTool.kt`
- WebSearchTool: `/home/user/KAgentic/agentic-library/src/main/kotlin/tools/WebSearchTool.kt`
- FileReaderTool: `/home/user/KAgentic/agentic-library/src/main/kotlin/tools/FileReaderTool.kt`
- APICallerTool: `/home/user/KAgentic/agentic-library/src/main/kotlin/tools/APICallerTool.kt`
- Example: `/home/user/KAgentic/examples/custom-tools/CustomToolExample.kt`
