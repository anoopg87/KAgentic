---
name: add-llm-provider
description: Add a new LLM provider to the KAgentic framework by implementing the LLMProvider interface. Use when the user wants to integrate a new language model API (like Mistral, Together AI, Perplexity, or custom LLMs). This skill guides through API integration, retry logic, response parsing, and testing.
version: "1.0.0"
---

# Add LLM Provider

Integrate new language model providers into KAgentic with production-ready patterns.

## When to Use This Skill

Trigger when you need to:
- "Add support for [LLM provider]"
- "I want to integrate [API] as an LLM"
- "How do I add a new language model?"
- "Create a provider for [LLM service]"

## Overview

This skill helps you:
1. Implement the LLMProvider interface (single method)
2. Integrate HTTP client with proper authentication
3. Add retry logic with exponential backoff
4. Handle request/response formats
5. Write tests and documentation

## LLMProvider Interface

Simple interface - just one method:

```kotlin
interface LLMProvider {
    suspend fun generate(input: String): String
}
```

## Implementation Steps

### Step 1: Create Provider Class

Location: `/home/user/KAgentic/agentic-library/src/main/kotlin/llm/YourProviderLLM.kt`

```kotlin
package llm

import core.Logger
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

/**
 * [ProviderName]LLM provides access to [Provider] language models.
 *
 * Supported models:
 * - model-1
 * - model-2
 *
 * Usage Example:
 * ```kotlin
 * val llm = YourProviderLLM(
 *     apiKey = System.getenv("YOUR_API_KEY"),
 *     model = "model-name"
 * )
 * val response = runBlocking { llm.generate("Hello!") }
 * println(response)
 * ```
 *
 * @property apiKey API key for authentication
 * @property model Model identifier to use
 * @property logger Optional logger for debugging
 * @property logEnabled Enable detailed logging
 */
class YourProviderLLM(
    val apiKey: String,
    val model: String = "default-model",
    private val logger: Logger? = null,
    private val logEnabled: Boolean = false
) : LLMProvider {
    
    companion object {
        private const val API_URL = "https://api.provider.com/v1/generate"
    }
    
    private val client = HttpClient()
    
    override suspend fun generate(input: String): String {
        if (logEnabled) logger?.log("Request: $input")
        
        return try {
            // Use retry helper for resilience
            retryWithBackoff(maxRetries = 3) {
                val response = client.post(API_URL) {
                    header("Authorization", "Bearer $apiKey")
                    contentType(ContentType.Application.Json)
                    setBody(buildRequestBody(input))
                }
                
                val responseBody = response.bodyAsText()
                if (logEnabled) logger?.log("Response: $responseBody")
                
                parseResponse(responseBody)
            }
        } catch (e: Exception) {
            if (logEnabled) logger?.log("Error: ${e.message}")
            "Error: ${e.message}"
        }
    }
    
    private fun buildRequestBody(input: String): String {
        val json = buildJsonObject {
            put("model", model)
            put("prompt", input)
            // Add other required fields based on API docs
        }
        return json.toString()
    }
    
    private fun parseResponse(responseBody: String): String {
        val json = Json.parseToJsonElement(responseBody).jsonObject
        return json["response"]?.jsonPrimitive?.content 
            ?: throw IllegalStateException("No response from LLM")
    }
}
```

### Step 2: Authentication Patterns

**Pattern 1: Bearer Token (OpenAI, Cohere, Grok, DeepSeek)**
```kotlin
header("Authorization", "Bearer $apiKey")
```

**Pattern 2: Custom Headers (Claude)**
```kotlin
header("x-api-key", apiKey)
header("anthropic-version", "2023-06-01")
```

**Pattern 3: URL Query Parameter (Gemini)**
```kotlin
val url = "$BASE_URL/$model:generateContent?key=$apiKey"
```

**Pattern 4: No Authentication (Ollama - Local)**
```kotlin
// No auth headers needed for local endpoints
val response = client.post("http://localhost:11434/api/generate")
```

### Step 3: Request/Response Handling

**Building JSON Requests:**
```kotlin
private fun buildRequestBody(input: String): String {
    val json = buildJsonObject {
        put("model", model)
        put("messages", buildJsonArray {
            add(buildJsonObject {
                put("role", "user")
                put("content", input)
            })
        })
        put("max_tokens", 1024)
        put("temperature", 0.7)
    }
    return json.toString()
}
```

**Parsing JSON Responses:**
```kotlin
private fun parseResponse(responseBody: String): String {
    try {
        val json = Json.parseToJsonElement(responseBody).jsonObject
        
        // Extract based on provider's response format
        return json["choices"]
            ?.jsonArray?.get(0)
            ?.jsonObject?.get("message")
            ?.jsonObject?.get("content")
            ?.jsonPrimitive?.content
            ?: json["response"]?.jsonPrimitive?.content
            ?: throw IllegalStateException("No content in response")
            
    } catch (e: Exception) {
        throw IllegalStateException("Failed to parse response: ${e.message}")
    }
}
```

### Step 4: Add Model Enum (Optional)

For providers with multiple models:

