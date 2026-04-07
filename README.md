# KAgentic: Agentic AI Framework for Kotlin

[![CI](https://github.com/anoopg87/KAgentic/actions/workflows/ci-publish.yml/badge.svg)](https://github.com/anoopg87/KAgentic/actions/workflows/ci-publish.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.23-purple.svg)](https://kotlinlang.org/)
[![JVM](https://img.shields.io/badge/JVM-17+-orange.svg)](https://www.oracle.com/java/)
[![Version](https://img.shields.io/badge/version-0.1.0--alpha-green.svg)](https://github.com/anoopg87/KAgentic/releases)

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

## 🤖 Claude Code Skills

KAgentic includes specialized Claude Code skills for accelerated development. These skills provide interactive, context-aware guidance directly in your development environment.

### Available Skills

- **add-custom-tool**: Create tools implementing the `ToolHandler` interface
  - Trigger: "I want to create a weather tool" or "Add a custom tool"
  - Provides templates, examples, and step-by-step guidance
  - References 4 existing tools as patterns

- **add-llm-provider**: Integrate new LLM providers
  - Trigger: "Add support for Mistral AI" or "Integrate new LLM"
  - Includes authentication patterns, retry logic, and API integration
  - References 7 existing providers (OpenAI, Claude, Gemini, Ollama, Cohere, Grok, DeepSeek)

- **create-agent-graph**: Build multi-agent workflows
  - Trigger: "Create a workflow with 3 agents" or "Build an agent graph"
  - Covers both SimpleAgentGraph (sequential) and ConditionalAgentGraph (branching)
  - Includes complete workflow examples

- **add-test-with-mock**: Write tests with mock objects
  - Trigger: "Write tests for my custom tool" or "Add test coverage"
  - Shows testing patterns from 9 existing test files
  - Includes mock LLM and tool patterns

- **understand-agent-flow**: Debug and trace agent execution
  - Trigger: "How does agent execution work?" or "Debug agent flow"
  - Explains core orchestration, tool selection, and memory integration
  - Visual flow diagrams and execution traces

- **add-memory-strategy**: Implement custom memory strategies
  - Trigger: "Create a custom memory strategy" or "Add memory implementation"
  - Shows thread-safety patterns with Mutex
  - Advanced feature for specialized use cases

### Using Skills with Claude Code

Skills auto-activate when you're working in the KAgentic repository with Claude Code. Simply describe what you want to do in natural language:

```
"I want to create a weather tool that fetches current conditions"
→ Triggers add-custom-tool skill with step-by-step guidance

"Add support for Together AI as an LLM provider"
→ Triggers add-llm-provider skill with templates and patterns

"Build a workflow where agents analyze, process, and summarize data"
→ Triggers create-agent-graph skill with graph examples
```

Each skill provides:
- ✅ Step-by-step implementation guidance
- ✅ Working code templates
- ✅ Best practices from existing code
- ✅ Testing approaches
- ✅ Troubleshooting tips

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
