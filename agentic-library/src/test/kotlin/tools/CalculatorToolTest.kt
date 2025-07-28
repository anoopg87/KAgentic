import tools.CalculatorTool
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class CalculatorToolTest {
    @Test
    fun testCanHandleMathExpression() {
        val tool = CalculatorTool()
        assertTrue(tool.canHandle("2+2"))
    }

    @Test
    fun testHandleReturnsResult() {
        val tool = CalculatorTool()
        val result = runBlocking { tool.handle("2+2") }
        assertTrue(result.contains("Result"))
    }
}
