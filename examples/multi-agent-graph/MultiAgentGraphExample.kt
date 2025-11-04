import core.AgentFramework
import llm.LLMProvider
import memory.ConversationMemory
import tools.CalculatorTool
import tools.WebSearchTool
import graph.SimpleAgentGraph
import kotlinx.coroutines.runBlocking

/**
 * Multi-Agent Graph Example
 *
 * This example demonstrates how to chain multiple agents together
 * to create a more complex workflow:
 * - Agent 1: Handles calculations
 * - Agent 2: Handles web searches
 * - Agent 3: Handles general questions
 */
fun main() = runBlocking {
    println("=== Multi-Agent Graph Example ===\n")

    // Create a mock LLM for demonstration (replace with real LLM in production)
    val mockLLM = object : LLMProvider {
        override suspend fun generate(input: String): String {
            return "Processed: $input"
        }
    }

    // Agent 1: Calculator specialist
    val calculatorAgent = AgentFramework(
        llm = mockLLM,
        tools = listOf(CalculatorTool()),
        memory = ConversationMemory()
    )

    // Agent 2: Web search specialist
    val searchAgent = AgentFramework(
        llm = mockLLM,
        tools = listOf(WebSearchTool()),
        memory = ConversationMemory()
    )

    // Agent 3: General purpose agent
    val generalAgent = AgentFramework(
        llm = mockLLM,
        tools = listOf(CalculatorTool(), WebSearchTool()),
        memory = ConversationMemory()
    )

    // Create the agent graph
    val graph = SimpleAgentGraph(
        agents = listOf(calculatorAgent, searchAgent, generalAgent)
    )

    // Example 1: Math calculation (should be handled by calculatorAgent)
    println("Query 1: 100 + 250 * 2")
    val response1 = graph.run("100 + 250 * 2")
    println("Response: $response1\n")

    // Example 2: Web search (should be handled by searchAgent)
    println("Query 2: search Kotlin coroutines")
    val response2 = graph.run("search Kotlin coroutines")
    println("Response: $response2\n")

    // Example 3: Complex workflow
    println("Query 3: calculate 50 * 3 then search the result")
    val response3 = graph.run("50 * 3")
    println("Response: $response3\n")

    println("=== Graph execution complete ===")
}
