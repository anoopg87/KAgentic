## Graph Examples Reference

Complete, working examples of agent graphs from the KAgentic project.

---

## Example 1: Basic Multi-Agent Graph (Sequential)

**Source**: `/home/user/KAgentic/examples/multi-agent-graph/MultiAgentGraphExample.kt`

**Purpose**: Demonstrates sequential agent chaining with specialized agents.

```kotlin
import core.AgentFramework
import llm.LLMProvider
import memory.ConversationMemory
import tools.CalculatorTool
import tools.WebSearchTool
import graph.SimpleAgentGraph
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    // Mock LLM for demonstration
    val mockLLM = object : LLMProvider {
        override suspend fun generate(input: String): String {
            return "Processed: $input"
        }
    }

    // Agent 1: Calculator specialist
    val calculatorAgent = AgentFramework(
        llm = mockLLM,
        tools = listOf(CalculatorTool()),
        memory = ConversationMemory()
    )

    // Agent 2: Web search specialist
    val searchAgent = AgentFramework(
        llm = mockLLM,
        tools = listOf(WebSearchTool()),
        memory = ConversationMemory()
    )

    // Agent 3: General purpose agent
    val generalAgent = AgentFramework(
        llm = mockLLM,
        tools = listOf(CalculatorTool(), WebSearchTool()),
        memory = ConversationMemory()
    )

    // Create the agent graph
    val graph = SimpleAgentGraph(
        agents = listOf(calculatorAgent, searchAgent, generalAgent)
    )

    // Execute queries
    val response1 = graph.run("100 + 250 * 2")
    println("Response: $response1")
}
```

**Key Points:**
- Each agent has specialized tools
- Sequential execution through 3 agents
- Separate memory per agent
- Mock LLM for testing

---

## Example 2: Data Processing Pipeline

**Use Case**: ETL (Extract, Transform, Load) workflow

```kotlin
import core.AgentFramework
import llm.OpenAILLM
import memory.ConversationMemory
import tools.*
import graph.SimpleAgentGraphBuilder
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val apiKey = System.getenv("OPENAI_API_KEY")

    // 1. Extractor: Reads data from various sources
    val extractorAgent = AgentFramework(
        llm = OpenAILLM(apiKey),
        tools = listOf(
            FileReaderTool(),      // Read local files
            WebSearchTool(),       // Fetch web data
            APICallerTool()        // Call external APIs
        ),
        memory = ConversationMemory()
    )

    // 2. Transformer: Processes and calculates
    val transformerAgent = AgentFramework(
        llm = OpenAILLM(apiKey),
        tools = listOf(
            CalculatorTool()       // Perform calculations
        ),
        memory = ConversationMemory()
    )

    // 3. Loader: Formats and outputs results
    val loaderAgent = AgentFramework(
        llm = OpenAILLM(apiKey),
        tools = listOf(),          // Pure LLM for formatting
        memory = ConversationMemory()
    )

    // Build pipeline
    val pipeline = SimpleAgentGraphBuilder()
        .addAgent(extractorAgent)
        .addAgent(transformerAgent)
        .addAgent(loaderAgent)
        .build()

    // Execute
    val result = pipeline.run("Process sales data from Q4 report")
    println(result)
}
```

**Flow:**
```
Input: "Process sales data from Q4 report"
  ↓
ExtractorAgent: Reads data from file/API
  ↓ Output: Raw data with numbers
TransformerAgent: Calculates totals, averages
  ↓ Output: Computed metrics
LoaderAgent: Formats into readable report
  ↓ Output: Final formatted report
```

---

## Example 3: Customer Support Router (Conditional)

**Use Case**: Route customer inquiries to specialized support agents

