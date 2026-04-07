---
name: create-agent-graph
description: Build multi-agent workflows by creating agent graphs with SimpleAgentGraphBuilder (sequential) or ConditionalAgentGraphBuilder (conditional branching). Use when creating complex orchestration workflows with multiple specialized agents.
version: "1.0.0"
---

# Create Agent Graph

Build complex multi-agent workflows using sequential or conditional graph patterns.

## When to Use This Skill

Trigger when you need to:
- "Create a workflow with multiple agents"
- "Build an agent graph"
- "Chain agents together"
- "Create conditional agent routing"
- "Build a multi-step agent pipeline"

## Overview

KAgentic supports two types of agent graphs:

1. **SimpleAgentGraph**: Linear, sequential execution (Agent A → Agent B → Agent C)
2. **ConditionalAgentGraph**: Dynamic routing with conditional branching

This skill helps you:
1. Choose the right graph type for your use case
2. Build graph structures with proper agent composition
3. Handle graph execution and state management
4. Test and debug multi-agent workflows

## Graph Types

### SimpleAgentGraph

**Use when**:
- You need sequential processing
- Each agent builds on previous results
- Workflow is always the same path
- Simple pipeline pattern

**Pattern**: Input → Agent1 → Agent2 → Agent3 → Output

### ConditionalAgentGraph

**Use when**:
- Workflow depends on agent responses
- Need branching logic
- Dynamic routing based on conditions
- Complex decision trees

**Pattern**: Input → Agent1 → (condition?) → Agent2A or Agent2B → ...

## Implementation Steps

### Step 1: Choose Graph Type

**Decision Matrix:**

| Use Case | Graph Type |
|----------|------------|
| Sequential pipeline | SimpleAgentGraph |
| Conditional branching | ConditionalAgentGraph |
| Fixed workflow | SimpleAgentGraph |
| Dynamic routing | ConditionalAgentGraph |
| Simple chaining | SimpleAgentGraph |
| Decision trees | ConditionalAgentGraph |

### Step 2: Create Specialized Agents

Build agents with specific capabilities:

```kotlin
import core.AgentFramework
import llm.OpenAILLM
import memory.ConversationMemory
import tools.*

// Analysis agent
val analyzerAgent = AgentFramework(
    llm = OpenAILLM(apiKey = System.getenv("OPENAI_API_KEY")),
    tools = listOf(FileReaderTool(), WebSearchTool()),
    memory = ConversationMemory()
)

// Processing agent
val processorAgent = AgentFramework(
    llm = OpenAILLM(apiKey = System.getenv("OPENAI_API_KEY")),
    tools = listOf(CalculatorTool(), APICallerTool()),
    memory = ConversationMemory()
)

// Summarization agent
val summarizerAgent = AgentFramework(
    llm = OpenAILLM(apiKey = System.getenv("OPENAI_API_KEY")),
    tools = listOf(),  // No tools needed
    memory = ConversationMemory()
)
```

### Step 3A: Build Simple Graph (Sequential)

```kotlin
import graph.SimpleAgentGraphBuilder
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    // Build graph
    val builder = SimpleAgentGraphBuilder()
    builder.addAgent(analyzerAgent)
           .addAgent(processorAgent)
           .addAgent(summarizerAgent)
    
    val graph = builder.build()
    
    // Execute
    val result = graph.run("Analyze sales data from file.csv and calculate totals")
    println(result)
}
```

**Execution Flow:**
1. analyzerAgent reads file and extracts data
2. processorAgent calculates totals
3. summarizerAgent creates final summary
4. Return result

### Step 3B: Build Conditional Graph (Branching)

```kotlin
import graph.ConditionalAgentGraphBuilder
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val builder = ConditionalAgentGraphBuilder()
    
    // Define nodes
    builder.addNode("analyze", analyzerAgent)
           .addNode("process", processorAgent)
           .addNode("summarize", summarizerAgent)
           .addNode("error_handler", errorAgent)
    
    // Define conditional edges
    builder.addEdge("analyze", "process") { response ->
        response.contains("data found", ignoreCase = true)
    }
    builder.addEdge("analyze", "error_handler") { response ->
        response.contains("error", ignoreCase = true)
    }
    builder.addEdge("process", "summarize") { response ->
        response.contains("calculation complete", ignoreCase = true)
    }
    
    // Build starting from "analyze"
    val graph = builder.build(startId = "analyze")
    
    // Execute
    val result = graph.run("Process financial report")
    println(result)
}
```

**Execution Flow:**
```
Input
  ↓
analyze
  ↓
  ├─→ (contains "data found") → process → summarize
  └─→ (contains "error") → error_handler
```

## Complete Examples

### Example 1: Data Processing Pipeline

