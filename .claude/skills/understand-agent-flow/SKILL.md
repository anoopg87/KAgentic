---
name: understand-agent-flow
description: Understand how the AgentFramework orchestrates LLMs, tools, and memory. Learn the internal execution flow, debugging strategies, and architectural patterns. Use when debugging agents or learning how the framework works.
version: "1.0.0"
---

# Understand Agent Flow

Learn how KAgentic's AgentFramework orchestrates LLMs, tools, and memory to create intelligent agents.

## When to Use This Skill

Trigger when you need to:
- "How does the agent framework work?"
- "Debug my agent's behavior"
- "Understand agent execution flow"
- "Why is my agent not using the right tool?"
- "Explain how tools are selected"

## Overview

The AgentFramework is the core orchestrator that:
1. Receives user input
2. Stores input in memory
3. Selects the best tool (if applicable)
4. Builds prompt for LLM
5. Generates response via LLM
6. Returns response to user

This skill helps you:
- Understand the complete execution flow
- Debug tool selection issues
- Trace agent decision-making
- Optimize agent performance
- Build better agents

## Core Architecture

### AgentFramework Components

```kotlin
class AgentFramework(
    val llm: LLMProvider,          // Generates AI responses
    val tools: List<ToolHandler>,  // Available tools
    val memory: AgentMemory,       // Conversation history
    val embeddingProvider: EmbeddingProvider? = null,  // Optional
    val chatModelProvider: ChatModelProvider? = null,  // Optional
    val graph: AgentGraph? = null  // Optional multi-agent
)
```

**Key Components:**
1. **LLM Provider**: Generates intelligent responses
2. **Tools**: Specialized capabilities (calculator, search, etc.)
3. **Memory**: Stores conversation history and state
4. **Embedding Provider**: (Optional) Vector-based reasoning
5. **Chat Model Provider**: (Optional) Multi-turn conversations
6. **Agent Graph**: (Optional) Multi-agent workflows

---

## Execution Flow

### Single Agent Flow (No Graph)

```
User Input
    ↓
[1] Store in Memory ("user_input")
    ↓
[2] Generate Embedding (optional)
    ↓
[3] Choose Best Tool
    ├─→ Filter tools (canHandle = true)
    ├─→ Score remaining tools
    └─→ Select highest score
    ↓
[4] Execute Tool (if selected)
    ↓
[5] Build Prompt
    ├─→ System prompt
    ├─→ User input
    └─→ Tool result (if any)
    ↓
[6] Generate LLM Response
    ├─→ Use ChatModelProvider (if multi-turn)
    └─→ Or use LLMProvider
    ↓
[7] Store Response in Memory
    ↓
[8] Return Response to User
```

### Multi-Agent Flow (With Graph)

```
User Input
    ↓
[1] Detect Graph Present
    ↓
[2] Delegate to graph.run(input)
    ├─→ SimpleAgentGraph: Sequential execution
    └─→ ConditionalAgentGraph: Conditional routing
    ↓
[3] Each Agent in Graph
    └─→ Runs own single-agent flow
    ↓
[4] Return Final Result
```

---

## Step-by-Step Breakdown

### Step 1: Store Input in Memory

```kotlin
memory.store("user_input", input)
```

**Purpose**: Persist user input for context and history

**Why Important**:
- Enables conversation continuity
- Allows tools to access original input
- Supports multi-turn conversations

### Step 2: Generate Embedding (Optional)

```kotlin
val inputEmbedding = embeddingProvider?.embed(input)
if (inputEmbedding != null) {
    memory.store("user_input_embedding", inputEmbedding.joinToString(","))
}
```

**Purpose**: Create vector representation for semantic search/reasoning

**When Used**:
- Advanced tool selection
- Semantic similarity matching
- Vector database integration

**Can Skip If**: No embedding provider configured

### Step 3: Choose Best Tool

```kotlin
private fun chooseBestTool(input: String, inputEmbedding: List<Float>?): ToolHandler? {
    return tools
        .filter { it.canHandle(input) }
        .maxByOrNull { tool -> tool.score(input) }
}
```

**Selection Process:**
1. **Filter**: Keep only tools where `canHandle(input) == true`
2. **Score**: Call `score(input)` on each remaining tool
3. **Select**: Choose tool with highest score
4. **Return**: Highest-scoring tool (or null if no tools available)

