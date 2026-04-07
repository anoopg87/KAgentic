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
            model = OpenAILLM.Model.GPT_5
        )
        assertNotNull(openai)
        assertTrue(openai.apiKey == "test-api-key")
    }

    @Test
    fun testClaudeLLMInstantiation() {
        val claude = ClaudeLLM(
            apiKey = "test-api-key",
            model = ClaudeLLM.Model.CLAUDE_OPUS_4
        )
        assertNotNull(claude)
        assertTrue(claude.apiKey == "test-api-key")
    }

    @Test
    fun testOllamaLLMInstantiation() {
        val ollama = OllamaLLM(
            model = OllamaLLM.Model.LLAMA2,
            endpoint = "http://localhost:11434"
        )
        assertNotNull(ollama)
        assertTrue(ollama.model == OllamaLLM.Model.LLAMA2)
    }

    @Test
    fun testCohereLLMInstantiation() {
        val cohere = CohereLLM(
            apiKey = "test-api-key",
            model = CohereLLM.Model.COMMAND
        )
        assertNotNull(cohere)
        assertTrue(cohere.apiKey == "test-api-key")
    }

    @Test
    fun testGrokLLMInstantiation() {
        val grok = GrokLLM(
            apiKey = "test-api-key",
            model = GrokLLM.Model.GROK_4
        )
        assertNotNull(grok)
        assertTrue(grok.apiKey == "test-api-key")
    }

    @Test
    fun testDeepSeekLLMInstantiation() {
        val deepseek = DeepSeekLLM(
            apiKey = "test-api-key",
            model = DeepSeekLLM.Model.DEEPSEEK_V3_0324
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
