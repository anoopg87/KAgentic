/**
 * ConditionalAgentGraphBuilder constructs complex agent graphs with conditional routing.
 * Supports branching and dynamic workflows.
 *
 * Usage Example:
 * ```kotlin
 * val builder = ConditionalAgentGraphBuilder()
 * builder.addNode("A", agentA)
 *        .addNode("B", agentB)
 *        .addEdge("A", "B") { response -> response.contains("next") }
 * val graph = builder.build("A")
 * val result = runBlocking { graph.run("Start task") }
 * println(result)
 * ```
 */
/**
 * ConditionalAgentGraphBuilder constructs complex agent graphs with conditional routing.
 * Supports branching and dynamic workflows.
 *
 * Usage Example:
 * ```kotlin
 * val builder = ConditionalAgentGraphBuilder()
 * builder.addNode("A", agentA)
 *        .addNode("B", agentB)
 *        .addEdge("A", "B") { response -> response.contains("next") }
 * val graph = builder.build("A")
 * val result = runBlocking { graph.run("Start task") }
 * println(result)
 * ```
 */
package graph

import core.AgentFramework

class ConditionalAgentGraphBuilder {
    private val nodes = mutableListOf<Node>()
    private val edges = mutableListOf<Edge>()

    data class Node(val id: String, val agent: AgentFramework)
    data class Edge(val from: String, val to: String, val condition: (String) -> Boolean)

    fun addNode(id: String, agent: AgentFramework): ConditionalAgentGraphBuilder {
        nodes.add(Node(id, agent))
        return this
    }

    fun addEdge(from: String, to: String, condition: (String) -> Boolean): ConditionalAgentGraphBuilder {
        edges.add(Edge(from, to, condition))
        return this
    }

    fun build(startId: String): AgentGraph {
        return object : AgentGraph {
            override suspend fun run(input: String): String {
                var currentId = startId
                var currentInput = input
                val nodeMap = nodes.associateBy { it.id }
                while (true) {
                    val node = nodeMap[currentId] ?: break
                    val response = node.agent.chat(currentInput)
                    val nextEdge = edges.firstOrNull { it.from == currentId && it.condition(response) } ?: return response
                    currentId = nextEdge.to
                    currentInput = response
                }
                return currentInput
            }
        }
    }
}
