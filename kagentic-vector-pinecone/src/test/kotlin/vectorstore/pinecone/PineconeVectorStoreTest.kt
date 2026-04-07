import vectorstore.pinecone.PineconeVectorStore
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertEquals

class PineconeVectorStoreTest {

    @Test
    fun testPineconeVectorStore_Instantiation_DefaultNamespace() {
        val store = PineconeVectorStore(
            apiKey = "test-api-key",
            indexHost = "https://my-index.svc.us-east1-gcp.pinecone.io"
        )
        assertNotNull(store)
    }

    @Test
    fun testPineconeVectorStore_Instantiation_CustomNamespace() {
        val store = PineconeVectorStore(
            apiKey = "test-api-key",
            indexHost = "https://my-index.svc.us-east1-gcp.pinecone.io",
            namespace = "tenant-42"
        )
        assertNotNull(store)
    }

    @Test
    fun testPineconeVectorStore_TextMetadataKey_IsStable() {
        // This key must not change — it would break retrieval of text from existing documents
        assertEquals("__text__", PineconeVectorStore.TEXT_METADATA_KEY)
    }

    @Test
    fun testPineconeVectorStore_ImplementsVectorStore() {
        val store: vectorstore.VectorStore = PineconeVectorStore(
            apiKey = "test-api-key",
            indexHost = "https://my-index.svc.us-east1-gcp.pinecone.io"
        )
        assertNotNull(store)
    }
}
