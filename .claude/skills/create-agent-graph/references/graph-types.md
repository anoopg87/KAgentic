### Graph Types Reference

## SimpleAgentGraph (Sequential)

**Purpose**: Linear workflow where agents execute in a fixed sequence.

**Source**: `/home/user/KAgentic/agentic-library/src/main/kotlin/graph/SimpleAgentGraphBuilder.kt`

### Implementation

```kotlin
class SimpleAgentGraphBuilder {
    private val agents = mutableListOf<AgentFramework>()

    fun addAgent(agent: AgentFramework): SimpleAgentGraphBuilder {
        agents.add(agent)
        return this
    }

    fun build(): AgentGraph {
        return SimpleAgentGraph(agents)
    }
}
```

### Characteristics

- **Execution**: Sequential, one after another
- **Flow**: Agent1 → Agent2 → Agent3 → ... → AgentN
- **Output**: Each agent receives previous agent's output as input
- **Terminal**: Returns after last agent completes
- **Complexity**: O(n) where n = number of agents

### When to Use

✅ **Use SimpleAgentGraph when:**
- You have a clear, linear process
- Each step depends on the previous step
- No branching or conditional logic needed
- Pipeline/assembly-line pattern

✅ **Examples:**
- Data fetching → Processing → Report generation
- Research → Writing → Editing
- Input validation → Transformation → Output formatting

❌ **Don't use SimpleAgentGraph when:**
- Need conditional routing based on outputs
- Different inputs require different workflows
- Need branching or decision trees

---

## ConditionalAgentGraph (Dynamic Routing)

**Purpose**: Dynamic workflow with conditional branching based on agent responses.

**Source**: `/home/user/KAgentic/agentic-library/src/main/kotlin/graph/ConditionalAgentGraphBuilder.kt`

### Implementation

```kotlin
class ConditionalAgentGraphBuilder {
    private val nodes = mutableListOf<Node>()
    private val edges = mutableListOf<Edge>()

    data class Node(val id: String, val agent: AgentFramework)
    data class Edge(val from: String, val to: String, val condition: (String) -> Boolean)

    fun addNode(id: String, agent: AgentFramework): ConditionalAgentGraphBuilder {
        nodes.add(Node(id, agent))
        return this
    }

    fun addEdge(from: String, to: String, condition: (String) -> Boolean): ConditionalAgentGraphBuilder {
        edges.add(Edge(from, to, condition))
        return this
    }

    fun build(startId: String): AgentGraph {
        return object : AgentGraph {
            override suspend fun run(input: String): String {
                var currentId = startId
                var currentInput = input
                val nodeMap = nodes.associateBy { it.id }
                while (true) {
                    val node = nodeMap[currentId] ?: break
                    val response = node.agent.chat(currentInput)
                    val nextEdge = edges.firstOrNull { 
                        it.from == currentId && it.condition(response) 
                    } ?: return response
                    currentId = nextEdge.to
                    currentInput = response
                }
                return currentInput
            }
        }
    }
}
```

### Characteristics

- **Execution**: Conditional, based on agent output
- **Flow**: Dynamic branching with decision points
- **Routing**: Edges define conditions for transitions
- **Terminal**: Returns when no matching edge found
- **Complexity**: O(n * m) where n = agents, m = average edges per node

### When to Use

✅ **Use ConditionalAgentGraph when:**
- Workflow depends on agent responses
- Need branching logic or decision trees
- Different inputs require different paths
- Complex routing rules

✅ **Examples:**
- Customer support routing (technical/billing/general)
- Data validation with error handling paths
- Multi-stage approval workflows
- Content moderation with different actions

❌ **Don't use ConditionalAgentGraph when:**
- Simple linear workflow (use SimpleAgentGraph)
- No conditional logic needed
- All inputs follow same path

---

## Comparison Matrix

| Feature | SimpleAgentGraph | ConditionalAgentGraph |
|---------|-----------------|----------------------|
| **Execution** | Sequential | Conditional |
| **Complexity** | Low | Medium-High |
| **Routing** | Fixed | Dynamic |
| **Branching** | None | Yes |
| **Setup** | Simple | More complex |
| **Use Case** | Pipelines | Decision trees |
| **Terminal Condition** | After last agent | No matching edge |
| **Memory per Agent** | Separate | Separate |

---

## Design Patterns

### Pattern 1: Simple Pipeline (SimpleAgentGraph)

```
Input → Fetch → Process → Format → Output
```

**Code:**
```kotlin
SimpleAgentGraphBuilder()
    .addAgent(fetchAgent)
    .addAgent(processAgent)
    .addAgent(formatAgent)
    .build()
```

### Pattern 2: Binary Decision (ConditionalAgentGraph)

```
Input → Classify → Valid? → Process
                 → Invalid? → Error
```

**Code:**
```kotlin
ConditionalAgentGraphBuilder()
    .addNode("classify", classifyAgent)
    .addNode("process", processAgent)
    .addNode("error", errorAgent)
    .addEdge("classify", "process") { it.contains("valid") }
    .addEdge("classify", "error") { it.contains("invalid") }
    .build("classify")
```

### Pattern 3: Multi-way Branch (ConditionalAgentGraph)

```
                 → Branch A
Input → Router → → Branch B
                 → Branch C
                 → Default
```

**Code:**
```kotlin
ConditionalAgentGraphBuilder()
    .addNode("router", routerAgent)
    .addNode("branchA", agentA)
    .addNode("branchB", agentB)
    .addNode("branchC", agentC)
    .addNode("default", defaultAgent)
    .addEdge("router", "branchA") { it.contains("typeA") }
    .addEdge("router", "branchB") { it.contains("typeB") }
    .addEdge("router", "branchC") { it.contains("typeC") }
    .addEdge("router", "default") { true }  // Catch-all
    .build("router")
```

### Pattern 4: Multi-Step with Retry (ConditionalAgentGraph)

```
Input → Step1 → Success? → Step2 → Done
              → Retry? → Step1 (again)
              → Error? → ErrorHandler
```

**Code:**
```kotlin
ConditionalAgentGraphBuilder()
    .addNode("step1", step1Agent)
    .addNode("step2", step2Agent)
    .addNode("error", errorAgent)
    .addEdge("step1", "step2") { it.contains("success") }
    .addEdge("step1", "step1") { it.contains("retry") }  // Loop back
    .addEdge("step1", "error") { it.contains("error") }
    .build("step1")
```

---

## Best Practices

### SimpleAgentGraph

1. **Keep it focused**: 3-5 agents is optimal
2. **Specialize agents**: Each agent should have distinct tools/purpose
3. **Test independently**: Verify each agent works alone first
4. **Chain logically**: Output of agent N should be valid input for agent N+1

### ConditionalAgentGraph

1. **Clear conditions**: Use specific keywords in conditions
2. **Avoid loops**: Watch for circular dependencies
3. **Have fallbacks**: Always include catch-all edges
4. **Test paths**: Verify all possible routing paths
5. **Document flow**: Complex graphs need clear documentation

### Both Types

1. **Separate memory**: Each agent should have its own ConversationMemory()
2. **Handle errors**: Add error handling nodes/agents
3. **Log intermediate**: Log outputs between agents for debugging
4. **Monitor performance**: Watch for slow agents in the chain
