/**
 * SimpleAgentGraphBuilder constructs a linear agent graph for sequential workflows.
 *
 * Usage Example:
 * ```kotlin
 * val builder = SimpleAgentGraphBuilder()
 * builder.addAgent(agent1).addAgent(agent2)
 * val graph = builder.build()
 * val result = runBlocking { graph.run("Start task") }
 * println(result)
 * ```
 */
/**
 * SimpleAgentGraphBuilder constructs a linear agent graph for sequential workflows.
 *
 * Usage Example:
 * ```kotlin
 * val builder = SimpleAgentGraphBuilder()
 * builder.addAgent(agent1).addAgent(agent2)
 * val graph = builder.build()
 * val result = runBlocking { graph.run("Start task") }
 * println(result)
 * ```
 */
package graph

import core.AgentFramework

class SimpleAgentGraphBuilder {
    private val agents = mutableListOf<AgentFramework>()

    fun addAgent(agent: AgentFramework): SimpleAgentGraphBuilder {
        agents.add(agent)
        return this
    }

    fun build(): AgentGraph {
        return SimpleAgentGraph(agents)
    }
}
