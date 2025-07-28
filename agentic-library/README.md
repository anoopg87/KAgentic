# Agentic AI Framework (Kotlin)

This library provides a modular, production-ready agentic AI framework for JVM applications. It supports multiple LLMs, agent graphs, tools, memory, embeddings, and chat models.

## Key Components & Usage Examples

### AgentFramework
```
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
```
val gemini = GeminiLLM(apiKey = System.getenv("GEMINI_API_KEY"))
val response = runBlocking { gemini.generate("Hello Gemini!") }
```
- **OpenAILLM**
```
val openai = OpenAILLM(apiKey = System.getenv("OPENAI_API_KEY"))
val response = runBlocking { openai.generate("Hello GPT!") }
```
- **ClaudeLLM, OllamaLLM, CohereLLM, GrokLLM**: Similar usage, see KDoc in source files.

### Tools
- **CalculatorTool**
```
val calc = CalculatorTool()
val result = runBlocking { calc.handle("2+2") }
```
- **WebSearchTool, FileReaderTool, APICallerTool**: See KDoc for examples.

### Memory
```
val memory = ConversationMemory()
runBlocking { memory.store("user_input", "Hello!") }
val history = runBlocking { memory.retrieve("history") }
println(history)
```

### Agent Graphs
#### SimpleAgentGraphBuilder
```
val builder = SimpleAgentGraphBuilder()
builder.addAgent(agent1).addAgent(agent2)
val graph = builder.build()
val result = runBlocking { graph.run("Start task") }
println(result)
```
#### ConditionalAgentGraphBuilder
```
val builder = ConditionalAgentGraphBuilder()
builder.addNode("A", agentA)
       .addNode("B", agentB)
       .addEdge("A", "B") { response -> response.contains("next") }
val graph = builder.build("A")
val result = runBlocking { graph.run("Start task") }
println(result)
```

### Embeddings & Chat Models
```
val embedder = SimpleEmbeddingProvider()
val embedding = runBlocking { embedder.embed("Hello world") }
val chatModel = SimpleChatModelProvider()
val response = runBlocking { chatModel.chat(listOf("Hello", "How are you?")) }
```

## Thread Safety & Logging
- All memory operations are thread-safe (see ConversationMemory).
- All LLMs and tools support robust error handling and logging.

## Extending
- Implement new tools by extending ToolHandler.
- Add new LLMs by implementing LLMProvider.
- Build custom agent graphs for advanced workflows.

See source code KDoc for full API documentation and more examples.
