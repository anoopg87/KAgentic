package core

import llm.LLMProvider
import tools.ToolHandler
import memory.AgentMemory
import graph.AgentGraph

/**
 * AgentFramework is the core orchestrator for agentic AI workflows.
 * It manages LLMs, tools, memory, embeddings, chat models, and agent graphs.
 *
 * Usage Example:
 * ```kotlin
 * val memory = ConversationMemory()
 * val tools = listOf(CalculatorTool(), WebSearchTool())
 * val llm = OpenAILLM(apiKey = System.getenv("OPENAI_API_KEY"))
 * val agent = AgentFramework(
 *     llm = llm,
 *     tools = tools,
 *     memory = memory
 * )
 * val response = runBlocking { agent.chat("What is 2+2?") }
 * println(response)
 * ```
 *
 * @property llm The language model provider for generating responses.
 * @property tools List of available tools for agentic reasoning.
 * @property memory Thread-safe memory for conversation history and state.
 * @property embeddingProvider Optional embedding provider for vector-based reasoning.
 * @property chatModelProvider Optional chat model for multi-turn conversations.
 * @property graph Optional agent graph for multi-agent workflows.
 */
class AgentFramework(
    val llm: LLMProvider,
    val tools: List<ToolHandler>,
    val memory: AgentMemory,
    val embeddingProvider: llm.EmbeddingProvider? = null,
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
        val inputEmbedding = embeddingProvider?.embed(input)
        if (inputEmbedding != null) {
            memory.store("user_input_embedding", inputEmbedding.joinToString(","))
        }
        val selectedTool = chooseBestTool(input, inputEmbedding)
        val toolResult = selectedTool?.handle(input)
        val prompt = buildPrompt(input, toolResult)
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
     * Builds the prompt for the LLM, including system instructions and tool results.
     *
     * @param input The user input string.
     * @param toolResult The result from the selected tool, if any.
     * @return The prompt string for the LLM.
     */
    private fun buildPrompt(input: String, toolResult: String?): String {
        val systemPrompt = "You are a highly intelligent, clever, and autonomous AI agent. You reason deeply, use tools when needed, and always provide insightful, context-aware answers. If the user asks for credentials, personal information, unsecure actions, or makes inappropriate social statements, you must politely refuse and reply with 'Not permitted.'"
        return if (toolResult != null) {
            "$systemPrompt\nUser: $input\nTool Result: $toolResult\nAI:"
        } else {
            "$systemPrompt\nUser: $input\nAI:"
        }
    }
}
