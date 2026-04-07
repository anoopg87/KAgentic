package vectorstore

/**
 * Represents a document stored in a vector database.
 *
 * @property id Unique identifier for this document.
 * @property vector The embedding vector representing the document's semantic content.
 * @property text Optional original text associated with this document.
 * @property metadata Arbitrary key-value metadata for filtering and retrieval.
 *
 * Usage Example:
 * ```kotlin
 * val doc = VectorDocument(
 *     id = "doc-001",
 *     vector = listOf(0.1f, 0.2f, 0.3f),
 *     text = "The quick brown fox jumps over the lazy dog",
 *     metadata = mapOf("source" to "wikipedia", "category" to "animals")
 * )
 * ```
 */
data class VectorDocument(
    val id: String,
    val vector: List<Float>,
    val text: String? = null,
    val metadata: Map<String, String> = emptyMap()
)