```kotlin
import core.AgentFramework
import llm.ClaudeLLM
import memory.ConversationMemory
import tools.*
import graph.ConditionalAgentGraphBuilder
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val apiKey = System.getenv("CLAUDE_API_KEY")

    // Triage agent: Classifies the request type
    val triageAgent = AgentFramework(
        llm = ClaudeLLM(apiKey),
        tools = listOf(),  // No tools needed for classification
        memory = ConversationMemory()
    )

    // Technical support: Handles technical issues
    val technicalAgent = AgentFramework(
        llm = ClaudeLLM(apiKey),
        tools = listOf(
            FileReaderTool(),   // Access documentation
            WebSearchTool()     // Search solutions
        ),
        memory = ConversationMemory()
    )

    // Billing support: Handles payment/billing
    val billingAgent = AgentFramework(
        llm = ClaudeLLM(apiKey),
        tools = listOf(
            APICallerTool(),    // Call billing API
            CalculatorTool()    // Calculate costs
        ),
        memory = ConversationMemory()
    )

    // General support: Handles everything else
    val generalAgent = AgentFramework(
        llm = ClaudeLLM(apiKey),
        tools = listOf(
            FileReaderTool(),   // Access FAQs
            WebSearchTool()     // General search
        ),
        memory = ConversationMemory()
    )

    // Build conditional graph
    val supportSystem = ConditionalAgentGraphBuilder()
        .addNode("triage", triageAgent)
        .addNode("technical", technicalAgent)
        .addNode("billing", billingAgent)
        .addNode("general", generalAgent)
        
        // Route based on triage response
        .addEdge("triage", "technical") { response ->
            response.contains("technical", ignoreCase = true) ||
            response.contains("bug", ignoreCase = true) ||
            response.contains("error", ignoreCase = true)
        }
        .addEdge("triage", "billing") { response ->
            response.contains("billing", ignoreCase = true) ||
            response.contains("payment", ignoreCase = true) ||
            response.contains("invoice", ignoreCase = true)
        }
        .addEdge("triage", "general") { response ->
            !response.contains("technical", ignoreCase = true) &&
            !response.contains("billing", ignoreCase = true)
        }
        .build("triage")

    // Test different queries
    val queries = listOf(
        "I can't log into my account",
        "How do I update my credit card?",
        "What are your business hours?"
    )

    for (query in queries) {
        println("\nQuery: $query")
        val response = supportSystem.run(query)
        println("Response: $response")
    }
}
```

**Flow:**
```
"I can't log into my account"
  ↓
TriageAgent: "This is a technical issue"
  ↓
Contains "technical" → Route to TechnicalAgent
  ↓
TechnicalAgent: Searches docs, provides solution
  ↓
Final response returned
```

---

## Example 4: Content Creation Workflow (Sequential)

**Use Case**: Research → Write → Edit pipeline

```kotlin
import core.AgentFramework
import llm.GeminiLLM
import memory.ConversationMemory
import tools.WebSearchTool
import graph.SimpleAgentGraphBuilder
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val apiKey = System.getenv("GEMINI_API_KEY")

    // 1. Researcher: Gathers information
    val researchAgent = AgentFramework(
        llm = GeminiLLM(apiKey),
        tools = listOf(WebSearchTool()),
        memory = ConversationMemory()
    )

    // 2. Writer: Creates draft content
    val writerAgent = AgentFramework(
        llm = GeminiLLM(apiKey),
        tools = listOf(),  // Pure writing, no tools
        memory = ConversationMemory()
    )

    // 3. Editor: Polishes and improves
    val editorAgent = AgentFramework(
        llm = GeminiLLM(apiKey),
        tools = listOf(),  // Pure editing, no tools
        memory = ConversationMemory()
    )

    // Build content pipeline
    val contentPipeline = SimpleAgentGraphBuilder()
        .addAgent(researchAgent)
        .addAgent(writerAgent)
        .addAgent(editorAgent)
        .build()

    // Create article
    val article = contentPipeline.run(
        "Write a blog post about Kotlin coroutines"
    )
    
    println(article)
}
```

**Flow:**
```
Input: "Write a blog post about Kotlin coroutines"
  ↓
ResearchAgent: Searches web for info about coroutines
  ↓ Output: "Found: coroutines are for async programming..."
WriterAgent: Drafts blog post using research
  ↓ Output: "Draft: Kotlin Coroutines: A Guide..."
EditorAgent: Polishes grammar, structure, clarity
  ↓ Output: "Final: Kotlin Coroutines: Your Complete Guide..."
```

---

## Example 5: Data Validation with Error Handling (Conditional)

**Use Case**: Validate → Process or Retry → Output

```kotlin
import core.AgentFramework
import llm.OpenAILLM
import memory.ConversationMemory
import tools.*
import graph.ConditionalAgentGraphBuilder
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val apiKey = System.getenv("OPENAI_API_KEY")

    // Validator: Checks data quality
    val validatorAgent = AgentFramework(
        llm = OpenAILLM(apiKey),
        tools = listOf(FileReaderTool()),
        memory = ConversationMemory()
    )

    // Processor: Handles valid data
    val processorAgent = AgentFramework(
        llm = OpenAILLM(apiKey),
        tools = listOf(CalculatorTool()),
        memory = ConversationMemory()
    )

    // Error handler: Deals with invalid data
    val errorAgent = AgentFramework(
        llm = OpenAILLM(apiKey),
        tools = listOf(),
        memory = ConversationMemory()
    )

    // Retrier: Attempts to fix and retry
    val retryAgent = AgentFramework(
        llm = OpenAILLM(apiKey),
        tools = listOf(FileReaderTool()),
        memory = ConversationMemory()
    )

    val workflow = ConditionalAgentGraphBuilder()
        .addNode("validate", validatorAgent)
        .addNode("process", processorAgent)
        .addNode("error", errorAgent)
        .addNode("retry", retryAgent)
        
        .addEdge("validate", "process") { response ->
            response.contains("valid", ignoreCase = true)
        }
        .addEdge("validate", "retry") { response ->
            response.contains("fixable", ignoreCase = true)
        }
        .addEdge("validate", "error") { response ->
            response.contains("invalid", ignoreCase = true) &&
            !response.contains("fixable", ignoreCase = true)
        }
        .addEdge("retry", "validate") { response ->
            response.contains("retry_validation", ignoreCase = true)
        }
        .build("validate")

    val result = workflow.run("Process data file: data.csv")
    println(result)
}
```