**Example:**
```
Input: "What is 5 + 3?"

CalculatorTool:
  canHandle("What is 5 + 3?") = true  ✓
  score("What is 5 + 3?") = 10

WebSearchTool:
  canHandle("What is 5 + 3?") = false  ✗
  (filtered out)

Selected: CalculatorTool (score 10)
```

### Step 4: Execute Tool

```kotlin
val selectedTool = chooseBestTool(input, inputEmbedding)
val toolResult = selectedTool?.handle(input)
```

**What Happens:**
- If tool selected: Execute `tool.handle(input)`
- If no tool selected: `toolResult = null`
- Tool returns formatted result string

**Example:**
```kotlin
// CalculatorTool selected
val toolResult = tool.handle("5 + 3")
// Returns: "Result: 8"
```

### Step 5: Build Prompt

```kotlin
private fun buildPrompt(input: String, toolResult: String?): String {
    val systemPrompt = "You are a highly intelligent, clever, and autonomous AI agent..."
    return if (toolResult != null) {
        "$systemPrompt\nUser: $input\nTool Result: $toolResult\nAI:"
    } else {
        "$systemPrompt\nUser: $input\nAI:"
    }
}
```

**Structure:**
```
[System Prompt]
You are a highly intelligent, clever, and autonomous AI agent...

User: [user input]

[If tool used]
Tool Result: [tool output]

AI: [LLM continues here]
```

**Example with Tool:**
```
System: You are a highly intelligent AI agent...
User: What is 5 + 3?
Tool Result: Result: 8
AI: The answer is 8.
```

**Example without Tool:**
```
System: You are a highly intelligent AI agent...
User: What is the capital of France?
AI: The capital of France is Paris.
```

### Step 6: Generate Response

```kotlin
val response = if (chatModelProvider != null) {
    // Multi-turn conversation
    val history = memory.retrieve("history")?.split("\n") ?: listOf()
    chatModelProvider.chat(history + listOf(prompt))
} else {
    // Single-turn generation
    llm.generate(prompt)
}
```

**Two Paths:**

**Path A: With ChatModelProvider** (multi-turn)
1. Retrieve conversation history from memory
2. Append current prompt to history
3. Generate response with full context

**Path B: With LLMProvider** (single-turn)
1. Generate response from prompt alone
2. No conversation history

### Step 7: Store Response

```kotlin
memory.store("agent_response", response)
```

**Purpose**: Save response for future context

### Step 8: Return Response

```kotlin
return response
```

**Final output returned to user**

---

## Tool Selection Deep Dive

### The Three-Step Selection Process

#### 1. canHandle() Filter

**Purpose**: Quickly eliminate irrelevant tools

**Example:**
```kotlin
class CalculatorTool : ToolHandler {
    override fun canHandle(input: String): Boolean {
        // Check for math patterns
        return input.matches(Regex(".*\\d+\\s*[+\\-*/]\\s*\\d+.*"))
    }
}
```

**Results:**
- "5 + 3" → `true` ✓
- "search kotlin" → `false` ✗

#### 2. score() Ranking

**Purpose**: Prioritize among multiple capable tools

**Convention:**
- **10**: Perfect match (highly specialized for this input)
- **5-9**: Good match (can handle but not ideal)
- **1-4**: Weak match (fallback option)

**Example:**
```kotlin
override fun score(input: String): Int {
    return when {
        input.contains("+") || input.contains("-") -> 10  // Math operator
        input.matches(Regex(".*\\d+.*")) -> 5             // Has numbers
        else -> 1                                          // Fallback
    }
}
```

#### 3. maxByOrNull Selection

Selects tool with highest score, or null if no tools pass `canHandle()`.

### Tool Selection Examples

**Example 1: Clear Winner**
```
Input: "Calculate 10 + 5"

Tools:
  CalculatorTool: canHandle=true, score=10 ✓ SELECTED
  WebSearchTool: canHandle=false (filtered)
  FileReaderTool: canHandle=false (filtered)

Selected: CalculatorTool
```

**Example 2: Multiple Candidates**
```
Input: "What is 42?"

Tools:
  CalculatorTool: canHandle=true, score=5
  WebSearchTool: canHandle=true, score=8 ✓ SELECTED
  FileReaderTool: canHandle=false (filtered)

Selected: WebSearchTool (higher score)
```

**Example 3: No Tool Matches**
```
Input: "Hello, how are you?"

Tools:
  CalculatorTool: canHandle=false
  WebSearchTool: canHandle=false
  FileReaderTool: canHandle=false

Selected: null (pure LLM response)
```

