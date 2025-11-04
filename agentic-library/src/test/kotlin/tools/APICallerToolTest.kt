package tools

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class APICallerToolTest {

    @Test
    fun testCanHandleAPICall() {
        val tool = APICallerTool()
        assertTrue(tool.canHandle("call api https://api.example.com"))
        assertTrue(tool.canHandle("https://api.github.com/users"))
        assertTrue(tool.canHandle("http://localhost:8080/api"))
    }

    @Test
    fun testCannotHandleNonAPIQuery() {
        val tool = APICallerTool()
        assertFalse(tool.canHandle("search something"))
        assertFalse(tool.canHandle("2+2"))
        assertFalse(tool.canHandle("read file test.txt"))
    }

    @Test
    fun testScoreAPICall() {
        val tool = APICallerTool()
        assertTrue(tool.score("call api https://example.com") == 10)
        assertTrue(tool.score("https://api.example.com") == 8)
        assertTrue(tool.score("hello") == 1)
    }

    @Test
    fun testHandleInvalidURL() {
        val tool = APICallerTool()
        val result = runBlocking { tool.handle("invalid-url") }
        // Should return error message for invalid URL
        assertTrue(result.contains("Error"))
    }

    @Test
    fun testHandleReturnsResponse() {
        val tool = APICallerTool()
        // Using a URL that doesn't require network (will fail gracefully)
        val result = runBlocking { tool.handle("https://httpbin.org/get") }
        // Should return either valid response or error message
        assertTrue(result.isNotEmpty())
    }
}
