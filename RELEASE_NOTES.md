# 🎉 KAgentic v0.1.0-alpha - Initial Alpha Release

This is the first alpha release of KAgentic, a modular Kotlin library for building agentic AI applications on the JVM.

**⚠️ Alpha Release Notice**: This is an alpha release. APIs may change in future versions. Please report any issues or feedback!

## ✨ What's New

### Core Features

- **AgentFramework** for orchestrating LLMs, tools, and memory
- **7 LLM Providers**: Gemini (Google), OpenAI (GPT), Claude (Anthropic), Ollama (local models), Cohere, Grok (xAI), DeepSeek
- **4 Built-in Tools**: CalculatorTool, WebSearchTool, FileReaderTool, APICallerTool
- **Thread-safe Memory**: ConversationMemory with concurrent access support using Mutex
- **Agent Graphs**: SimpleAgentGraph for sequential chaining, ConditionalAgentGraphBuilder for conditional routing
- **Plugin System**: Dynamic tool loading via ToolPluginLoader
- **Retry Logic**: Exponential backoff for network resilience (implemented in OpenAI, Gemini, and Claude providers)
- **Embedding & Chat Models**: Support for vector embeddings and multi-turn chat

### Documentation & Examples

- 📚 Comprehensive README with installation guide, quick start, and examples
- 📝 Full KDoc documentation for all public APIs
- 🎯 3 complete example projects:
  - Basic Agent: Simple agent with tools and memory
  - Multi-Agent Graph: Chaining multiple specialized agents
  - Custom Tools: Creating custom tools (DateTime, Weather, Translator)
- 📖 CONTRIBUTING.md with contribution guidelines
- 🔒 SECURITY.md with security best practices
- 📋 CHANGELOG.md for tracking version history

### Testing

- ✅ Comprehensive test coverage for core components
- ✅ LLM provider tests (all 7 providers)
- ✅ Tool tests (all 4 tools + ToolPluginLoader)
- ✅ Memory tests (ConversationMemory)
- ✅ Graph tests (SimpleAgentGraph)
- ✅ Mock implementations for testing without API calls

### Project Governance

- 📝 Professional issue templates (bug report, feature request, question)
- 📝 Pull request template with comprehensive checklists
- ✅ CI workflow with GitHub Actions
- ✅ Apache 2.0 License

## 🚀 Quick Start

### Installation

Add to your `build.gradle.kts`:

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/anoopg87/KAgentic")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("com.agentic:kotlin-agentic:0.1.0-alpha")
}
```

### Basic Usage

```kotlin
import core.AgentFramework
import llm.OpenAILLM
import memory.ConversationMemory
import tools.CalculatorTool
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val agent = AgentFramework(
        llm = OpenAILLM(apiKey = System.getenv("OPENAI_API_KEY")),
        tools = listOf(CalculatorTool()),
        memory = ConversationMemory()
    )

    val response = agent.chat("What is 25 * 4 + 100?")
    println(response)
}
```

## 📚 Documentation

- **README**: [https://github.com/anoopg87/KAgentic/blob/main/README.md](https://github.com/anoopg87/KAgentic/blob/main/README.md)
- **Examples**: [https://github.com/anoopg87/KAgentic/tree/main/examples](https://github.com/anoopg87/KAgentic/tree/main/examples)
- **CHANGELOG**: [https://github.com/anoopg87/KAgentic/blob/main/CHANGELOG.md](https://github.com/anoopg87/KAgentic/blob/main/CHANGELOG.md)
- **Contributing**: [https://github.com/anoopg87/KAgentic/blob/main/CONTRIBUTING.md](https://github.com/anoopg87/KAgentic/blob/main/CONTRIBUTING.md)
- **Security**: [https://github.com/anoopg87/KAgentic/blob/main/SECURITY.md](https://github.com/anoopg87/KAgentic/blob/main/SECURITY.md)

## 🎯 Example Projects

### Basic Agent

```kotlin
val agent = AgentFramework(
    llm = OpenAILLM(apiKey = System.getenv("OPENAI_API_KEY")),
    tools = listOf(CalculatorTool(), WebSearchTool()),
    memory = ConversationMemory()
)

val response = agent.chat("What is 25 * 4 + 100?")
```

### Multi-Agent Graph

```kotlin
val calculatorAgent = AgentFramework(llm, listOf(CalculatorTool()), memory)
val searchAgent = AgentFramework(llm, listOf(WebSearchTool()), memory)

val graph = SimpleAgentGraph(listOf(calculatorAgent, searchAgent))
val result = graph.run("Calculate 100 + 50 then search for the result")
```

### Custom Tool

```kotlin
class DateTimeTool : ToolHandler {
    override fun canHandle(input: String) = input.contains("time", ignoreCase = true)
    override fun score(input: String) = if (canHandle(input)) 10 else 1
    override suspend fun handle(input: String): String {
        return "Current time: ${LocalDateTime.now()}"
    }
}
```

## 🗺️ Roadmap

Future plans for KAgentic:

- [ ] **Maven Central Publishing**: Broader accessibility
- [ ] **More LLM Providers**: Mistral, Perplexity, additional providers
- [ ] **Vector Database Support**: Pinecone, Weaviate, Chroma integration
- [ ] **Streaming Responses**: Real-time response streaming
- [ ] **Advanced Memory**: Sliding window, summarization strategies
- [ ] **Production Monitoring**: Observability and tracing
- [ ] **Kotlin Multiplatform**: Support for JS, Native targets

## 🐛 Known Issues

- WebSearchTool uses DuckDuckGo API (demonstration only, may be rate-limited)
- No built-in rate limiting (should be implemented by users)
- Limited error recovery beyond retry logic

## 💡 Feedback Welcome

This is an alpha release, and we'd love your feedback:

- 🐛 **Report bugs**: [GitHub Issues](https://github.com/anoopg87/KAgentic/issues)
- 💡 **Suggest features**: [GitHub Issues](https://github.com/anoopg87/KAgentic/issues)
- 💬 **Ask questions**: [GitHub Discussions](https://github.com/anoopg87/KAgentic/discussions)
- 🤝 **Contribute**: See [CONTRIBUTING.md](https://github.com/anoopg87/KAgentic/blob/main/CONTRIBUTING.md)

## 🤝 Contributing

Contributions are welcome! See [CONTRIBUTING.md](https://github.com/anoopg87/KAgentic/blob/main/CONTRIBUTING.md) for guidelines.

Areas that need help:
- Additional LLM provider integrations
- More built-in tools
- Vector database support
- Documentation improvements
- Example projects

## 📄 License

Apache License 2.0 - see [LICENSE](https://github.com/anoopg87/KAgentic/blob/main/LICENSE)

## 🙏 Acknowledgments

- Inspired by [LangChain](https://github.com/langchain-ai/langchain) and [LangGraph](https://github.com/langchain-ai/langgraph)
- Built with Kotlin and Ktor
- Thanks to all early contributors and testers

## 📊 Project Stats

- **Lines of Code**: ~5,000+
- **Test Files**: 9
- **LLM Providers**: 7
- **Built-in Tools**: 4
- **Example Projects**: 3
- **Documentation Files**: 6

---

**Full Changelog**: [CHANGELOG.md](https://github.com/anoopg87/KAgentic/blob/main/CHANGELOG.md)

**Download**: [GitHub Packages](https://github.com/anoopg87/KAgentic/packages)

Made with ❤️ for the Kotlin community
