## Agent Execution Flow Reference

Detailed breakdown of AgentFramework execution with examples and debugging guidance.

**Source**: `/home/user/KAgentic/agentic-library/src/main/kotlin/core/AgentFramework.kt`

---

## Complete Execution Trace

### Example Input: "What is 5 + 3?"

```
Step 1: chat() called
├─ Input: "What is 5 + 3?"
└─ Graph check: null (no graph configured)

Step 2: Store in Memory
├─ Key: "user_input"
└─ Value: "What is 5 + 3?"

Step 3: Generate Embedding (Optional)
├─ embeddingProvider: null
└─ Skip embedding

Step 4: Choose Best Tool
├─ Filter Phase:
│  ├─ CalculatorTool.canHandle("What is 5 + 3?") = true  ✓
│  ├─ WebSearchTool.canHandle("What is 5 + 3?") = false ✗
│  └─ FileReaderTool.canHandle("What is 5 + 3?") = false ✗
├─ Score Phase:
│  └─ CalculatorTool.score("What is 5 + 3?") = 10
└─ Selection: CalculatorTool (highest score)

Step 5: Execute Tool
├─ Tool: CalculatorTool
├─ Input: "What is 5 + 3?"
└─ Result: "Result: 8"

Step 6: Build Prompt
├─ System: "You are a highly intelligent, clever, and autonomous AI agent..."
├─ User: "What is 5 + 3?"
├─ Tool Result: "Result: 8"
└─ Full Prompt:
    "You are a highly intelligent...
     User: What is 5 + 3?
     Tool Result: Result: 8
     AI:"

Step 7: Generate Response
├─ chatModelProvider: null
├─ Use llm.generate(prompt)
├─ LLM: OpenAILLM
└─ Response: "The answer is 8. I used the calculator to add 5 and 3."

Step 8: Store Response
├─ Key: "agent_response"
└─ Value: "The answer is 8. I used the calculator to add 5 and 3."

Step 9: Return
└─ Output: "The answer is 8. I used the calculator to add 5 and 3."
```

---

## Source Code Annotated

```kotlin
suspend fun chat(input: String): String {
    // ┌─────────────────────────────────────────────────────────┐
    // │ STEP 1: Check for Agent Graph                            │
    // │ If graph present, delegate multi-agent workflow          │
    // └─────────────────────────────────────────────────────────┘
    if (graph != null) {
        return graph.run(input)  // Delegate to graph
    }
    
    // ┌─────────────────────────────────────────────────────────┐
    // │ STEP 2: Store User Input                                 │
    // │ Persist input for context and future retrieval          │
    // └─────────────────────────────────────────────────────────┘
    memory.store("user_input", input)
    
    // ┌─────────────────────────────────────────────────────────┐
    // │ STEP 3: Generate Embedding (Optional)                    │
    // │ Create vector representation for semantic reasoning     │
    // └─────────────────────────────────────────────────────────┘
    val inputEmbedding = embeddingProvider?.embed(input)
    if (inputEmbedding != null) {
        memory.store("user_input_embedding", inputEmbedding.joinToString(","))
    }
    
    // ┌─────────────────────────────────────────────────────────┐
    // │ STEP 4: Choose Best Tool                                 │
    // │ Filter → Score → Select                                  │
    // └─────────────────────────────────────────────────────────┘
    val selectedTool = chooseBestTool(input, inputEmbedding)
    
    // ┌─────────────────────────────────────────────────────────┐
    // │ STEP 5: Execute Tool (if selected)                       │
    // │ Call tool.handle() to get result                         │
    // └─────────────────────────────────────────────────────────┘
    val toolResult = selectedTool?.handle(input)
    
    // ┌─────────────────────────────────────────────────────────┐
    // │ STEP 6: Build Prompt                                     │
    // │ Combine system prompt, user input, and tool result      │
    // └─────────────────────────────────────────────────────────┘
    val prompt = buildPrompt(input, toolResult)
    
    // ┌─────────────────────────────────────────────────────────┐
    // │ STEP 7: Generate Response                                │
    // │ Use ChatModelProvider or LLMProvider                     │
    // └─────────────────────────────────────────────────────────┘
    val response = if (chatModelProvider != null) {
        // Multi-turn with history
        val history = memory.retrieve("history")?.split("\n") ?: listOf()
        chatModelProvider.chat(history + listOf(prompt))
    } else {
        // Single-turn without history
        llm.generate(prompt)
    }
    
    // ┌─────────────────────────────────────────────────────────┐
    // │ STEP 8: Store Response                                   │
    // │ Save for context and future reference                    │
    // └─────────────────────────────────────────────────────────┘
    memory.store("agent_response", response)
    
    // ┌─────────────────────────────────────────────────────────┐
    // │ STEP 9: Return to User                                   │
    // └─────────────────────────────────────────────────────────┘
    return response
}
```