```kotlin
class YourProviderLLM(
    val apiKey: String,
    val model: Model = Model.DEFAULT,
    private val logger: Logger? = null,
    private val logEnabled: Boolean = false
) : LLMProvider {
    
    enum class Model(val modelName: String) {
        DEFAULT("provider-default-v1"),
        ADVANCED("provider-advanced-v1"),
        FAST("provider-fast-v1")
    }
    
    override suspend fun generate(input: String): String {
        // Use model.modelName in API calls
        val response = client.post(API_URL) {
            setBody(buildJsonObject {
                put("model", model.modelName)
                put("prompt", input)
            }.toString())
        }
        return parseResponse(response.bodyAsText())
    }
}
```

### Step 5: Add Retry Logic

Use RetryHelper for resilience:

```kotlin
import llm.retryWithBackoff

override suspend fun generate(input: String): String {
    return try {
        retryWithBackoff(maxRetries = 3) {
            val response = client.post(API_URL) {
                // Configure request
            }
            parseResponse(response.bodyAsText())
        }
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}
```

### Step 6: Write Tests

Add to `/home/user/KAgentic/agentic-library/src/test/kotlin/llm/LLMProviderTest.kt`:

```kotlin
@Test
fun testYourProviderLLMInstantiation() {
    val provider = YourProviderLLM(
        apiKey = "test-api-key",
        model = "test-model"
    )
    assertNotNull(provider)
    assertTrue(provider.apiKey == "test-api-key")
}

@Test
fun testYourProviderLLMWithEnum() {
    val provider = YourProviderLLM(
        apiKey = "test-key",
        model = YourProviderLLM.Model.ADVANCED
    )
    assertNotNull(provider)
}
```

### Step 7: Update Documentation

**Add to README.md:**
```markdown
### YourProvider
\`\`\`kotlin
val provider = YourProviderLLM(apiKey = System.getenv("YOUR_API_KEY"))
val response = runBlocking { provider.generate("Hello!") }
\`\`\`
```

**Add to CHANGELOG.md:**
```markdown
### Added
- YourProviderLLM for [Provider Name] integration
```

## Complete Example

```kotlin
package llm

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

class MistralLLM(
    val apiKey: String,
    val model: String = "mistral-large-latest"
) : LLMProvider {
    
    companion object {
        private const val API_URL = "https://api.mistral.ai/v1/chat/completions"
    }
    
    private val client = HttpClient()
    
    override suspend fun generate(input: String): String {
        return try {
            retryWithBackoff(maxRetries = 3) {
                val response = client.post(API_URL) {
                    header("Authorization", "Bearer $apiKey")
                    contentType(ContentType.Application.Json)
                    setBody(buildJsonObject {
                        put("model", model)
                        put("messages", buildJsonArray {
                            add(buildJsonObject {
                                put("role", "user")
                                put("content", input)
                            })
                        })
                    }.toString())
                }
                
                val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                json["choices"]
                    ?.jsonArray?.get(0)
                    ?.jsonObject?.get("message")
                    ?.jsonObject?.get("content")
                    ?.jsonPrimitive?.content
                    ?: "No response"
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
```

## Best Practices

### From OpenAILLM
- Use enum for model selection
- Include max_tokens parameter
- Parse nested JSON responses
- Bearer token authentication

### From ClaudeLLM
- Custom headers for authentication
- Include API version header
- Set max_tokens explicitly
- Handle rate limits gracefully

### From GeminiLLM
- API key in URL query parameter
- Different endpoint structure
- Model-specific content formatting

### From OllamaLLM
- Local endpoint support
- No authentication needed
- Stream response handling (optional)

## Troubleshooting

**Issue: API returns 401 Unauthorized**
- Verify API key is correct
- Check authorization header format
- Ensure API key has proper permissions

**Issue: API returns 429 Too Many Requests**
- Implement rate limiting
- Increase retry backoff delay
- Use exponential backoff

**Issue: JSON parsing fails**
- Log raw response for debugging
- Set ignoreUnknownKeys = true
- Verify response structure matches expectations

**Issue: Timeout errors**
- Increase HTTP client timeout
- Check network connectivity
- Verify endpoint URL is correct

## Related Skills

- **add-test-with-mock**: Testing LLM providers without API calls
- **understand-agent-flow**: Debugging LLM integration

## Reference Files

- Template: `.claude/skills/add-llm-provider/templates/llm-provider-template.kt`
- Provider Comparison: `.claude/skills/add-llm-provider/references/existing-providers.md`
- Retry Patterns: `.claude/skills/add-llm-provider/references/retry-patterns.md`

## Existing Providers for Reference

- OpenAILLM: `/home/user/KAgentic/agentic-library/src/main/kotlin/llm/OpenAILLM.kt`
- ClaudeLLM: `/home/user/KAgentic/agentic-library/src/main/kotlin/llm/ClaudeLLM.kt`
- GeminiLLM: `/home/user/KAgentic/agentic-library/src/main/kotlin/llm/GeminiLLM.kt`
- OllamaLLM: `/home/user/KAgentic/agentic-library/src/main/kotlin/llm/OllamaLLM.kt`
- RetryHelper: `/home/user/KAgentic/agentic-library/src/main/kotlin/llm/RetryHelper.kt`
