# Existing LLM Providers Comparison

Comparison of all 7 LLM providers in KAgentic.

## Provider Summary

| Provider | Auth Method | Endpoint | Features |
|----------|-------------|----------|----------|
| OpenAI | Bearer Token | api.openai.com/v1/chat/completions | Retry, Logging, Enum |
| Claude | Custom Headers | api.anthropic.com/v1/messages | Retry, Logging, Version Header |
| Gemini | URL Parameter | generativelanguage.googleapis.com | Retry, Logging |
| Ollama | None (Local) | localhost:11434/api/generate | Local Models |
| Cohere | Bearer Token | api.cohere.ai/v1/generate | Basic Implementation |
| Grok | Bearer Token | api.grok.com/v1/chat/completions | Logging |
| DeepSeek | Bearer Token | api.deepseek.com/v1/chat/completions | Logging |

## Authentication Patterns

### Bearer Token (Most Common)
Used by: OpenAI, Cohere, Grok, DeepSeek

```kotlin
header("Authorization", "Bearer $apiKey")
```

### Custom Headers
Used by: Claude

```kotlin
header("x-api-key", apiKey)
header("anthropic-version", "2023-06-01")
```

### URL Query Parameter
Used by: Gemini

```kotlin
val url = "$BASE_URL/$model:generateContent?key=$apiKey"
```

### No Authentication
Used by: Ollama (local models)

```kotlin
// No authentication needed
client.post("http://localhost:11434/api/generate")
```

## Request Format Examples

### OpenAI Format (Chat Completions)
```json
{
  "model": "gpt-4",
  "messages": [
    {"role": "user", "content": "Hello"}
  ],
  "max_tokens": 1024
}
```

### Claude Format
```json
{
  "model": "claude-opus-4",
  "max_tokens": 1024,
  "messages": [
    {"role": "user", "content": "Hello"}
  ]
}
```

### Gemini Format
```json
{
  "contents": [
    {"parts": [{"text": "Hello"}]}
  ]
}
```

### Ollama Format
```json
{
  "model": "llama2",
  "prompt": "Hello"
}
```

## Retry Logic Implementation

Providers with retry: OpenAI, Claude, Gemini

```kotlin
override suspend fun generate(input: String): String {
    return try {
        retryWithBackoff(maxRetries = 3) {
            // API call here
        }
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}
```

Providers without retry: Ollama, Cohere, Grok, DeepSeek

## Model Selection Patterns

### Enum Pattern (OpenAI, Claude, Gemini, Ollama)
```kotlin
enum class Model(val modelName: String) {
    GPT_5("gpt-5"),
    O3("o3"),
    O4_MINI("o4-mini")
}
```

### String Parameter (Cohere, Grok, DeepSeek)
```kotlin
class Provider(val apiKey: String, val model: String = "default")
```

## When to Use Each Pattern

- **Bearer Token**: Most API providers (standard)
- **Custom Headers**: Provider-specific requirements
- **URL Parameter**: When provider requires it (less common)
- **No Auth**: Local/self-hosted models only
