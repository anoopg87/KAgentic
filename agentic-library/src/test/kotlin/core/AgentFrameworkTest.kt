import core.AgentFramework
import llm.SimpleChatModelProvider
import memory.ConversationMemory
import tools.CalculatorTool
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class AgentFrameworkTest {
    @Test
    fun testChatReturnsResponse() {
        val memory = ConversationMemory()
        val tools = listOf(CalculatorTool())
        val llm = object : llm.LLMProvider {
            override suspend fun generate(input: String): String = "LLM: $input"
        }
        val agent = AgentFramework(
            llm = llm,
            tools = tools,
            memory = memory,
            chatModelProvider = SimpleChatModelProvider()
        )
        val response = runBlocking { agent.chat("2+2") }
        assertTrue(response.isNotEmpty())
    }
}
