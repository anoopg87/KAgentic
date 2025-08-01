/**
 * SimpleEmbeddingProvider is a dummy implementation of EmbeddingProvider.
 * Returns a random vector for demonstration purposes.
 *
 * Usage Example:
 * ```kotlin
 * val embedder = SimpleEmbeddingProvider()
 * val embedding = runBlocking { embedder.embed("Hello world") }
 * println(embedding)
 * ```
 */
package llm

import kotlin.random.Random

class SimpleEmbeddingProvider : EmbeddingProvider {
    override suspend fun embed(input: String): List<Float> {
        // Dummy implementation: returns a fixed-size random vector for demonstration
        return List(128) { Random.nextFloat() }
    }
}
