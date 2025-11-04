# KAgentic: Agentic AI Framework for Kotlin

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.23-purple.svg)](https://kotlinlang.org/)
[![JVM](https://img.shields.io/badge/JVM-17+-orange.svg)](https://www.oracle.com/java/)
[![Version](https://img.shields.io/badge/version-0.1.0--alpha-green.svg)](https://github.com/anoopg87/KAgentic)

A modular, production-ready Kotlin library for building agentic AI applications on the JVM.

**Inspired by [LangChain](https://github.com/langchain-ai/langchain) and [LangGraph](https://github.com/langchain-ai/langgraph),** KAgentic brings agentic design patterns, graph-based orchestration, and tool/LLM extensibility to the Kotlin ecosystem.

## ✨ Features

- 🤖 **Extensible Agent Orchestration**: Compose agents, tools, and LLMs into flexible workflows
- 🧠 **Multiple LLM Support**: Integrate Gemini, OpenAI, Claude, Ollama, Cohere, Grok, DeepSeek
- 🛠️ **Pluggable Tools**: Calculator, web search, file reader, API caller, and custom tools
- 🕸️ **Agent Graphs**: Chain, branch, and conditionally route agent calls for complex reasoning
- 💾 **Memory Management**: Thread-safe conversation memory for context retention
- 🔁 **Retry Logic**: Built-in exponential backoff for transient network errors
- 📝 **Comprehensive Documentation**: Full KDoc comments and example projects
- ✅ **Well-Tested**: Extensive test coverage for core components

## 🚀 Quick Start

### Installation

Add the library to your Gradle project:

```kotlin
// build.gradle.kts
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

### Basic Example

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

### Prerequisites

- JDK 17 or higher
- Gradle 8.2 or higher
- API keys for LLM providers (OpenAI, Gemini, Claude, etc.)

---

## Key Components & Usage Examples

### AgentFramework
```kotlin
val memory = ConversationMemory()
val tools = listOf(CalculatorTool(), WebSearchTool())
val llm = OpenAILLM(apiKey = System.getenv("OPENAI_API_KEY"))
val agent = AgentFramework(
    llm = llm,
    tools = tools,
    memory = memory
)
val response = runBlocking { agent.chat("What is 2+2?") }
println(response)
```

### LLMs
- **GeminiLLM**
```kotlin
val gemini = GeminiLLM(apiKey = System.getenv("GEMINI_API_KEY"))
val response = runBlocking { gemini.generate("Hello Gemini!") }
```
- **OpenAILLM**
```kotlin
val openai = OpenAILLM(apiKey = System.getenv("OPENAI_API_KEY"))
val response = runBlocking { openai.generate("Hello GPT!") }
```
- **ClaudeLLM, OllamaLLM, CohereLLM, GrokLLM**: Similar usage, see KDoc in source files.

### Tools
- **CalculatorTool**
```kotlin
val calc = CalculatorTool()
val result = runBlocking { calc.handle("2+2") }
```
- **WebSearchTool, FileReaderTool, APICallerTool**: See KDoc for examples.

### Memory
```kotlin
val memory = ConversationMemory()
runBlocking { memory.store("user_input", "Hello!") }
val history = runBlocking { memory.retrieve("history") }
println(history)
```

### Agent Graphs
#### SimpleAgentGraphBuilder
```kotlin
val builder = SimpleAgentGraphBuilder()
builder.addAgent(agent1).addAgent(agent2)
val graph = builder.build()
val result = runBlocking { graph.run("Start task") }
println(result)
```
#### ConditionalAgentGraphBuilder
```kotlin
val builder = ConditionalAgentGraphBuilder()
builder.addNode("A", agentA)
       .addNode("B", agentB)
       .addEdge("A", "B") { response -> response.contains("next") }
val graph = builder.build("A")
val result = runBlocking { graph.run("Start task") }
println(result)
```

### Embeddings & Chat Models
```kotlin
val embedder = SimpleEmbeddingProvider()
val embedding = runBlocking { embedder.embed("Hello world") }
val chatModel = SimpleChatModelProvider()
val response = runBlocking { chatModel.chat(listOf("Hello", "How are you?")) }
```

## 🎯 Examples

Check out the [examples/](examples/) directory for complete, runnable examples:

- **[Basic Agent](examples/basic-agent/)**: Simple agent with tools and memory
- **[Multi-Agent Graph](examples/multi-agent-graph/)**: Chaining multiple specialized agents
- **[Custom Tools](examples/custom-tools/)**: Creating your own tools

## 🔧 Extending KAgentic

### Create Custom Tools

Implement the `ToolHandler` interface:

```kotlin
class MyCustomTool : ToolHandler {
    override fun canHandle(input: String): Boolean {
        return input.contains("my-keyword", ignoreCase = true)
    }

    override fun score(input: String): Int {
        return if (canHandle(input)) 10 else 1
    }

    override suspend fun handle(input: String): String {
        // Your tool logic here
        return "Result: $input"
    }
}
```

### Create Custom LLM Providers

Implement the `LLMProvider` interface:

```kotlin
class MyCustomLLM : LLMProvider {
    override suspend fun generate(input: String): String {
        // Call your LLM API
        return "Response from my LLM"
    }
}
```

### Build Complex Agent Graphs

Use `ConditionalAgentGraphBuilder` for advanced workflows:

```kotlin
val graph = ConditionalAgentGraphBuilder()
    .addNode("analyzer", analyzerAgent)
    .addNode("processor", processorAgent)
    .addEdge("analyzer", "processor") { it.contains("needs-processing") }
    .build("analyzer")

val result = graph.run("Analyze and process this data")
```

## 🧪 Testing

Run the test suite:

```bash
./gradlew test
```

The project includes:
- Unit tests for all core components
- Integration tests for agent workflows
- Mock LLM providers for testing without API calls

## 📈 Project Status

**Current Version**: 0.1.0-alpha

This is an alpha release. APIs may change in future versions. Feedback and contributions are welcome!

### Roadmap

- [ ] Maven Central publishing
- [ ] More LLM provider integrations
- [ ] Vector database support for embeddings
- [ ] Streaming responses support
- [ ] Advanced memory strategies (sliding window, summarization)
- [ ] Production monitoring and observability
- [ ] Kotlin Multiplatform support

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Inspired by [LangChain](https://github.com/langchain-ai/langchain) and [LangGraph](https://github.com/langchain-ai/langgraph)
- Built with Kotlin and Ktor
- Special thanks to all contributors

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/anoopg87/KAgentic/issues)
- **Discussions**: [GitHub Discussions](https://github.com/anoopg87/KAgentic/discussions)

---

Made with ❤️ for the Kotlin community
