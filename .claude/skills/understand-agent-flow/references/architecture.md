## KAgentic Architecture Reference

Comprehensive overview of KAgentic's architectural design and component relationships.

---

## System Architecture

```
┌────────────────────────────────────────────────────────────┐
│                     KAgentic Framework                      │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────┐ │
│  │ AgentFramework│  │  Agent Graph │  │  Memory Layer   │ │
│  │  (Core)      │◄─┤  (Multi-Agent│◄─┤ (Conversation)  │ │
│  └───────┬──────┘  └──────────────┘  └─────────────────┘ │
│          │                                                  │
│    ┌─────┴──────┬──────────────┬───────────┐             │
│    │            │              │           │             │
│ ┌──▼──┐  ┌──────▼───┐  ┌──────▼──┐  ┌────▼──────┐      │
│ │ LLM │  │  Tools   │  │Embeddings│  │Chat Model│      │
│ │Layer│  │  Layer   │  │  Layer   │  │  Layer   │      │
│ └─────┘  └──────────┘  └──────────┘  └───────────┘      │
└────────────────────────────────────────────────────────────┘
         │           │           │            │
         │           │           │            │
    ┌────▼───┐  ┌───▼────┐  ┌───▼──┐   ┌────▼──────┐
    │OpenAI  │  │Calc    │  │Simple│   │Simple     │
    │Gemini  │  │Search  │  │Embed │   │ChatModel  │
    │Claude  │  │File    │  │Provider│   │Provider   │
    │...     │  │API     │  └──────┘   └───────────┘
    └────────┘  │Custom  │
                └────────┘
```

---

## Core Components

### 1. AgentFramework (Core Orchestrator)

**Location**: `/home/user/KAgentic/agentic-library/src/main/kotlin/core/AgentFramework.kt`

**Responsibilities**:
- Orchestrate LLM, tools, and memory
- Manage agent execution lifecycle
- Route to agent graphs (if present)
- Build prompts and generate responses

**Key Methods**:
- `chat(input: String): String` - Main entry point
- `chooseBestTool(input: String): ToolHandler?` - Tool selection
- `buildPrompt(input: String, toolResult: String?): String` - Prompt construction

**Properties**:
```kotlin
llm: LLMProvider              // Required
tools: List<ToolHandler>      // Required
memory: AgentMemory          // Required
embeddingProvider: EmbeddingProvider?    // Optional
chatModelProvider: ChatModelProvider?    // Optional
graph: AgentGraph?                      // Optional
```

---

### 2. LLM Layer

**Interface**: `LLMProvider`

```kotlin
interface LLMProvider {
    suspend fun generate(input: String): String
}
```

**Implementations**:
- `GeminiLLM` - Google Gemini API
- `OpenAILLM` - OpenAI GPT API
- `ClaudeLLM` - Anthropic Claude API
- `OllamaLLM` - Local Ollama models
- `CohereLLM` - Cohere API
- `GrokLLM` - xAI Grok API
- `DeepSeekLLM` - DeepSeek API

**Characteristics**:
- Async (suspend functions)
- Single method contract
- HTTP-based API calls
- Retry logic via `RetryHelper`
- Configurable models and parameters

**Common Pattern**:
```kotlin
val llm = OpenAILLM(
    apiKey = System.getenv("OPENAI_API_KEY"),
    model = "gpt-4"
)
val response = runBlocking { llm.generate("Hello") }
```

---

### 3. Tools Layer

**Interface**: `ToolHandler`

```kotlin
interface ToolHandler {
    fun canHandle(input: String): Boolean
    fun score(input: String): Int
    suspend fun handle(input: String): String
}
```

**Built-in Tools**:
- `CalculatorTool` - Mathematical expressions
- `WebSearchTool` - Web search (DuckDuckGo)
- `FileReaderTool` - File I/O operations
- `APICallerTool` - HTTP API calls

**Tool Lifecycle**:
1. **Filter**: `canHandle()` determines eligibility
2. **Score**: `score()` ranks eligible tools
3. **Execute**: `handle()` performs the operation

**Design Principles**:
- Single responsibility
- Fast `canHandle()` checks
- Meaningful score values (1-10)
- Idempotent operations
- Error handling

---

### 4. Memory Layer

**Interface**: `AgentMemory` or `MemoryStrategy`

```kotlin
interface MemoryStrategy {
    suspend fun store(key: String, value: String)
    suspend fun retrieve(key: String): String?
    suspend fun clear()
}
```

**Implementation**: `ConversationMemory`