---

## Tool Selection Deep Dive

### chooseBestTool() Implementation

```kotlin
private fun chooseBestTool(input: String, inputEmbedding: List<Float>?): ToolHandler? {
    return tools
        .filter { it.canHandle(input) }      // ← Filter phase
        .maxByOrNull { tool -> tool.score(input) }  // ← Score & select phase
}
```

### Execution Example

**Scenario: Multiple Tools Available**

```
Input: "search kotlin coroutines"

Tools Available:
┌──────────────────┬─────────────┬───────┐
│ Tool             │ canHandle() │ score │
├──────────────────┼─────────────┼───────┤
│ CalculatorTool   │ false       │  -    │  ← Filtered out
│ WebSearchTool    │ true        │  10   │  ← Selected!
│ FileReaderTool   │ false       │  -    │  ← Filtered out
│ APICallerTool    │ true        │  5    │  ← Not highest
└──────────────────┴─────────────┴───────┘

Result: WebSearchTool selected (score 10)
```

### Trace with Logging

```kotlin
private fun chooseBestTool(input: String, inputEmbedding: List<Float>?): ToolHandler? {
    println("=== Tool Selection ===")
    println("Input: $input")
    
    val capableTools = tools.filter { tool ->
        val can = tool.canHandle(input)
        println("${tool::class.simpleName}.canHandle() = $can")
        can
    }
    
    println("Capable tools: ${capableTools.map { it::class.simpleName }}")
    
    return capableTools.maxByOrNull { tool ->
        val score = tool.score(input)
        println("${tool::class.simpleName}.score() = $score")
        score
    }.also { selected ->
        println("Selected: ${selected::class.simpleName}")
        println("=====================")
    }
}
```

**Output**:
```
=== Tool Selection ===
Input: search kotlin coroutines
CalculatorTool.canHandle() = false
WebSearchTool.canHandle() = true
FileReaderTool.canHandle() = false
APICallerTool.canHandle() = true
Capable tools: [WebSearchTool, APICallerTool]
WebSearchTool.score() = 10
APICallerTool.score() = 5
Selected: WebSearchTool
=====================
```

---

## buildPrompt() Deep Dive

### Implementation

```kotlin
private fun buildPrompt(input: String, toolResult: String?): String {
    val systemPrompt = "You are a highly intelligent, clever, and autonomous AI agent. You reason deeply, use tools when needed, and always provide insightful, context-aware answers. If the user asks for credentials, personal information, unsecure actions, or makes inappropriate social statements, you must politely refuse and reply with 'Not permitted.'"
    
    return if (toolResult != null) {
        "$systemPrompt\nUser: $input\nTool Result: $toolResult\nAI:"
    } else {
        "$systemPrompt\nUser: $input\nAI:"
    }
}
```

### Example Outputs

**With Tool:**
```
System: You are a highly intelligent, clever, and autonomous AI agent...

User: What is 5 + 3?

Tool Result: Result: 8

AI: ← LLM continues from here
```

**Without Tool:**
```
System: You are a highly intelligent, clever, and autonomous AI agent...

User: What is the capital of France?

AI: ← LLM continues from here
```

---

## Response Generation Paths

### Path 1: With ChatModelProvider

```kotlin
val response = if (chatModelProvider != null) {
    val history = memory.retrieve("history")?.split("\n") ?: listOf()
    chatModelProvider.chat(history + listOf(prompt))
}
```

**When Used:**
- Multi-turn conversations
- Context from previous messages
- Conversation continuity

