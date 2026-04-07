# Tool Design Best Practices

Best practices for designing custom tools in KAgentic, derived from existing implementations.

## Best Practices

### 1. Pattern Matching Design

**✅ DO:**
- Use case-insensitive matching for keywords
- Combine multiple matching strategies (keyword + regex)
- Make patterns specific but not overly restrictive
- Test with various input phrasings

**❌ DON'T:**
- Use expensive operations in canHandle()
- Match too broadly (causes conflicts with other tools)
- Require exact input formats
- Use blocking I/O in canHandle()

**Example:**
```kotlin
// Good: Flexible keyword matching
override fun canHandle(input: String): Boolean {
    return input.contains("weather", ignoreCase = true) ||
           input.contains("temperature", ignoreCase = true)
}

// Bad: Too restrictive
override fun canHandle(input: String): Boolean {
    return input == "get weather"  // Only matches exact string
}
```

### 2. Scoring Strategy

**✅ DO:**
- Use consistent scoring scale (1-10)
- Reserve 10 for exact/explicit matches
- Use 7-9 for strong matches
- Use 4-6 for pattern matches
- Default to 1 for fallback

**❌ DON'T:**
- Use scores above 10
- Use random/variable scoring
- Return 0 (use 1 as minimum)
- Make scoring dependent on external state

**Example:**
```kotlin
// Good: Clear hierarchy
override fun score(input: String): Int {
    return when {
        input.startsWith("calculate", ignoreCase = true) -> 10
        input.contains("math", ignoreCase = true) -> 7
        input.matches(Regex("\\d+")) -> 5
        else -> 1
    }
}
```

### 3. Error Handling

**✅ DO:**
- Use try-catch for all operations
- Return descriptive error messages as strings
- Include error details in messages
- Log errors if logger available
- Validate inputs before processing

**❌ DON'T:**
- Throw exceptions from handle()
- Return null or empty strings on error
- Use generic error messages
- Silently fail

**Example:**
```kotlin
// Good: Descriptive error handling
override suspend fun handle(input: String): String {
    return try {
        validateInput(input)
        val result = processInput(input)
        "Result: $result"
    } catch (e: IllegalArgumentException) {
        "Error: Invalid input format - ${e.message}"
    } catch (e: IOException) {
        "Error: Failed to access resource - ${e.message}"
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}

// Bad: Generic errors
override suspend fun handle(input: String): String {
    try {
        return process(input)
    } catch (e: Exception) {
        return "Error"  // Not helpful
    }
}
```

### 4. Documentation

**✅ DO:**
- Add KDoc to all public classes
- Include usage examples in KDoc
- Document expected input formats
- Describe what the tool returns
- Show runBlocking in examples

**❌ DON'T:**
- Skip documentation
- Use vague descriptions
- Omit usage examples
- Forget to document exceptions

**Example:**
```kotlin
/**
 * WeatherTool provides current weather information.
 *
 * Handles queries about weather, temperature, and conditions.
 *
 * Usage Example:
 * ```kotlin
 * val tool = WeatherTool()
 * val result = runBlocking { tool.handle("weather in Tokyo") }
 * println(result)
 * ```
 *
 * @property apiKey Weather API key for service access
 */
class WeatherTool(private val apiKey: String) : ToolHandler { ... }
```

### 5. Input Validation

**✅ DO:**
- Validate before processing
- Check for null/empty inputs
- Verify required parameters
- Sanitize user input
- Limit input size if needed

**❌ DON'T:**
- Process without validation
- Trust all user input
- Allow unlimited input size
- Skip format checks

### 6. Resource Management

**✅ DO:**
- Close connections after use
- Clean up temporary resources
- Use try-finally for cleanup
- Dispose of file handles
- Manage timeouts appropriately

**❌ DON'T:**
- Leave connections open
- Leak file handles
- Ignore timeouts
- Assume resources are infinite

### 7. Testing

