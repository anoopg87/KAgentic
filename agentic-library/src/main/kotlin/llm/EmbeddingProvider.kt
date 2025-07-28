/**
 * EmbeddingProvider defines the interface for generating vector embeddings from text.
 * Can be implemented using local or remote models.
 *
 * Usage Example:
 * ```kotlin
 * val embedder = SimpleEmbeddingProvider()
 * val embedding = runBlocking { embedder.embed("Hello world") }
 * println(embedding)
 * ```
 */
package llm

interface EmbeddingProvider {
    suspend fun embed(input: String): List<Float>
}
