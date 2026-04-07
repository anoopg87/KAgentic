package vectorstore

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.sqrt

/**
 * A thread-safe, in-memory implementation of [VectorStore] using cosine similarity.
 *
 * Suitable for development, testing, and small-scale production workloads where
 * persistence is not required. For large-scale or persistent use cases, prefer
 * a dedicated vector database backend (Pinecone, Qdrant, etc.).
 *
 * Thread safety is guaranteed via a [Mutex] — all reads and writes are serialised.
 *
 * Similarity metric: **cosine similarity** — values range from -1.0 (opposite) to
 * 1.0 (identical direction), with typical embedding results in [0.0, 1.0].
 *
 * Usage Example:
 * ```kotlin
 * val store = InMemoryVectorStore()
 *
 * runBlocking {
 *     store.upsert(VectorDocument(
 *         id = "1",
 *         vector = listOf(0.1f, 0.9f, 0.2f),
 *         text = "Kotlin is a modern JVM language",
 *         metadata = mapOf("lang" to "kotlin")
 *     ))
 *
 *     val results = store.search(
 *         query = listOf(0.15f, 0.85f, 0.25f),
 *         topK = 5,
 *         filter = mapOf("lang" to "kotlin")
 *     )
 *     println(results)
 * }
 * ```
 */
class InMemoryVectorStore : VectorStore {

    private val store = mutableMapOf<String, VectorDocument>()
    private val mutex = Mutex()

    override suspend fun upsert(document: VectorDocument) {
        mutex.withLock {
            store[document.id] = document
        }
    }

    override suspend fun upsertAll(documents: List<VectorDocument>) {
        mutex.withLock {
            documents.forEach { store[it.id] = it }
        }
    }

    override suspend fun search(
        query: List<Float>,
        topK: Int,
        filter: Map<String, String>
    ): List<VectorSearchResult> {
        require(topK > 0) { "topK must be greater than 0" }

        return mutex.withLock {
            store.values
                .filter { doc -> matchesFilter(doc, filter) }
                .mapNotNull { doc ->
                    val score = cosineSimilarity(query, doc.vector)
                    if (score.isNaN()) null
                    else VectorSearchResult(
                        id = doc.id,
                        score = score,
                        text = doc.text,
                        metadata = doc.metadata
                    )
                }
                .sortedByDescending { it.score }
                .take(topK)
        }
    }

    override suspend fun delete(id: String) {
        mutex.withLock {
            store.remove(id)
        }
    }

    override suspend fun deleteAll() {
        mutex.withLock {
            store.clear()
        }
    }

    /**
     * Returns the number of documents currently stored.
     * Useful for diagnostics and testing.
     */
    suspend fun size(): Int = mutex.withLock { store.size }

    // --- Private helpers ---

    /**
     * Returns true if the document's metadata contains all filter key-value pairs.
     * An empty filter always matches.
     */
    private fun matchesFilter(doc: VectorDocument, filter: Map<String, String>): Boolean {
        if (filter.isEmpty()) return true
        return filter.all { (key, value) -> doc.metadata[key] == value }
    }

    /**
     * Computes cosine similarity between two vectors.
     *
     * cosine_similarity(a, b) = dot(a, b) / (||a|| * ||b||)
     *
     * Returns 0.0 if either vector is a zero vector.
     */
    private fun cosineSimilarity(a: List<Float>, b: List<Float>): Float {
        require(a.size == b.size) {
            "Vector dimension mismatch: query has ${a.size} dimensions, document has ${b.size} dimensions"
        }

        var dot = 0.0
        var normA = 0.0
        var normB = 0.0

        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }

        val denom = sqrt(normA) * sqrt(normB)
        return if (denom == 0.0) 0f else (dot / denom).toFloat()
    }
}
