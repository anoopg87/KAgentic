/**
 * SimpleAgentGraph chains multiple agents in sequence.
 * Each agent receives the previous agent's output as input.
 *
 * Usage Example:
 * ```kotlin
 * val graph = SimpleAgentGraph(listOf(agent1, agent2))
 * val result = runBlocking { graph.run("Start task") }
 * println(result)
 * ```
 */

package graph

import core.AgentFramework
import tools.ToolHandler

class SimpleAgentGraph(private val agents: List<AgentFramework>) : AgentGraph {
    override suspend fun run(input: String): String {
        var currentInput = input
        val maxSteps = 10 // Prevent infinite loops
        var steps = 0
        var lastAgent: AgentFramework? = null
        while (steps < maxSteps) {
            // Each agent decides if it can handle the current input
            val agent = agents.firstOrNull { agent -> agent.tools.any { tool: ToolHandler -> tool.canHandle(currentInput) } }
                ?: // No agent can handle, break
                break
            lastAgent = agent
            val response = agent.chat(currentInput)
            // If response is terminal (no further agent can handle), return
             agents.firstOrNull { next -> next != agent && next.tools.any { tool: ToolHandler -> tool.canHandle(response) } }
                ?: return response
            currentInput = response
            steps++
        }
        // Fallback: return last response or input
        return lastAgent?.chat(currentInput) ?: currentInput
    }
}