---

## Debugging Strategies

### Issue: Agent Not Using Expected Tool

**Symptoms**: Agent ignores tool or uses wrong tool

**Debug Steps:**

1. **Check canHandle() Logic**
```kotlin
val tool = MyTool()
println("Can handle: ${tool.canHandle(input)}")  // Should be true
```

2. **Check score() Values**
```kotlin
tools.forEach { tool ->
    if (tool.canHandle(input)) {
        println("${tool::class.simpleName}: score=${tool.score(input)}")
    }
}
```

3. **Trace Tool Selection**
```kotlin
val selectedTool = tools
    .filter { it.canHandle(input) }
    .also { println("Filtered tools: ${it.map { t -> t::class.simpleName }}") }
    .maxByOrNull { it.score(input) }
    .also { println("Selected: ${it::class.simpleName}") }
```

### Issue: Agent Responses Are Inconsistent

**Possible Causes:**
1. Memory state pollution
2. Tool returning inconsistent results
3. LLM temperature too high

**Debug Steps:**

1. **Check Memory State**
```kotlin
println("User input: ${memory.retrieve("user_input")}")
println("Last response: ${memory.retrieve("agent_response")}")
```

2. **Test Tool Directly**
```kotlin
val toolResult = runBlocking { tool.handle(input) }
println("Tool result: $toolResult")
```

3. **Inspect Prompt**
```kotlin
// Add logging to buildPrompt()
println("=== PROMPT ===")
println(prompt)
println("=== END PROMPT ===")
```

### Issue: Agent Graph Not Routing Correctly

**Debug Conditional Graphs:**

```kotlin
val mockLLM = object : LLMProvider {
    override suspend fun generate(input: String): String {
        val response = "route to agent B"
        println("LLM response: $response")  // Debug output
        return response
    }
}

// Check edge conditions
builder.addEdge("A", "B") { response ->
    val matches = response.contains("route to agent B")
    println("Edge A→B condition: $matches")  // Debug output
    matches
}
```

### Issue: Performance Problems

**Measure Each Step:**

```kotlin
suspend fun chat(input: String): String {
    val start = System.currentTimeMillis()
    
    memory.store("user_input", input)
    println("Memory store: ${System.currentTimeMillis() - start}ms")
    
    val toolStart = System.currentTimeMillis()
    val selectedTool = chooseBestTool(input, null)
    println("Tool selection: ${System.currentTimeMillis() - toolStart}ms")
    
    val toolExecStart = System.currentTimeMillis()
    val toolResult = selectedTool?.handle(input)
    println("Tool execution: ${System.currentTimeMillis() - toolExecStart}ms")
    
    val llmStart = System.currentTimeMillis()
    val response = llm.generate(buildPrompt(input, toolResult))
    println("LLM generation: ${System.currentTimeMillis() - llmStart}ms")
    
    println("Total: ${System.currentTimeMillis() - start}ms")
    return response
}
```

---

## Advanced Patterns

### Pattern 1: Agent with Conversation History

```kotlin
val agent = AgentFramework(
    llm = myLLM,
    tools = myTools,
    memory = ConversationMemory(),
    chatModelProvider = SimpleChatModelProvider()  // Enables history
)

// Multi-turn conversation
runBlocking {
    agent.chat("My name is Alice")  // Stored in history
    agent.chat("What is my name?")  // Uses history: "Your name is Alice"
}
```

### Pattern 2: Agent with Semantic Search

```kotlin
val agent = AgentFramework(
    llm = myLLM,
    tools = myTools,
    memory = memory,
    embeddingProvider = SimpleEmbeddingProvider()  // Enables embeddings
)

// Tools can access embeddings for advanced matching
```

### Pattern 3: Multi-Agent with Graph

```kotlin
val graph = SimpleAgentGraphBuilder()
    .addAgent(agent1)
    .addAgent(agent2)
    .build()

val orchestrator = AgentFramework(
    llm = coordinatorLLM,
    tools = listOf(),
    memory = memory,
    graph = graph  // Delegates to graph
)

// Single call, multiple agents
val result = orchestrator.chat("Complex multi-step task")
```

---

## Optimization Tips

### 1. Tool Performance

**Slow Tool:**
```kotlin
override suspend fun handle(input: String): String {
    delay(5000)  // BAD: 5 second delay
    return "result"
}
```

