package core

import llm.LLMProvider
import tools.ToolHandler
import memory.AgentMemory
import graph.AgentGraph
import vectorstore.VectorStore

/**
 * AgentFramework is the core orchestrator for agentic AI workflows.
 * It manages LLMs, tools, memory, embeddings, vector stores, chat models, and agent graphs.
 *
 * When both [embeddingProvider] and [vectorStore] are provided, the framework automatically
 * performs Retrieval-Augmented Generation (RAG): it embeds the user input, retrieves the
 * most relevant documents from the vector store, and injects them into the LLM prompt as
 * grounding context.
 *
 * Usage Example (basic):
 * ```kotlin
 * val agent = AgentFramework(
 *     llm = OpenAILLM(apiKey = System.getenv("OPENAI_API_KEY")),
 *     tools = listOf(CalculatorTool()),
 *     memory = ConversationMemory()
 * )
 * val response = runBlocking { agent.chat("What is 2+2?") }
 * ```
 *
 * Usage Example (with RAG):
 * ```kotlin
 * val embedder = OpenAIEmbeddingProvider(apiKey = System.getenv("OPENAI_API_KEY"))
 * val store = InMemoryVectorStore()
 * // Pre-index your documents into store...
 *
 * val agent = AgentFramework(
 *     llm = OpenAILLM(apiKey = System.getenv("OPENAI_API_KEY")),
 *     tools = listOf(),
 *     memory = ConversationMemory(),
 *     embeddingProvider = embedder,
 *     vectorStore = store,
 *     ragTopK = 3
 * )
 * val response = runBlocking { agent.chat("What is KAgentic?") }
 * ```
 *
 * @property llm The language model provider for generating responses.
 * @property tools List of available tools for agentic reasoning.
 * @property memory Thread-safe memory for conversation history and state.
 * @property embeddingProvider Optional embedding provider. Required for RAG and semantic tool selection.
 * @property vectorStore Optional vector store for RAG context retrieval.
 * @property ragTopK Number of documents to retrieve from the vector store per query (default 3).
 * @property chatModelProvider Optional chat model for multi-turn conversations.
 * @property graph Optional agent graph for multi-agent workflows.
 */
class AgentFramework(
    val llm: LLMProvider,
    val tools: List<ToolHandler>,
    val memory: AgentMemory,
    val embeddingProvider: llm.EmbeddingProvider? = null,
    val vectorStore: VectorStore? = null,
    val ragTopK: Int = 3,
    val chatModelProvider: llm.ChatModelProvider? = null,
    val graph: AgentGraph? = null
) {
    /**
     * Main entrypoint for agentic chat.
     * Stores input, selects best tool, builds prompt, and generates response.
     * Delegates to agent graph if present.
     *
     * @param input The user input string.
     * @return The agent's response string.
     * @throws Exception if any tool or LLM fails.
     */
    suspend fun chat(input: String): String {
        // If graph is present, delegate to graph for multi-agent workflow
        if (graph != null) {
            return graph.run(input)
        }
        // Otherwise, run single agent logic
        memory.store("user_input", input)

        // Embed the input once — reused for both RAG retrieval and tool selection
        val inputEmbedding = embeddingProvider?.embed(input)

        // RAG: retrieve relevant context from vector store when both are configured
        val ragContext = if (vectorStore != null && inputEmbedding != null) {
            val results = vectorStore.search(query = inputEmbedding, topK = ragTopK)
            results.mapNotNull { it.text }.joinToString("\n---\n").takeIf { it.isNotBlank() }
        } else null

        val selectedTool = chooseBestTool(input, inputEmbedding)
        val toolResult = selectedTool?.handle(input)
        val prompt = buildPrompt(input, toolResult, ragContext)
        val response = if (chatModelProvider != null) {
            // Use chatModelProvider for multi-turn chat
            val history = memory.retrieve("history")?.split("\n") ?: listOf()
            chatModelProvider.chat(history + listOf(prompt))
        } else {
            llm.generate(prompt)
        }
        memory.store("agent_response", response)
        return response
    }

    /**
     * Selects the best tool for the given input using score-based ranking.
     *
     * @param input The user input string.
     * @param inputEmbedding Optional embedding vector for advanced selection.
     * @return The best ToolHandler or null if none can handle the input.
     */
    private fun chooseBestTool(input: String, inputEmbedding: List<Float>?): ToolHandler? {
        return tools
            .filter { it.canHandle(input) }
            .maxByOrNull { tool -> tool.score(input) }
    }

    /**
     * Builds the prompt for the LLM, including system instructions, RAG context, and tool results.
     *
     * @param input The user input string.
     * @param toolResult The result from the selected tool, if any.
     * @param ragContext Relevant document context retrieved from the vector store, if any.
     * @return The prompt string for the LLM.
     */
    private fun buildPrompt(input: String, toolResult: String?, ragContext: String?): String {
        val systemPrompt = "You are a highly intelligent, clever, and autonomous AI agent. You reason deeply, use tools when needed, and always provide insightful, context-aware answers. If the user asks for credentials, personal information, unsecure actions, or makes inappropriate social statements, you must politely refuse and reply with 'Not permitted.'"
        return buildString {
            append(systemPrompt)
            if (ragContext != null) {
                append("\n\nRelevant Context:\n$ragContext")
            }
            append("\nUser: $input")
            if (toolResult != null) {
                append("\nTool Result: $toolResult")
            }
            append("\nAI:")
        }
    }
}