**Features**:
- Thread-safe (uses `Mutex`)
- Key-value storage
- In-memory (non-persistent)
- Conversation history tracking

**Usage**:
```kotlin
memory.store("user_input", "Hello")
val stored = memory.retrieve("user_input")
memory.clear()
```

---

### 5. Agent Graph Layer

**Interface**: `AgentGraph`

```kotlin
interface AgentGraph {
    suspend fun run(input: String): String
}
```

**Implementations**:
- `SimpleAgentGraph` - Sequential execution
- `ConditionalAgentGraph` - Conditional branching

**Builders**:
- `SimpleAgentGraphBuilder`
- `ConditionalAgentGraphBuilder`

**Purpose**:
- Multi-agent orchestration
- Complex workflows
- Agent composition

---

### 6. Embedding Layer (Optional)

**Interface**: `EmbeddingProvider`

```kotlin
interface EmbeddingProvider {
    suspend fun embed(text: String): List<Float>
}
```

**Implementation**: `SimpleEmbeddingProvider`

**Use Cases**:
- Semantic search
- Vector database integration
- Advanced tool selection

---

### 7. Chat Model Layer (Optional)

**Interface**: `ChatModelProvider`

```kotlin
interface ChatModelProvider {
    suspend fun chat(messages: List<String>): String
}
```

**Implementation**: `SimpleChatModelProvider`

**Purpose**:
- Multi-turn conversations
- Context retention
- Conversation history

---

## Component Relationships

### Dependency Graph

```
AgentFramework (Core)
    ├── LLMProvider (required)
    ├── ToolHandler[] (required)
    ├── AgentMemory (required)
    ├── EmbeddingProvider (optional)
    ├── ChatModelProvider (optional)
    └── AgentGraph (optional)
        └── AgentFramework[] (recursive)
```

### Data Flow

```
User Input
    │
    ├──> Memory (store)
    ├──> Embedding (optional)
    ├──> Tool Selection
    │       ├──> canHandle() filter
    │       ├──> score() ranking
    │       └──> handle() execution
    ├──> Prompt Building
    │       ├──> System prompt
    │       ├──> User input
    │       └──> Tool result (if any)
    ├──> LLM Generation
    │       ├──> ChatModelProvider (with history)
    │       └──> LLMProvider (without history)
    ├──> Memory (store response)
    └──> Return to User
```

---

## Design Patterns

### 1. Strategy Pattern

**Tools** use strategy pattern:
- `ToolHandler` interface
- Multiple implementations
- Runtime selection

**Benefits**:
- Easy to add new tools
- Swappable implementations
- Testable with mocks

### 2. Builder Pattern

**Agent Graphs** use builder pattern:
- `SimpleAgentGraphBuilder`
- `ConditionalAgentGraphBuilder`

**Benefits**:
- Fluent API
- Step-by-step construction
- Validation before building

### 3. Dependency Injection

**AgentFramework** uses constructor injection:
```kotlin
AgentFramework(
    llm = myLLM,           // Inject
    tools = myTools,       // Inject
    memory = myMemory      // Inject
)
```

**Benefits**:
- Testability
- Flexibility
- Loose coupling

### 4. Template Method

**Tool Selection** uses template method:
1. Filter (same for all)
2. Score (same for all)
3. Select (same for all)

### 5. Chain of Responsibility

**Sequential Graphs** use chain pattern:
- Agent1 → Agent2 → Agent3
- Each agent processes and passes on

---

## Architectural Principles

### 1. Interface Segregation

Small, focused interfaces:
- `LLMProvider`: 1 method
- `ToolHandler`: 3 methods
- `MemoryStrategy`: 3 methods

### 2. Open/Closed Principle

Open for extension:
- Add new LLM providers
- Add new tools
- Add new memory strategies

Closed for modification:
- Core framework unchanged

### 3. Single Responsibility

Each component has one job:
- `AgentFramework`: Orchestration
- `LLMProvider`: Generation
- `ToolHandler`: Tool execution
- `MemoryStrategy`: State management

### 4. Dependency Inversion

Depend on abstractions:
- AgentFramework depends on `LLMProvider` interface
- Not on specific implementations like `OpenAILLM`

---

## Component Communication

### Synchronous vs Asynchronous

**Synchronous**:
- `canHandle()` - Fast checks
- `score()` - Fast scoring

**Asynchronous** (suspend):
- `generate()` - LLM API calls
- `handle()` - Tool execution
- `embed()` - Embedding generation
- `chat()` - Agent execution

### Thread Safety

**Thread-Safe Components**:
- `ConversationMemory` (uses `Mutex`)

