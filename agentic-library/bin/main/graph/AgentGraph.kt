/**
 * AgentGraph defines the interface for multi-agent workflows.
 * Implementations can chain, branch, or conditionally route agent calls.
 *
 * Usage Example:
 * ```kotlin
 * val graph = SimpleAgentGraph(listOf(agent1, agent2))
 * val result = runBlocking { graph.run("Start task") }
 * println(result)
 * ```
 */
package graph

interface AgentGraph {
    suspend fun run(input: String): String
}
