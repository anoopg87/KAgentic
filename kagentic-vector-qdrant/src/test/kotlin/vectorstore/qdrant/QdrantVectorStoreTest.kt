package vectorstore.qdrant

import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertEquals

class QdrantVectorStoreTest {

    @Test
    fun testQdrantVectorStore_Instantiation_NoApiKey() {
        val store = QdrantVectorStore(
            host = "http://localhost:6333",
            collectionName = "test-collection"
        )
        assertNotNull(store)
    }

    @Test
    fun testQdrantVectorStore_Instantiation_WithApiKey() {
        val store = QdrantVectorStore(
            host = "https://xyz.aws.cloud.qdrant.io:6333",
            collectionName = "prod-docs",
            apiKey = "test-api-key"
        )
        assertNotNull(store)
    }

    @Test
    fun testQdrantVectorStore_TextPayloadKey_IsStable() {
        // This key must not change — it would break retrieval of text from existing points
        assertEquals("__text__", QdrantVectorStore.TEXT_PAYLOAD_KEY)
    }

    @Test
    fun testQdrantVectorStore_ImplementsVectorStore() {
        val store: vectorstore.VectorStore = QdrantVectorStore(
            host = "http://localhost:6333",
            collectionName = "test-collection"
        )
        assertNotNull(store)
    }
}
