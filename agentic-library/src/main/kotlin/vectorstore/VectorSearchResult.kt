package vectorstore

/**
 * Represents a single result returned from a vector similarity search.
 *
 * @property id The unique identifier of the matched document.
 * @property score Similarity score (higher = more similar). Range depends on metric:
 *   cosine similarity produces values in [-1.0, 1.0], typically [0.0, 1.0] for embeddings.
 * @property text The original text of the matched document, if stored.
 * @property metadata The metadata associated with the matched document.
 *
 * Usage Example:
 * ```kotlin
 * val results: List<VectorSearchResult> = vectorStore.search(queryVector, topK = 5)
 * results.forEach { result ->
 *     println("${result.id} (score=${result.score}): ${result.text}")
 * }
 * ```
 */
data class VectorSearchResult(
    val id: String,
    val score: Float,
    val text: String? = null,
    val metadata: Map<String, String> = emptyMap()
)