**Example:**
```
History:
  "User: My name is Alice"
  "AI: Nice to meet you, Alice!"
  "User: What is my name?"

Current Prompt:
  "System: ...
   User: What is my name?
   AI:"

Chat Model sees full history → "Your name is Alice"
```

### Path 2: With LLMProvider Only

```kotlin
val response = llm.generate(prompt)
```

**When Used:**
- Single-turn interactions
- No conversation history needed
- Simpler use cases

**Example:**
```
Prompt:
  "System: ...
   User: What is 2+2?
   Tool Result: Result: 4
   AI:"

LLM sees only current turn → "The answer is 4"
```

---

## Memory Operations

### Store Operations

```kotlin
memory.store("user_input", input)                    // Step 2
memory.store("user_input_embedding", embedding)      // Step 3 (optional)
memory.store("agent_response", response)             // Step 8
```

### Retrieve Operations

```kotlin
val history = memory.retrieve("history")             // Step 7 (if chatModelProvider)
val lastInput = memory.retrieve("user_input")        // Can be accessed by tools
val lastResponse = memory.retrieve("agent_response") // Can be accessed by tools
```

### Memory Timeline

```
Time ──────────────────────────────────────────────────>

T0: memory.store("user_input", "What is 5+3?")
T1: memory.store("user_input_embedding", "0.1,0.2,...")
T2: [Tool executes - may read memory]
T3: memory.store("agent_response", "The answer is 8")
T4: [Next turn - may read previous response]
```

---

## Error Scenarios

### Scenario 1: No Tool Matches

```
Input: "Hello, how are you?"

Tool Selection:
├─ CalculatorTool.canHandle() = false
├─ WebSearchTool.canHandle() = false
└─ FileReaderTool.canHandle() = false

Result: selectedTool = null
        toolResult = null
        → Proceed without tool
        → Pure LLM response
```

### Scenario 2: Tool Execution Fails

```
Input: "search unavailable site"

Tool Selection:
└─ WebSearchTool selected

Tool Execution:
└─ tool.handle(input) throws IOException("Network error")

Result: Exception propagates
        → Agent.chat() fails
        → Error returned to caller
```

### Scenario 3: LLM API Fails

```
Input: "What is 2+2?"

Tool: CalculatorTool executes successfully
      toolResult = "Result: 4"

LLM Generation:
└─ llm.generate(prompt) throws IOException("API timeout")

Result: Exception propagates
        → Agent.chat() fails
        → Error returned to caller
        → Note: toolResult was computed but not used
```

---

## Performance Profiling

### Typical Timings

```
Total Time: ~1500ms

Breakdown:
├─ Memory Store (input):        1ms    (0.07%)
├─ Embedding Generation:        50ms   (3.33%)  [if enabled]
├─ Tool Selection:              5ms    (0.33%)
├─ Tool Execution:              100ms  (6.67%)  [varies by tool]
├─ Prompt Building:             1ms    (0.07%)
├─ LLM Generation:              1300ms (86.67%) ← Slowest!
├─ Memory Store (response):     1ms    (0.07%)
└─ Return:                      <1ms   (0.00%)
```

### Optimization Opportunities

1. **Cache LLM Responses**
```kotlin
val cache = mutableMapOf<String, String>()

fun cachedGenerate(prompt: String): String {
    return cache.getOrPut(prompt) {
        llm.generate(prompt)
    }
}
```

2. **Parallel Tool Checking** (future optimization)
```kotlin
val capableTools = tools.parMap { tool ->
    if (tool.canHandle(input)) tool else null
}.filterNotNull()
```

3. **Lazy Embedding**
```kotlin
// Only generate if tool selection needs it
val embedding by lazy { embeddingProvider?.embed(input) }
```

---

## Debugging Examples

### Debug: Print All Steps

