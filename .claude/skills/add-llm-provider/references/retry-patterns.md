# Retry Patterns with RetryHelper

RetryHelper provides exponential backoff for resilient LLM API calls.

## RetryHelper API

Location: `/home/user/KAgentic/agentic-library/src/main/kotlin/llm/RetryHelper.kt`

### Main Function

```kotlin
suspend fun <T> retryWithExponentialBackoff(
    maxRetries: Int = 3,
    initialDelayMs: Long = 1000,
    maxDelayMs: Long = 10000,
    factor: Double = 2.0,
    retryOn: (Exception) -> Boolean = { true },
    block: suspend () -> T
): T
```

### Extension Function (Simpler)

```kotlin
suspend fun <T> retryWithBackoff(
    maxRetries: Int = 3,
    block: suspend () -> T
): T
```

## Basic Usage

```kotlin
override suspend fun generate(input: String): String {
    return retryWithBackoff(maxRetries = 3) {
        val response = client.post(API_URL) {
            // Configure request
        }
        parseResponse(response.bodyAsText())
    }
}
```

## How Exponential Backoff Works

Default delays with factor=2.0:
- Attempt 1: Immediate
- Attempt 2: Wait 1000ms (1s)
- Attempt 3: Wait 2000ms (2s)
- Attempt 4: Wait 4000ms (4s)

## Custom Configuration

```kotlin
retryWithExponentialBackoff(
    maxRetries = 5,
    initialDelayMs = 500,
    maxDelayMs = 30000,
    factor = 2.5
) {
    // Your API call
}
```

## Selective Retry

Only retry on specific exceptions:

```kotlin
retryWithExponentialBackoff(
    retryOn = { exception ->
        exception is IOException || 
        exception is TimeoutException
    }
) {
    // API call
}
```

## Best Practices

1. **Use default maxRetries=3** for most cases
2. **Don't retry on 401/403** (authentication errors)
3. **Do retry on 429/500/503** (rate limits, server errors)
4. **Log retry attempts** for debugging
5. **Set reasonable timeouts** to prevent hanging

## Example: Full Implementation

```kotlin
override suspend fun generate(input: String): String {
    if (logEnabled) logger?.log("Request: $input")
    
    return try {
        retryWithBackoff(maxRetries = 3) {
            val response = client.post(API_URL) {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(buildRequest(input))
            }
            
            // Check for retriable errors
            when (response.status.value) {
                429 -> throw Exception("Rate limit - will retry")
                in 500..599 -> throw Exception("Server error - will retry")
                else -> parseResponse(response.bodyAsText())
            }
        }
    } catch (e: Exception) {
        if (logEnabled) logger?.log("Error after retries: ${e.message}")
        "Error: ${e.message}"
    }
}
```
