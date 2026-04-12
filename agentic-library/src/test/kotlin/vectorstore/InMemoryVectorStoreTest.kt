import vectorstore.InMemoryVectorStore
import vectorstore.VectorDocument
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InMemoryVectorStoreTest {

    private lateinit var store: InMemoryVectorStore

    @BeforeEach
    fun setup() {
        store = InMemoryVectorStore()
    }

    // --- upsert / upsertAll ---

    @Test
    fun testUpsert_SingleDocument_StoresCorrectly() = runBlocking {
        val doc = VectorDocument(id = "1", vector = listOf(1f, 0f, 0f), text = "hello")
        store.upsert(doc)
        assertEquals(1, store.size())
    }

    @Test
    fun testUpsert_SameId_Overwrites() = runBlocking {
        store.upsert(VectorDocument(id = "1", vector = listOf(1f, 0f), text = "original"))
        store.upsert(VectorDocument(id = "1", vector = listOf(0f, 1f), text = "updated"))
        assertEquals(1, store.size())
        val results = store.search(listOf(0f, 1f), topK = 1)
        assertEquals("updated", results.first().text)
    }

    @Test
    fun testUpsertAll_MultipleDocs_StoresAll() = runBlocking {
        val docs = (1..5).map { i ->
            VectorDocument(id = "$i", vector = listOf(i.toFloat(), 0f), text = "doc $i")
        }
        store.upsertAll(docs)
        assertEquals(5, store.size())
    }

    // --- search ---

    @Test
    fun testSearch_IdenticalVector_ReturnsScoreOne() = runBlocking {
        val vector = listOf(0.6f, 0.8f)
        store.upsert(VectorDocument(id = "1", vector = vector, text = "exact match"))
        val results = store.search(vector, topK = 1)
        assertEquals(1, results.size)
        assertEquals("1", results.first().id)
        assertTrue(results.first().score >= 0.9999f) // cosine of identical vectors = 1.0
    }

    @Test
    fun testSearch_OrderedByScore_DescendingOrder() = runBlocking {
        store.upsertAll(listOf(
            VectorDocument(id = "near",  vector = listOf(0.9f, 0.1f), text = "near"),
            VectorDocument(id = "far",   vector = listOf(0.1f, 0.9f), text = "far"),
            VectorDocument(id = "exact", vector = listOf(1.0f, 0.0f), text = "exact")
        ))
        val query = listOf(1.0f, 0.0f)
        val results = store.search(query, topK = 3)
        assertEquals(3, results.size)
        assertTrue(results[0].score >= results[1].score)
        assertTrue(results[1].score >= results[2].score)
        assertEquals("exact", results[0].id)
    }

    @Test
    fun testSearch_TopKLimitsResults() = runBlocking {
        val docs = (1..10).map { i ->
            VectorDocument(id = "$i", vector = listOf(i.toFloat(), 0f))
        }
        store.upsertAll(docs)
        val results = store.search(listOf(1f, 0f), topK = 3)
        assertEquals(3, results.size)
    }

    @Test
    fun testSearch_EmptyStore_ReturnsEmpty() = runBlocking {
        val results = store.search(listOf(1f, 0f), topK = 5)
        assertTrue(results.isEmpty())
    }

    @Test
    fun testSearch_WithMetadataFilter_ReturnsOnlyMatching() = runBlocking {
        store.upsertAll(listOf(
            VectorDocument(id = "1", vector = listOf(1f, 0f), text = "a", metadata = mapOf("lang" to "kotlin")),
            VectorDocument(id = "2", vector = listOf(1f, 0f), text = "b", metadata = mapOf("lang" to "java")),
            VectorDocument(id = "3", vector = listOf(1f, 0f), text = "c", metadata = mapOf("lang" to "kotlin"))
        ))
        val results = store.search(listOf(1f, 0f), topK = 10, filter = mapOf("lang" to "kotlin"))
        assertEquals(2, results.size)
        assertTrue(results.all { it.metadata["lang"] == "kotlin" })
    }

    @Test
    fun testSearch_MultiKeyFilter_AllConditionsMustMatch() = runBlocking {
        store.upsertAll(listOf(
            VectorDocument(id = "1", vector = listOf(1f, 0f), metadata = mapOf("lang" to "kotlin", "env" to "prod")),
            VectorDocument(id = "2", vector = listOf(1f, 0f), metadata = mapOf("lang" to "kotlin", "env" to "dev")),
            VectorDocument(id = "3", vector = listOf(1f, 0f), metadata = mapOf("lang" to "java",   "env" to "prod"))
        ))
        val results = store.search(listOf(1f, 0f), topK = 10, filter = mapOf("lang" to "kotlin", "env" to "prod"))
        assertEquals(1, results.size)
        assertEquals("1", results.first().id)
    }

    @Test
    fun testSearch_EmptyFilter_ReturnsAll() = runBlocking {
        val docs = (1..4).map { i ->
            VectorDocument(id = "$i", vector = listOf(i.toFloat(), 0f), metadata = mapOf("tag" to "$i"))
        }
        store.upsertAll(docs)
        val results = store.search(listOf(1f, 0f), topK = 10, filter = emptyMap())
        assertEquals(4, results.size)
    }

    // --- delete ---

    @Test
    fun testDelete_ExistingDocument_RemovesIt() = runBlocking {
        store.upsert(VectorDocument(id = "1", vector = listOf(1f, 0f)))
        store.delete("1")
        assertEquals(0, store.size())
    }

    @Test
    fun testDelete_NonExistentId_NoOp() = runBlocking {
        store.upsert(VectorDocument(id = "1", vector = listOf(1f, 0f)))
        store.delete("nonexistent")
        assertEquals(1, store.size())
    }

    @Test
    fun testDeleteAll_ClearsStore() = runBlocking {
        val docs = (1..5).map { i -> VectorDocument(id = "$i", vector = listOf(i.toFloat(), 0f)) }
        store.upsertAll(docs)
        store.deleteAll()
        assertEquals(0, store.size())
        assertTrue(store.search(listOf(1f, 0f), topK = 5).isEmpty())
    }

    // --- metadata in results ---

    @Test
    fun testSearch_ResultContainsTextAndMetadata() = runBlocking {
        store.upsert(VectorDocument(
            id = "1",
            vector = listOf(1f, 0f),
            text = "sample text",
            metadata = mapOf("author" to "alice", "year" to "2024")
        ))
        val result = store.search(listOf(1f, 0f), topK = 1).first()
        assertEquals("sample text", result.text)
        assertEquals("alice", result.metadata["author"])
        assertEquals("2024", result.metadata["year"])
    }
}
