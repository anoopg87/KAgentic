package examples

import core.AgentFramework
import llm.OpenAILLM  // or GeminiLLM, ClaudeLLM, etc.
import memory.ConversationMemory
import tools.*
import graph.ConditionalAgentGraphBuilder
import kotlinx.coroutines.runBlocking

/**
 * Conditional Agent Graph Template
 *
 * This template creates a dynamic workflow where routing between agents
 * depends on the output of previous agents (conditional branching).
 *
 * Use this when:
 * - Workflow path depends on agent responses
 * - Need decision trees or branching logic
 * - Different inputs require different processing
 *
 * Flow: Input → AgentA → (condition?) → AgentB or AgentC → ...
 */
fun main() = runBlocking {
    // Configuration
    val apiKey = System.getenv("OPENAI_API_KEY")  // or GEMINI_API_KEY, CLAUDE_API_KEY, etc.

    // Step 1: Create specialized agents
    // Each agent has a specific role in the workflow

    // Triage/Router agent - makes initial decisions
    val triageAgent = AgentFramework(
        llm = OpenAILLM(apiKey = apiKey),
        tools = listOf(
            // TODO: Add tools for classification/routing
            // Usually lightweight or no tools
        ),
        memory = ConversationMemory()
    )

    // Branch A agent - handles one type of request
    val branchAAgent = AgentFramework(
        llm = OpenAILLM(apiKey = apiKey),
        tools = listOf(
            // TODO: Add tools specific to branch A
            // Example: FileReaderTool(), WebSearchTool()
        ),
        memory = ConversationMemory()
    )

    // Branch B agent - handles another type of request
    val branchBAgent = AgentFramework(
        llm = OpenAILLM(apiKey = apiKey),
        tools = listOf(
            // TODO: Add tools specific to branch B
            // Example: CalculatorTool(), APICallerTool()
        ),
        memory = ConversationMemory()
    )

    // Fallback/default agent - handles edge cases
    val fallbackAgent = AgentFramework(
        llm = OpenAILLM(apiKey = apiKey),
        tools = listOf(
            // TODO: Add general-purpose tools
        ),
        memory = ConversationMemory()
    )

    // Step 2: Build the conditional graph
    val builder = ConditionalAgentGraphBuilder()

    // Define nodes
    builder.addNode("triage", triageAgent)
           .addNode("branchA", branchAAgent)
           .addNode("branchB", branchBAgent)
           .addNode("fallback", fallbackAgent)

    // Define conditional edges
    // Edge: from → to when condition is true
    builder.addEdge("triage", "branchA") { response ->
        // TODO: Replace with your actual condition
        response.contains("route_to_A", ignoreCase = true) ||
        response.contains("option_A", ignoreCase = true)
    }

    builder.addEdge("triage", "branchB") { response ->
        // TODO: Replace with your actual condition
        response.contains("route_to_B", ignoreCase = true) ||
        response.contains("option_B", ignoreCase = true)
    }

    builder.addEdge("triage", "fallback") { response ->
        // TODO: Replace with your actual condition
        // Typically: catch all that doesn't match other conditions
        !response.contains("route_to_A", ignoreCase = true) &&
        !response.contains("route_to_B", ignoreCase = true)
    }

    // Optional: Add multi-step flows
    // builder.addEdge("branchA", "next_step") { response ->
    //     response.contains("continue", ignoreCase = true)
    // }

    // Build the graph with starting node
    val graph = builder.build(startId = "triage")

    // Step 3: Execute the graph
    val input = "TODO: Replace with your actual input"
    println("Input: $input")
    println("Processing through conditional graph...")

    val result = graph.run(input)

    println("\nFinal Result:")
    println(result)
}

/**
 * Example Configurations:
 *
 * 1. Customer Support Router:
 *    triage → technical (if contains "technical")
 *          → billing (if contains "billing")
 *          → general (otherwise)
 *
 * 2. Data Processing Router:
 *    validate → process_numeric (if contains "number")
 *            → process_text (if contains "text")
 *            → error_handler (if contains "error")
 *
 * 3. Content Analysis Router:
 *    analyze → fact_check (if needs verification)
 *           → summarize (if needs summary)
 *           → expand (if needs more detail)
 *
 * 4. Multi-Step Workflow:
 *    step1 → step2A (if condition met)
 *          → step2B (if different condition)
 *    step2A → step3 (final processing)
 *    step2B → step3 (final processing)
 */

/**
 * Condition Design Tips:
 *
 * 1. Use clear, specific keywords:
 *    ✅ response.contains("technical_issue")
 *    ❌ response.length > 10
 *
 * 2. Use case-insensitive matching:
 *    ✅ response.contains("keyword", ignoreCase = true)
 *
 * 3. Combine conditions logically:
 *    ✅ contains("A") && !contains("B")
 *    ✅ contains("A") || contains("B")
 *
 * 4. Have a catch-all fallback:
 *    ✅ Last edge: { response -> true }
 *    ✅ Or check for absence of other conditions
 *
 * 5. Avoid circular logic:
 *    ❌ A → B → A (infinite loop)
 *    ✅ A → B → C (terminal)
 */