**Fast Tool:**
```kotlin
override suspend fun handle(input: String): String {
    // Use caching, efficient algorithms
    return cache.getOrPut(input) { computeResult(input) }
}
```

### 2. Memory Efficiency

**Inefficient:**
```kotlin
// Stores entire conversation every time
memory.store("history", fullConversationHistory)
```

**Efficient:**
```kotlin
// Store only recent messages
memory.store("last_10_messages", recentMessages.takeLast(10))
```

### 3. LLM Optimization

**Inefficient:**
```kotlin
// Sends huge prompts
val context = allDocuments.joinToString("\n")  // Could be 100KB+
llm.generate("$context\n$input")
```

**Efficient:**
```kotlin
// Send only relevant context
val relevantDocs = findRelevant(input, documents).take(3)
llm.generate("${relevantDocs.joinToString("\n")}\n$input")
```

---

## Common Misconceptions

### ❌ "Agent automatically knows which tool to use"

**Reality**: Agent uses score-based selection. You must implement `canHandle()` and `score()` correctly.

### ❌ "Memory persists across restarts"

**Reality**: ConversationMemory is in-memory only. Implement custom MemoryStrategy for persistence.

### ❌ "Agent learns from conversations"

**Reality**: Agent doesn't learn. It uses LLM + tools + current memory. No training occurs.

### ❌ "All tools are tried until one works"

**Reality**: Only ONE tool is selected (highest score). If it fails, error is returned.

---

## Flow Diagrams

### Single Agent Execution

```
┌─────────────┐
│ User Input  │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Store Input │
│  in Memory  │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│ Generate        │
│ Embedding       │ (Optional)
└──────┬──────────┘
       │
       ▼
┌─────────────────────┐
│ Choose Best Tool    │
│ 1. Filter (canHandle)│
│ 2. Score (score)    │
│ 3. Select (maxBy)   │
└──────┬──────────────┘
       │
       ├─── Tool Selected ───┐
       │                     │
       │                     ▼
       │              ┌─────────────┐
       │              │ Execute Tool│
       │              └──────┬──────┘
       │                     │
       └─── No Tool ─────────┤
                             │
                             ▼
                      ┌─────────────┐
                      │ Build Prompt│
                      └──────┬──────┘
                             │
                             ▼
                      ┌─────────────┐
                      │ LLM Generate│
                      └──────┬──────┘
                             │
                             ▼
                      ┌─────────────┐
                      │Store Response│
                      └──────┬──────┘
                             │
                             ▼
                      ┌─────────────┐
                      │   Return    │
                      └─────────────┘
```

### Tool Selection Process

```
[All Tools]
     │
     ▼
┌─────────────────┐
│ Filter          │
│ canHandle(input)│
│ = true          │
└────────┬────────┘
         │
    [Capable Tools]
         │
         ▼
┌─────────────────┐
│ Score Each      │
│ tool.score(input)│
└────────┬────────┘
         │
    [Scored Tools]
         │
         ▼
┌─────────────────┐
│ Select Highest  │
│ maxByOrNull     │
└────────┬────────┘
         │
         ▼
   [Selected Tool]
   or null
```

---

## Related Skills

- **add-custom-tool**: Create tools that integrate into this flow
- **add-llm-provider**: Customize LLM generation step
- **create-agent-graph**: Build multi-agent workflows
- **add-memory-strategy**: Customize memory behavior

## Reference Files

- AgentFramework Source: `/home/user/KAgentic/agentic-library/src/main/kotlin/core/AgentFramework.kt`
- Architecture Reference: `.claude/skills/understand-agent-flow/references/architecture.md`
- Execution Flow Reference: `.claude/skills/understand-agent-flow/references/execution-flow.md`

---

## Quick Reference

### Agent Lifecycle
1. Input → 2. Memory → 3. Embedding → 4. Tool → 5. Prompt → 6. LLM → 7. Response

### Tool Selection
Filter (canHandle) → Score (score) → Select (maxByOrNull)

### Debugging Checklist
- [ ] Check `canHandle()` returns true for expected input
- [ ] Verify `score()` returns highest value for expected tool
- [ ] Test tool `handle()` executes correctly
- [ ] Inspect generated prompt includes tool result
- [ ] Verify LLM receives correct prompt
- [ ] Check memory stores values correctly

### Performance Hotspots
1. LLM generation (usually slowest)
2. Tool execution (depends on tool)
3. Embedding generation (if used)
4. Memory operations (usually fast)