**Flow:**
```
Input: "Process data file"
  ↓
ValidateAgent: Checks data
  ↓
  ├─→ "valid" → ProcessAgent → Done
  ├─→ "fixable" → RetryAgent → ValidateAgent (again)
  └─→ "invalid" → ErrorAgent → Error message
```

---

## Example 6: Multi-Stage Approval Workflow (Conditional)

**Use Case**: Document review with multiple approvers

```kotlin
val approvalWorkflow = ConditionalAgentGraphBuilder()
    .addNode("initial_review", initialReviewerAgent)
    .addNode("manager_review", managerAgent)
    .addNode("director_review", directorAgent)
    .addNode("approved", approvalAgent)
    .addNode("rejected", rejectionAgent)
    
    // Initial review decision
    .addEdge("initial_review", "manager_review") { 
        it.contains("approve") 
    }
    .addEdge("initial_review", "rejected") { 
        it.contains("reject") 
    }
    
    // Manager review decision
    .addEdge("manager_review", "director_review") {
        it.contains("needs_director_approval")
    }
    .addEdge("manager_review", "approved") {
        it.contains("manager_approved")
    }
    .addEdge("manager_review", "rejected") {
        it.contains("reject")
    }
    
    // Director final decision
    .addEdge("director_review", "approved") {
        it.contains("director_approved")
    }
    .addEdge("director_review", "rejected") {
        it.contains("reject")
    }
    .build("initial_review")
```

---

## Common Patterns Summary

### 1. Linear Pipeline (Simple)
```
A → B → C → D
```
**Use**: ETL, content creation, sequential processing

### 2. Binary Decision (Conditional)
```
A → [condition] → B (true)
              → C (false)
```
**Use**: Validation, binary classification

### 3. Multi-way Router (Conditional)
```
A → [condition1] → B
  → [condition2] → C
  → [condition3] → D
  → [default] → E
```
**Use**: Request routing, classification

### 4. Retry Loop (Conditional)
```
A → [success] → B → Done
  → [retry] → A (loop back)
  → [error] → C (error handler)
```
**Use**: Fault tolerance, data processing

### 5. Parallel Branches (Conditional)
```
A → B → [merge]
  → C → [merge] → D
```
**Use**: Parallel processing (with manual merging)

### 6. Multi-Stage (Conditional)
```
A → B → [check] → C
      → [check] → D
C → [final] → E
D → [final] → E
```
**Use**: Approval workflows, staged processing

---

## Testing Patterns

### Unit Test for SimpleAgentGraph

```kotlin
@Test
fun testSimpleGraph() = runBlocking {
    val mockLLM = object : LLMProvider {
        override suspend fun generate(input: String) = "mock: $input"
    }
    
    val agent1 = AgentFramework(mockLLM, listOf(), ConversationMemory())
    val agent2 = AgentFramework(mockLLM, listOf(), ConversationMemory())
    
    val graph = SimpleAgentGraphBuilder()
        .addAgent(agent1)
        .addAgent(agent2)
        .build()
    
    val result = graph.run("test")
    assertNotNull(result)
}
```

### Unit Test for ConditionalAgentGraph

```kotlin
@Test
fun testConditionalRouting() = runBlocking {
    val mockLLM = object : LLMProvider {
        override suspend fun generate(input: String) = "route_to_B"
    }
    
    val agentA = AgentFramework(mockLLM, listOf(), ConversationMemory())
    val agentB = AgentFramework(mockLLM, listOf(), ConversationMemory())
    
    val graph = ConditionalAgentGraphBuilder()
        .addNode("A", agentA)
        .addNode("B", agentB)
        .addEdge("A", "B") { it.contains("route_to_B") }
        .build("A")
    
    val result = graph.run("test")
    assertTrue(result.contains("route_to_B"))
}
```

---

## Tips for Building Effective Graphs

1. **Start Simple**: Begin with SimpleAgentGraph, upgrade to Conditional if needed
2. **Test Agents First**: Verify each agent works independently before composing
3. **Clear Conditions**: Use specific, testable condition logic
4. **Avoid Loops**: Watch for infinite loops in conditional graphs
5. **Log Intermediate**: Print outputs between agents during development
6. **Specialize Tools**: Give each agent only the tools it needs
7. **Separate Memory**: Each agent gets its own ConversationMemory instance
8. **Handle Errors**: Always include error handling paths
9. **Document Flow**: Draw diagrams of complex conditional graphs
10. **Monitor Performance**: Profile slow agents in production