```kotlin
package examples

import core.AgentFramework
import llm.OpenAILLM
import memory.ConversationMemory
import tools.*
import graph.SimpleAgentGraphBuilder
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val apiKey = System.getenv("OPENAI_API_KEY")
    
    // 1. Data fetcher agent
    val fetchAgent = AgentFramework(
        llm = OpenAILLM(apiKey),
        tools = listOf(FileReaderTool(), WebSearchTool()),
        memory = ConversationMemory()
    )
    
    // 2. Data processor agent
    val processAgent = AgentFramework(
        llm = OpenAILLM(apiKey),
        tools = listOf(CalculatorTool()),
        memory = ConversationMemory()
    )
    
    // 3. Report generator agent
    val reportAgent = AgentFramework(
        llm = OpenAILLM(apiKey),
        tools = listOf(),
        memory = ConversationMemory()
    )
    
    // Build sequential graph
    val graph = SimpleAgentGraphBuilder()
        .addAgent(fetchAgent)
        .addAgent(processAgent)
        .addAgent(reportAgent)
        .build()
    
    // Execute
    val report = graph.run("Generate monthly sales report from data.csv")
    println(report)
}
```

### Example 2: Conditional Customer Support Routing

```kotlin
package examples

import core.AgentFramework
import llm.OpenAILLM
import memory.ConversationMemory
import tools.*
import graph.ConditionalAgentGraphBuilder
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val apiKey = System.getenv("OPENAI_API_KEY")
    
    // Triage agent - classifies requests
    val triageAgent = AgentFramework(
        llm = OpenAILLM(apiKey),
        tools = listOf(),
        memory = ConversationMemory()
    )
    
    // Technical support agent
    val techAgent = AgentFramework(
        llm = OpenAILLM(apiKey),
        tools = listOf(FileReaderTool(), WebSearchTool()),
        memory = ConversationMemory()
    )
    
    // Billing support agent
    val billingAgent = AgentFramework(
        llm = OpenAILLM(apiKey),
        tools = listOf(APICallerTool(), CalculatorTool()),
        memory = ConversationMemory()
    )
    
    // General support agent
    val generalAgent = AgentFramework(
        llm = OpenAILLM(apiKey),
        tools = listOf(),
        memory = ConversationMemory()
    )
    
    // Build conditional graph
    val graph = ConditionalAgentGraphBuilder()
        .addNode("triage", triageAgent)
        .addNode("technical", techAgent)
        .addNode("billing", billingAgent)
        .addNode("general", generalAgent)
        .addEdge("triage", "technical") { response ->
            response.contains("technical", ignoreCase = true)
        }
        .addEdge("triage", "billing") { response ->
            response.contains("billing", ignoreCase = true) || 
            response.contains("payment", ignoreCase = true)
        }
        .addEdge("triage", "general") { response ->
            !response.contains("technical", ignoreCase = true) &&
            !response.contains("billing", ignoreCase = true)
        }
        .build("triage")
    
    // Execute
    val response = graph.run("I can't log into my account")
    println(response)
}
```

### Example 3: Research and Analysis Workflow

```kotlin
package examples

import core.AgentFramework
import llm.ClaudeLLM
import memory.ConversationMemory
import tools.WebSearchTool
import graph.ConditionalAgentGraphBuilder
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val apiKey = System.getenv("CLAUDE_API_KEY")
    
    // Research agent
    val researchAgent = AgentFramework(
        llm = ClaudeLLM(apiKey),
        tools = listOf(WebSearchTool()),
        memory = ConversationMemory()
    )
    
    // Fact checker agent
    val factCheckAgent = AgentFramework(
        llm = ClaudeLLM(apiKey),
        tools = listOf(WebSearchTool()),
        memory = ConversationMemory()
    )
    
    // Writer agent
    val writerAgent = AgentFramework(
        llm = ClaudeLLM(apiKey),
        tools = listOf(),
        memory = ConversationMemory()
    )
    
    // Editor agent
    val editorAgent = AgentFramework(
        llm = ClaudeLLM(apiKey),
        tools = listOf(),
        memory = ConversationMemory()
    )
    
    val graph = ConditionalAgentGraphBuilder()
        .addNode("research", researchAgent)
        .addNode("factcheck", factCheckAgent)
        .addNode("write", writerAgent)
        .addNode("edit", editorAgent)
        .addEdge("research", "factcheck") { it.contains("sources found") }
        .addEdge("factcheck", "write") { it.contains("verified") }
        .addEdge("factcheck", "research") { it.contains("needs more data") }
        .addEdge("write", "edit") { it.contains("draft complete") }
        .build("research")
    
    val article = graph.run("Write article about Kotlin coroutines")
    println(article)
}
```

## Testing Agent Graphs

### Test Simple Graph

```kotlin
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class AgentGraphTest {
    @Test
    fun testSimpleGraphExecution() = runBlocking {
        val mockLLM = object : LLMProvider {
            override suspend fun generate(input: String) = "Mock: $input"
        }
        
        val agent1 = AgentFramework(mockLLM, listOf(), ConversationMemory())
        val agent2 = AgentFramework(mockLLM, listOf(), ConversationMemory())
        
        val graph = SimpleAgentGraphBuilder()
            .addAgent(agent1)
            .addAgent(agent2)
            .build()
        
        val result = graph.run("test input")
        assertNotNull(result)
        assertTrue(result.contains("Mock"))
    }
}
```

