package vectorstore

/**
 * VectorStore defines the contract for all vector database backends.
 *
 * Implement this interface to plug in any vector database (Pinecone, Qdrant,
 * Weaviate, pgvector, Chroma, etc.) into the KAgentic framework.
 *
 * The framework ships with [InMemoryVectorStore] as a built-in implementation
 * suitable for development, testing, and small-scale use cases.
 *
 * Usage Example:
 * ```kotlin
 * val store: VectorStore = InMemoryVectorStore()
 *
 * // Index a document
 * store.upsert(VectorDocument(
 *     id = "doc-1",
 *     vector = embedder.embed("Hello world"),
 *     text = "Hello world",
 *     metadata = mapOf("source" to "manual")
 * ))
 *
 * // Search semantically
 * val results = store.search(
 *     query = embedder.embed("greeting"),
 *     topK = 3,
 *     filter = mapOf("source" to "manual")
 * )
 * ```
 *
 * ## Custom Implementation
 *
 * ```kotlin
 * class MyVectorStore : VectorStore {
 *     override suspend fun upsert(document: VectorDocument) { ... }
 *     override suspend fun upsertAll(documents: List<VectorDocument>) { ... }
 *     override suspend fun search(query: List<Float>, topK: Int, filter: Map<String, String>): List<VectorSearchResult> { ... }
 *     override suspend fun delete(id: String) { ... }
 *     override suspend fun deleteAll() { ... }
 * }
 * ```
 */
interface VectorStore {

    /**
     * Inserts or updates a single document in the vector store.
     * If a document with the same [VectorDocument.id] already exists, it is replaced.
     *
     * @param document The document to upsert.
     */
    suspend fun upsert(document: VectorDocument)

    /**
     * Inserts or updates a batch of documents in the vector store.
     * Prefer this over repeated [upsert] calls when indexing multiple documents,
     * as implementations may optimise batch operations.
     *
     * @param documents The list of documents to upsert.
     */
    suspend fun upsertAll(documents: List<VectorDocument>)

    /**
     * Performs a nearest-neighbour search over the vector store.
     *
     * @param query The query embedding vector to search with.
     * @param topK Maximum number of results to return.
     * @param filter Optional metadata filter — only documents whose metadata contains
     *   ALL provided key-value pairs are eligible. An empty map matches all documents.
     * @return A list of [VectorSearchResult] ordered by descending similarity score.
     */
    suspend fun search(
        query: List<Float>,
        topK: Int,
        filter: Map<String, String> = emptyMap()
    ): List<VectorSearchResult>

    /**
     * Deletes a single document by its ID.
     * No-ops if the document does not exist.
     *
     * @param id The ID of the document to delete.
     */
    suspend fun delete(id: String)

    /**
     * Deletes all documents from the vector store.
     * Use with care — this is irreversible.
     */
    suspend fun deleteAll()
}
