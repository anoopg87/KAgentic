import memory.ConversationMemory
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ConversationMemoryTest {
    @Test
    fun testStoreAndRetrieveHistory() {
        val memory = ConversationMemory()
        runBlocking { memory.store("user_input", "Hello") }
        runBlocking { memory.store("agent_response", "Hi!") }
        val history = runBlocking { memory.retrieve("history") }
        assertEquals("User: Hello\nAgent: Hi!", history)
    }
}
