package tools

// Plugin loader for YAML/OpenAPI tools
interface ToolPluginLoader {
    fun loadPlugin(config: String): ToolHandler
}

class ToolPluginLoaderImpl : ToolPluginLoader {
    override fun loadPlugin(config: String): ToolHandler {
        return when (config.lowercase()) {
            "calculator" -> CalculatorTool()
            "websearch" -> WebSearchTool()
            "filereader" -> FileReaderTool()
            "apicaller" -> APICallerTool()
            else -> object : ToolHandler {
                override suspend fun handle(input: String): String = "Unknown tool: $config"
            }
        }
    }
}