```kotlin
suspend fun chat(input: String): String {
    println("━━━ AGENT EXECUTION START ━━━")
    println("Input: $input")
    
    if (graph != null) {
        println("→ Delegating to graph")
        return graph.run(input)
    }
    
    println("→ Storing input in memory")
    memory.store("user_input", input)
    
    println("→ Selecting tool")
    val selectedTool = chooseBestTool(input, null)
    println("→ Selected: ${selectedTool?.let { it::class.simpleName } ?: "None"}")
    
    val toolResult = selectedTool?.let {
        println("→ Executing tool")
        it.handle(input).also { result ->
            println("→ Tool result: $result")
        }
    }
    
    println("→ Building prompt")
    val prompt = buildPrompt(input, toolResult)
    println("→ Prompt length: ${prompt.length} chars")
    
    println("→ Generating LLM response")
    val response = llm.generate(prompt)
    println("→ Response: $response")
    
    println("→ Storing response")
    memory.store("agent_response", response)
    
    println("━━━ AGENT EXECUTION END ━━━")
    return response
}
```

### Debug: Measure Performance

```kotlin
suspend fun chat(input: String): String {
    val timings = mutableMapOf<String, Long>()
    val start = System.currentTimeMillis()
    
    fun recordTime(label: String) {
        timings[label] = System.currentTimeMillis() - start
    }
    
    if (graph != null) return graph.run(input)
    
    memory.store("user_input", input)
    recordTime("memory_store")
    
    val selectedTool = chooseBestTool(input, null)
    recordTime("tool_selection")
    
    val toolResult = selectedTool?.handle(input)
    recordTime("tool_execution")
    
    val prompt = buildPrompt(input, toolResult)
    recordTime("prompt_build")
    
    val response = llm.generate(prompt)
    recordTime("llm_generation")
    
    memory.store("agent_response", response)
    recordTime("total")
    
    // Print timings
    timings.forEach { (label, time) ->
        println("$label: ${time}ms")
    }
    
    return response
}
```

---

## Edge Cases

### Case 1: Empty Input

```kotlin
chat("")  // What happens?

Result:
- Stored in memory: ✓
- Tools: Likely none match (canHandle("") = false)
- Prompt: "User: \nAI:"
- LLM: Generates response to empty input
```

### Case 2: Very Long Input

```kotlin
chat("A".repeat(10000))  // 10,000 characters

Result:
- Stored in memory: ✓ (memory has no size limit)
- Tools: Depend on implementation
- Prompt: Very long (may hit LLM limits)
- LLM: May truncate or error
```

### Case 3: Concurrent Calls

```kotlin
// Two parallel calls
launch { agent.chat("Query 1") }
launch { agent.chat("Query 2") }

Result:
- Memory: Thread-safe (Mutex)
- Tools: Stateless (safe)
- LLM: HTTP client handles concurrency
- Race condition: Possible in memory
  → agent1 reads "user_input" while agent2 is writing
```

**Recommendation**: Use separate agent instances per coroutine

---

## Multi-Agent Flow (with Graph)

### With SimpleAgentGraph

```
agent.chat(input)
    ↓
graph.run(input)
    ↓
agent1.chat(input)
    ├─ [Full single-agent flow]
    └─ returns output1
    ↓
agent2.chat(output1)
    ├─ [Full single-agent flow]
    └─ returns output2
    ↓
agent3.chat(output2)
    ├─ [Full single-agent flow]
    └─ returns output3
    ↓
return output3
```

### With ConditionalAgentGraph

```
agent.chat(input)
    ↓
graph.run(input)
    ↓
currentNode = startNode
while (true):
    output = currentNode.agent.chat(input)
    nextEdge = find edge where condition(output) = true
    if no nextEdge: return output
    currentNode = nextEdge.target
    input = output
```

---

## Summary

**Key Execution Points:**
1. Graph check (delegate if present)
2. Store input
3. Generate embedding (optional)
4. Select tool (filter → score → select)
5. Execute tool (if selected)
6. Build prompt
7. Generate response (with/without history)
8. Store response
9. Return

**Performance Bottlenecks:**
1. LLM generation (~1-3s)
2. Tool execution (varies)
3. Everything else (<100ms)

**Common Issues:**
1. Wrong tool selected → Check `canHandle()` and `score()`
2. Tool not used → Check `canHandle()` returns true
3. Slow execution → Profile each step
4. Inconsistent results → Check memory state

**Best Practices:**
1. Log tool selection for debugging
2. Profile slow operations
3. Use separate agents for concurrent requests
4. Test tool methods independently
5. Monitor memory usage over time
