package tools

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class WebSearchToolTest {

    @Test
    fun testCanHandleSearchQuery() {
        val tool = WebSearchTool()
        assertTrue(tool.canHandle("search Kotlin programming"))
        assertTrue(tool.canHandle("find information about AI"))
        assertTrue(tool.canHandle("SEARCH machine learning"))
    }

    @Test
    fun testCannotHandleNonSearchQuery() {
        val tool = WebSearchTool()
        assertFalse(tool.canHandle("2+2"))
        assertFalse(tool.canHandle("read file test.txt"))
    }

    @Test
    fun testScoreSearchQuery() {
        val tool = WebSearchTool()
        assertTrue(tool.score("search Kotlin") == 10)
        assertTrue(tool.score("find AI") == 7)
        assertTrue(tool.score("hello world") == 1)
    }

    @Test
    fun testHandleReturnsResponse() {
        val tool = WebSearchTool()
        val result = runBlocking { tool.handle("search test query") }
        // Should return either valid response or error message
        assertTrue(result.isNotEmpty())
    }
}
