

# Agentic AI Framework (Kotlin)

Agentic AI Framework is a modular, production-ready Kotlin library for building agentic AI applications on the JVM. 

**Inspired by [LangChain](https://github.com/langchain-ai/langchain) and [LangGraph](https://github.com/langchain-ai/langgraph),** this framework brings agentic design patterns, graph-based orchestration, and tool/LLM extensibility to the Kotlin ecosystem. It is ideal for developers who want to:

- Compose multi-agent workflows and chains
- Integrate multiple LLMs and tools
- Build secure, robust, and extensible AI systems

Key features:

- **Extensible agent orchestration**: Compose agents, tools, and LLMs into flexible workflows.
- **Multiple LLM support**: Integrate Gemini, OpenAI, Claude, Ollama, Cohere, Grok, and more.
- **Pluggable tools**: Calculator, web search, file reader, API caller, and custom tools.
- **Agent graphs**: Chain, branch, and conditionally route agent calls for complex reasoning.
- **Embeddings & chat models**: Add semantic search and multi-turn chat capabilities.
- **Robust error handling & logging**: Secure, ethical, and reliable agentic workflows.

## Getting Started

Add the library to your JVM project and start composing agents, tools, and graphs. See below for key usage patterns and code examples.

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

## Extending
- Implement new tools by extending ToolHandler.
- Add new LLMs by implementing LLMProvider.
- Build custom agent graphs for advanced workflows.

See source code KDoc for full API documentation and more examples.
