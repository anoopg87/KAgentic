import embeddings.GeminiEmbeddingProvider
import embeddings.OllamaEmbeddingProvider
import embeddings.OpenAIEmbeddingProvider
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertEquals

class EmbeddingProviderTest {

    // --- OpenAIEmbeddingProvider ---

    @Test
    fun testOpenAIEmbeddingProvider_DefaultModel_IsTextEmbedding3Small() {
        val provider = OpenAIEmbeddingProvider(apiKey = "test-key")
        assertNotNull(provider)
        assertEquals(OpenAIEmbeddingProvider.Model.TEXT_EMBEDDING_3_SMALL, provider.model)
    }

    @Test
    fun testOpenAIEmbeddingProvider_ExplicitModel_IsSet() {
        val provider = OpenAIEmbeddingProvider(
            apiKey = "test-key",
            model = OpenAIEmbeddingProvider.Model.TEXT_EMBEDDING_3_LARGE
        )
        assertEquals(OpenAIEmbeddingProvider.Model.TEXT_EMBEDDING_3_LARGE, provider.model)
    }

    @Test
    fun testOpenAIEmbeddingProvider_AllModelsHaveModelIds() {
        OpenAIEmbeddingProvider.Model.entries.forEach { model ->
            assertNotNull(model.modelId)
            assert(model.modelId.isNotBlank()) { "Model ${model.name} has blank modelId" }
        }
    }

    // --- GeminiEmbeddingProvider ---

    @Test
    fun testGeminiEmbeddingProvider_DefaultModel_IsTextEmbedding004() {
        val provider = GeminiEmbeddingProvider(apiKey = "test-key")
        assertNotNull(provider)
        assertEquals(GeminiEmbeddingProvider.Model.TEXT_EMBEDDING_004, provider.model)
    }

    @Test
    fun testGeminiEmbeddingProvider_ExplicitModel_IsSet() {
        val provider = GeminiEmbeddingProvider(
            apiKey = "test-key",
            model = GeminiEmbeddingProvider.Model.EMBEDDING_001
        )
        assertEquals(GeminiEmbeddingProvider.Model.EMBEDDING_001, provider.model)
    }

    @Test
    fun testGeminiEmbeddingProvider_AllModelsHaveModelIds() {
        GeminiEmbeddingProvider.Model.entries.forEach { model ->
            assertNotNull(model.modelId)
            assert(model.modelId.isNotBlank()) { "Model ${model.name} has blank modelId" }
        }
    }

    // --- OllamaEmbeddingProvider ---

    @Test
    fun testOllamaEmbeddingProvider_DefaultModel_IsNomicEmbedText() {
        val provider = OllamaEmbeddingProvider()
        assertNotNull(provider)
        assertEquals(OllamaEmbeddingProvider.Model.NOMIC_EMBED_TEXT, provider.model)
    }

    @Test
    fun testOllamaEmbeddingProvider_CustomEndpoint_IsSet() {
        val provider = OllamaEmbeddingProvider(endpoint = "http://custom-host:11434")
        assertNotNull(provider)
        assertEquals("http://custom-host:11434", provider.endpoint)
    }

    @Test
    fun testOllamaEmbeddingProvider_AllModelsHaveModelIds() {
        OllamaEmbeddingProvider.Model.entries.forEach { model ->
            assertNotNull(model.modelId)
            assert(model.modelId.isNotBlank()) { "Model ${model.name} has blank modelId" }
        }
    }
}
