import core.AgentFramework
import graph.SimpleAgentGraph
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SimpleAgentGraphTest {
    @Test
    fun testRunChainsAgents() {
        val alwaysHandleTool = object : tools.ToolHandler {
            override fun canHandle(input: String) = true
            override suspend fun handle(input: String) = input
            override fun score(input: String) = 1
        }
        val agent1 = AgentFramework(
            llm = object : llm.LLMProvider {
                override suspend fun generate(input: String) = "A1: $input"
            },
            tools = listOf(alwaysHandleTool),
            memory = memory.ConversationMemory()
        )
        val agent2 = AgentFramework(
            llm = object : llm.LLMProvider {
                override suspend fun generate(input: String) = "A2: $input"
            },
            tools = listOf(alwaysHandleTool),
            memory = memory.ConversationMemory()
        )
        val graph = SimpleAgentGraph(listOf(agent1, agent2))
        val result = runBlocking { graph.run("start") }
        println("SimpleAgentGraphTest result: $result")
        println("SimpleAgentGraphTest result: $result")
    }
}
