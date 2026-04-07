package examples

import core.AgentFramework
import llm.OpenAILLM  // or GeminiLLM, ClaudeLLM, etc.
import memory.ConversationMemory
import tools.*
import graph.SimpleAgentGraphBuilder
import kotlinx.coroutines.runBlocking

/**
 * Simple Sequential Agent Graph Template
 *
 * This template creates a linear workflow where agents execute in sequence.
 * Each agent processes the output of the previous agent.
 *
 * Use this when:
 * - You need sequential processing
 * - Each step builds on the previous one
 * - The workflow path is always the same
 *
 * Flow: Input → Agent1 → Agent2 → Agent3 → Output
 */
fun main() = runBlocking {
    // Configuration
    val apiKey = System.getenv("OPENAI_API_KEY")  // or GEMINI_API_KEY, CLAUDE_API_KEY, etc.

    // Step 1: Create specialized agents
    // Each agent should have specific tools and responsibilities

    val agent1 = AgentFramework(
        llm = OpenAILLM(apiKey = apiKey),
        tools = listOf(
            // TODO: Add tools for agent1's specific role
            // Examples: FileReaderTool(), WebSearchTool()
        ),
        memory = ConversationMemory()
    )

    val agent2 = AgentFramework(
        llm = OpenAILLM(apiKey = apiKey),
        tools = listOf(
            // TODO: Add tools for agent2's specific role
            // Examples: CalculatorTool(), APICallerTool()
        ),
        memory = ConversationMemory()
    )

    val agent3 = AgentFramework(
        llm = OpenAILLM(apiKey = apiKey),
        tools = listOf(
            // TODO: Add tools for agent3's specific role
            // Examples: (none for summarization)
        ),
        memory = ConversationMemory()
    )

    // Step 2: Build the sequential graph
    val graph = SimpleAgentGraphBuilder()
        .addAgent(agent1)  // First in sequence
        .addAgent(agent2)  // Second in sequence
        .addAgent(agent3)  // Third in sequence
        .build()

    // Step 3: Execute the graph
    val input = "TODO: Replace with your actual input"
    println("Input: $input")
    println("Processing through agent graph...")

    val result = graph.run(input)

    println("\nFinal Result:")
    println(result)
}

/**
 * Example Configurations:
 *
 * 1. Data Processing Pipeline:
 *    Agent1 (FileReaderTool, WebSearchTool) → fetches data
 *    Agent2 (CalculatorTool) → processes data
 *    Agent3 (no tools) → generates report
 *
 * 2. Content Creation Pipeline:
 *    Agent1 (WebSearchTool) → researches topic
 *    Agent2 (no tools) → writes content
 *    Agent3 (no tools) → edits and polishes
 *
 * 3. Analysis Pipeline:
 *    Agent1 (FileReaderTool) → loads data
 *    Agent2 (WebSearchTool) → enriches with external data
 *    Agent3 (CalculatorTool) → performs analysis
 */