**✅ DO:**
- Test canHandle with various inputs
- Test score returns expected values
- Test handle with valid inputs
- Test error conditions
- Use runBlocking for async tests
- Mock external dependencies

**❌ DON'T:**
- Skip edge case testing
- Test only happy paths
- Make real API calls in tests
- Ignore async testing patterns

## Anti-Patterns to Avoid

### Anti-Pattern 1: Stateful Tools
```kotlin
// BAD: Tool maintains state between calls
class BadTool : ToolHandler {
    private var callCount = 0  // State between calls
    
    override suspend fun handle(input: String): String {
        callCount++  // Bad: affects tool selection
        return "Called $callCount times"
    }
}

// GOOD: Stateless tool
class GoodTool : ToolHandler {
    override suspend fun handle(input: String): String {
        val localState = initializeState()
        return processWithState(input, localState)
    }
}
```

### Anti-Pattern 2: Blocking in canHandle
```kotlin
// BAD: Network call in canHandle
override fun canHandle(input: String): Boolean {
    val response = httpClient.get(url)  // Blocks!
    return response.contains("valid")
}

// GOOD: Fast pattern matching
override fun canHandle(input: String): Boolean {
    return input.contains("keyword", ignoreCase = true)
}
```

### Anti-Pattern 3: Overlapping Patterns
```kotlin
// BAD: Too broad - conflicts with other tools
override fun canHandle(input: String): Boolean {
    return input.isNotEmpty()  // Matches everything!
}

// GOOD: Specific patterns
override fun canHandle(input: String): Boolean {
    return input.contains("specific keyword", ignoreCase = true)
}
```

## Performance Considerations

1. **canHandle() must be fast** - Called for every tool on every input
2. **Avoid regex if simple string matching works**
3. **Cache expensive computations** (but keep tools stateless for calls)
4. **Use suspend for I/O operations**
5. **Consider timeout for external calls**

## Security Considerations

1. **Validate and sanitize all inputs**
2. **Never execute arbitrary code from input**
3. **Limit file system access**
4. **Validate URLs before making requests**
5. **Handle API keys securely** (environment variables)
6. **Prevent injection attacks** (SQL, command, etc.)

## Example: Well-Designed Tool

```kotlin
package tools

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * DateTimeTool provides current date and time information.
 *
 * Usage Example:
 * ```kotlin
 * val tool = DateTimeTool()
 * val result = runBlocking { tool.handle("what time is it") }
 * println(result)  // "Current time: 2024-01-01 14:30:00"
 * ```
 */
class DateTimeTool : ToolHandler {
    
    override fun canHandle(input: String): Boolean {
        return input.contains("time", ignoreCase = true) ||
               input.contains("date", ignoreCase = true) ||
               input.contains("today", ignoreCase = true) ||
               input.contains("now", ignoreCase = true)
    }
    
    override fun score(input: String): Int {
        return when {
            input.matches(Regex("what (time|date) is it", RegexOption.IGNORE_CASE)) -> 10
            input.contains("current time", ignoreCase = true) -> 9
            input.contains("today", ignoreCase = true) -> 7
            input.contains("time", ignoreCase = true) -> 5
            input.contains("date", ignoreCase = true) -> 5
            else -> 1
        }
    }
    
    override suspend fun handle(input: String): String {
        return try {
            val now = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val formatted = now.format(formatter)
            
            when {
                input.contains("time", ignoreCase = true) -> 
                    "Current time: $formatted"
                input.contains("date", ignoreCase = true) -> 
                    "Current date: ${formatted.split(" ")[0]}"
                else -> 
                    "Current date and time: $formatted"
            }
        } catch (e: Exception) {
            "Error: Unable to get current time - ${e.message}"
        }
    }
}
```

This tool demonstrates:
- ✅ Multiple keyword matching
- ✅ Clear scoring hierarchy
- ✅ Proper error handling
- ✅ KDoc with usage example
- ✅ Stateless design
- ✅ Fast canHandle implementation
- ✅ Descriptive results
