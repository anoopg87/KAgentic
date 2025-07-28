package llm

interface LLMProvider {
    suspend fun generate(input: String): String
}
