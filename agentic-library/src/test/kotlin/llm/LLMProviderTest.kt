package llm

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

/**
 * Tests for LLM provider implementations.
 * These are basic contract tests to ensure all LLM providers implement the interface correctly.
 */
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

    @Test
    fun testClaudeLLMInstantiation() {
        val claude = ClaudeLLM(
            apiKey = "test-api-key",
            model = "claude-3-opus-20240229"
        )
        assertNotNull(claude)
        assertTrue(claude.apiKey == "test-api-key")
    }

    @Test
    fun testOllamaLLMInstantiation() {
        val ollama = OllamaLLM(
            model = "llama2",
            baseUrl = "http://localhost:11434"
        )
        assertNotNull(ollama)
        assertTrue(ollama.model == "llama2")
    }

    @Test
    fun testCohereLLMInstantiation() {
        val cohere = CohereLLM(
            apiKey = "test-api-key",
            model = "command"
        )
        assertNotNull(cohere)
        assertTrue(cohere.apiKey == "test-api-key")
    }

    @Test
    fun testGrokLLMInstantiation() {
        val grok = GrokLLM(
            apiKey = "test-api-key",
            model = "grok-1"
        )
        assertNotNull(grok)
        assertTrue(grok.apiKey == "test-api-key")
    }

    @Test
    fun testDeepSeekLLMInstantiation() {
        val deepseek = DeepSeekLLM(
            apiKey = "test-api-key",
            model = "deepseek-chat"
        )
        assertNotNull(deepseek)
        assertTrue(deepseek.apiKey == "test-api-key")
    }

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

    @Test
    fun testSimpleEmbeddingProvider() {
        val embedder = SimpleEmbeddingProvider()
        val embedding = runBlocking { embedder.embed("test input") }
        assertNotNull(embedding)
        assertTrue(embedding.isNotEmpty())
        // SimpleEmbeddingProvider should return a list of floats
        assertTrue(embedding.all { it is Float })
    }

    @Test
    fun testSimpleChatModelProvider() {
        val chatModel = SimpleChatModelProvider()
        val messages = listOf("Hello", "How are you?", "Tell me a joke")
        val response = runBlocking { chatModel.chat(messages) }
        assertNotNull(response)
        assertTrue(response.isNotEmpty())
    }
}
