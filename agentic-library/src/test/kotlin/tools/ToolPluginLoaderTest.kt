package tools

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class ToolPluginLoaderTest {

    @Test
    fun testLoadCalculatorPlugin() {
        val loader = ToolPluginLoaderImpl()
        val tool = loader.loadPlugin("calculator")
        assertNotNull(tool)
        assertTrue(tool is CalculatorTool)
    }

    @Test
    fun testLoadWebSearchPlugin() {
        val loader = ToolPluginLoaderImpl()
        val tool = loader.loadPlugin("websearch")
        assertNotNull(tool)
        assertTrue(tool is WebSearchTool)
    }

    @Test
    fun testLoadFileReaderPlugin() {
        val loader = ToolPluginLoaderImpl()
        val tool = loader.loadPlugin("filereader")
        assertNotNull(tool)
        assertTrue(tool is FileReaderTool)
    }

    @Test
    fun testLoadAPICallerPlugin() {
        val loader = ToolPluginLoaderImpl()
        val tool = loader.loadPlugin("apicaller")
        assertNotNull(tool)
        assertTrue(tool is APICallerTool)
    }

    @Test
    fun testLoadUnknownPlugin() {
        val loader = ToolPluginLoaderImpl()
        val tool = loader.loadPlugin("unknown")
        assertNotNull(tool)
        // Should return a tool that can't handle anything
        assertTrue(!tool.canHandle("test"))
    }

    @Test
    fun testLoadPluginCaseInsensitive() {
        val loader = ToolPluginLoaderImpl()
        val tool1 = loader.loadPlugin("CALCULATOR")
        val tool2 = loader.loadPlugin("Calculator")
        val tool3 = loader.loadPlugin("calculator")

        assertNotNull(tool1)
        assertNotNull(tool2)
        assertNotNull(tool3)
        assertTrue(tool1 is CalculatorTool)
        assertTrue(tool2 is CalculatorTool)
        assertTrue(tool3 is CalculatorTool)
    }
}