### Test Conditional Graph

```kotlin
@Test
fun testConditionalGraphRouting() = runBlocking {
    val mockLLM = object : LLMProvider {
        override suspend fun generate(input: String) = "route to B"
    }
    
    val agentA = AgentFramework(mockLLM, listOf(), ConversationMemory())
    val agentB = AgentFramework(mockLLM, listOf(), ConversationMemory())
    
    val graph = ConditionalAgentGraphBuilder()
        .addNode("A", agentA)
        .addNode("B", agentB)
        .addEdge("A", "B") { it.contains("route to B") }
        .build("A")
    
    val result = graph.run("test")
    assertTrue(result.contains("route to B"))
}
```

## Best Practices

### 1. Agent Specialization

**✅ DO:**
```kotlin
// Each agent has specific tools and purpose
val dataAgent = AgentFramework(llm, listOf(FileReaderTool()), memory)
val calcAgent = AgentFramework(llm, listOf(CalculatorTool()), memory)
```

**❌ DON'T:**
```kotlin
// All agents have all tools - defeats purpose of specialization
val agent1 = AgentFramework(llm, allTools, memory)
val agent2 = AgentFramework(llm, allTools, memory)
```

### 2. Condition Design

**✅ DO:**
```kotlin
// Clear, specific conditions
.addEdge("A", "B") { response ->
    response.contains("success", ignoreCase = true) &&
    !response.contains("error", ignoreCase = true)
}
```

**❌ DON'T:**
```kotlin
// Vague or complex conditions
.addEdge("A", "B") { response ->
    response.length > 10  // Too generic
}
```

### 3. Error Handling

**✅ DO:**
```kotlin
// Add error handling nodes
builder.addNode("error_handler", errorAgent)
       .addEdge("process", "error_handler") { it.contains("error") }
```

### 4. Memory Management

Each agent in a graph should have its own memory instance:

```kotlin
// ✅ DO: Separate memory for each agent
val agent1 = AgentFramework(llm, tools, ConversationMemory())
val agent2 = AgentFramework(llm, tools, ConversationMemory())

// ❌ DON'T: Shared memory causes state leakage
val sharedMemory = ConversationMemory()
val agent1 = AgentFramework(llm, tools, sharedMemory)
val agent2 = AgentFramework(llm, tools, sharedMemory)
```

### 5. Graph Complexity

- Keep graphs simple (3-5 agents for simple, up to 10 for complex)
- Avoid circular dependencies in conditional graphs
- Test each agent independently before composing
- Document graph structure and flow

## Troubleshooting

**Issue: Graph doesn't route correctly**
- Check condition logic carefully
- Log responses to see actual values
- Verify condition functions return boolean
- Ensure all paths have valid edges

**Issue: Graph hangs or loops**
- Check for circular edges
- Ensure terminal conditions exist
- Add maximum iteration limits
- Verify each agent returns properly

**Issue: Unexpected results**
- Test each agent independently first
- Verify agent specialization is correct
- Check that memory is not shared
- Log intermediate results

**Issue: Performance problems**
- Reduce number of agents
- Parallelize independent agents (future feature)
- Cache LLM responses where appropriate
- Use lighter models for simple tasks

## Advanced Patterns

### Pattern 1: Fan-out/Fan-in

```kotlin
// Execute multiple branches, aggregate results
val graph = ConditionalAgentGraphBuilder()
    .addNode("split", splitterAgent)
    .addNode("process1", processor1)
    .addNode("process2", processor2)
    .addNode("merge", mergerAgent)
    .addEdge("split", "process1") { true }
    .addEdge("split", "process2") { true }
    // Future: Support parallel execution
    .build("split")
```

### Pattern 2: Retry Logic

```kotlin
// Retry failed operations
.addEdge("process", "retry") { it.contains("retry") }
.addEdge("retry", "process") { it.contains("attempt") }
```

### Pattern 3: Validation Pipeline

```kotlin
// Multi-step validation
.addNode("validate1", validator1)
.addNode("validate2", validator2)
.addEdge("validate1", "validate2") { it.contains("pass") }
.addEdge("validate1", "error") { it.contains("fail") }
```

## Related Skills

- **add-custom-tool**: Create tools for specialized agents
- **add-llm-provider**: Use different LLMs for different agents
- **understand-agent-flow**: Debug graph execution

## Reference Files

- Simple Graph Builder: `/home/user/KAgentic/agentic-library/src/main/kotlin/graph/SimpleAgentGraphBuilder.kt`
- Conditional Graph Builder: `/home/user/KAgentic/agentic-library/src/main/kotlin/graph/ConditionalAgentGraphBuilder.kt`
- Example: `/home/user/KAgentic/examples/multi-agent-graph/MultiAgentGraphExample.kt`
- Graph Types Reference: `.claude/skills/create-agent-graph/references/graph-types.md`
- More Examples: `.claude/skills/create-agent-graph/references/graph-examples.md`