**Not Thread-Safe** (by design):
- `AgentFramework` (create per-thread/coroutine)
- Tools (stateless recommended)
- LLM Providers (HTTP client handles concurrency)

---

## Extension Points

### 1. Custom LLM Provider

```kotlin
class MyCustomLLM : LLMProvider {
    override suspend fun generate(input: String): String {
        // Your implementation
    }
}
```

### 2. Custom Tool

```kotlin
class MyCustomTool : ToolHandler {
    override fun canHandle(input: String) = /* logic */
    override fun score(input: String) = /* 1-10 */
    override suspend fun handle(input: String) = /* result */
}
```

### 3. Custom Memory

```kotlin
class MyCustomMemory : MemoryStrategy {
    override suspend fun store(key: String, value: String) { }
    override suspend fun retrieve(key: String): String? { }
    override suspend fun clear() { }
}
```

### 4. Custom Agent Graph

```kotlin
class MyCustomGraph : AgentGraph {
    override suspend fun run(input: String): String {
        // Your workflow logic
    }
}
```

---

## Performance Characteristics

### Time Complexity

| Operation | Complexity | Notes |
|-----------|-----------|-------|
| Tool Selection | O(n) | n = number of tools |
| Memory Store | O(1) | HashMap-based |
| Memory Retrieve | O(1) | HashMap-based |
| LLM Generation | Variable | Network I/O bound |
| Tool Execution | Variable | Tool-dependent |

### Space Complexity

| Component | Space | Notes |
|-----------|-------|-------|
| AgentFramework | O(1) | Minimal overhead |
| ConversationMemory | O(k) | k = stored items |
| Tools List | O(n) | n = number of tools |
| Agent Graph | O(m) | m = number of agents |

### Bottlenecks

1. **LLM API Calls** (500ms - 5s)
   - Slowest operation
   - Network dependent
   - Can use caching

2. **Tool Execution** (100ms - 2s)
   - Varies by tool
   - WebSearch: ~1s
   - Calculator: <10ms
   - FileReader: ~50ms

3. **Memory Operations** (<1ms)
   - Usually negligible
   - Thread-safe with Mutex

---

## Error Handling

### Error Propagation

```
Tool.handle() throws
    ↓
AgentFramework.chat() catches
    ↓
Builds error prompt
    ↓
LLM generates error response
    ↓
Returns to user
```

### Retry Mechanism

`RetryHelper` provides exponential backoff:
- Max retries: 3
- Delays: 1s, 2s, 4s
- Used by LLM providers

---

## Testing Architecture

### Unit Tests

- Test each component independently
- Use mocks for dependencies
- Fast execution (<100ms per test)

### Integration Tests

- Test component interactions
- Use real implementations (where practical)
- Slower execution (~1s per test)

### Mock Components

Easy to create mocks:
```kotlin
// Mock LLM
val mockLLM = object : LLMProvider {
    override suspend fun generate(input: String) = "mock"
}

// Mock Tool
val mockTool = object : ToolHandler {
    override fun canHandle(input: String) = true
    override fun score(input: String) = 10
    override suspend fun handle(input: String) = "result"
}
```

---

## Deployment Architecture

### Recommended Setup

```
┌─────────────────┐
│   Application   │
│   (Your Code)   │
└────────┬────────┘
         │
    ┌────▼────┐
    │ KAgentic│
    │ Library │
    └────┬────┘
         │
    ┌────┴───────────┬──────────┐
    │                │          │
┌───▼───┐      ┌────▼─────┐  ┌▼──────┐
│OpenAI │      │DuckDuck  │  │Local  │
│ API   │      │Go Search │  │Files  │
└───────┘      └──────────┘  └───────┘
```

### Environment Variables

```bash
OPENAI_API_KEY=sk-...
CLAUDE_API_KEY=sk-ant-...
GEMINI_API_KEY=AI...
```

### Dependencies

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.agentic:kotlin-agentic:0.1.0-alpha")
    implementation("io.ktor:ktor-client-core:2.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
```

---

## Summary

**Key Architectural Features**:
1. ✅ Modular design (easy to extend)
2. ✅ Interface-based (testable)
3. ✅ Async/coroutine support (efficient)
4. ✅ Thread-safe where needed (reliable)
5. ✅ Multiple LLM providers (flexible)
6. ✅ Pluggable tools (extensible)
7. ✅ Graph-based orchestration (powerful)

**Design Philosophy**:
- Simple interfaces
- Composition over inheritance
- Dependency injection
- Testability first
- Kotlin idioms
