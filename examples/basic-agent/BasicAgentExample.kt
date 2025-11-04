import core.AgentFramework
import llm.OpenAILLM
import memory.ConversationMemory
import tools.CalculatorTool
import tools.WebSearchTool
import kotlinx.coroutines.runBlocking

/**
 * Basic Agent Example
 *
 * This example demonstrates how to create a simple agent with:
 * - An LLM (OpenAI)
 * - Multiple tools (Calculator and WebSearch)
 * - Memory for conversation history
 */
fun main() = runBlocking {
    // Step 1: Set up memory
    val memory = ConversationMemory()

    // Step 2: Define available tools
    val tools = listOf(
        CalculatorTool(),
        WebSearchTool()
    )

    // Step 3: Initialize the LLM
    // Note: You need to set the OPENAI_API_KEY environment variable
    val apiKey = System.getenv("OPENAI_API_KEY") ?: throw IllegalStateException(
        "Please set OPENAI_API_KEY environment variable"
    )
    val llm = OpenAILLM(apiKey = apiKey, model = "gpt-3.5-turbo")

    // Step 4: Create the agent
    val agent = AgentFramework(
        llm = llm,
        tools = tools,
        memory = memory
    )

    // Step 5: Use the agent
    println("=== Basic Agent Example ===\n")

    // Example 1: Math calculation
    println("Query 1: What is 25 * 4 + 100?")
    val response1 = agent.chat("25 * 4 + 100")
    println("Response: $response1\n")

    // Example 2: Web search
    println("Query 2: search latest Kotlin features")
    val response2 = agent.chat("search latest Kotlin features")
    println("Response: $response2\n")

    // Example 3: General question (no tool needed)
    println("Query 3: What is an agentic AI system?")
    val response3 = agent.chat("What is an agentic AI system?")
    println("Response: $response3\n")

    // Step 6: View conversation history
    println("=== Conversation History ===")
    val history = memory.getHistory()
    history.forEach { exchange ->
        println("User: ${exchange.userInput}")
        println("Agent: ${exchange.agentResponse}")
        println("---")
    }
}
