# KAgentic Examples

This directory contains examples demonstrating how to use the KAgentic framework.

## Prerequisites

Before running the examples, make sure you have:

1. JDK 17 or higher installed
2. Gradle 8.2 or higher installed
3. API keys for any LLM providers you want to use (OpenAI, Gemini, Claude, etc.)

## Running Examples

### Basic Agent Example

The basic agent example shows how to create a simple agent with tools and memory.

```bash
cd examples/basic-agent
kotlinc BasicAgentExample.kt -include-runtime -d BasicAgentExample.jar -cp ../../agentic-library/build/libs/*
java -jar BasicAgentExample.jar
```

**What it demonstrates:**
- Creating an agent with LLM, tools, and memory
- Using CalculatorTool for math operations
- Using WebSearchTool for web queries
- Viewing conversation history

### Multi-Agent Graph Example

The multi-agent graph example shows how to chain multiple specialized agents together.

```bash
cd examples/multi-agent-graph
kotlinc MultiAgentGraphExample.kt -include-runtime -d MultiAgentGraphExample.jar -cp ../../agentic-library/build/libs/*
java -jar MultiAgentGraphExample.jar
```

**What it demonstrates:**
- Creating specialized agents for different tasks
- Chaining agents together with SimpleAgentGraph
- Automatic routing of queries to the right agent
- Complex multi-step workflows

### Custom Tools Example

The custom tools example shows how to create your own tools.

```bash
cd examples/custom-tools
kotlinc CustomToolExample.kt -include-runtime -d CustomToolExample.jar -cp ../../agentic-library/build/libs/*
java -jar CustomToolExample.jar
```

**What it demonstrates:**
- Implementing the ToolHandler interface
- Creating custom tools (DateTime, Weather, Translator)
- Tool scoring and selection
- Integrating with external APIs

## Environment Variables

Some examples require API keys. Set them as environment variables:

```bash
export OPENAI_API_KEY="your-openai-key"
export GEMINI_API_KEY="your-gemini-key"
export CLAUDE_API_KEY="your-claude-key"
```

## Code Structure

Each example follows this structure:

1. **Setup**: Configure LLM, tools, and memory
2. **Agent Creation**: Create AgentFramework with components
3. **Usage**: Send queries and receive responses
4. **Output**: Display results and conversation history

## Best Practices

1. **API Key Security**: Never hardcode API keys. Use environment variables.
2. **Error Handling**: Wrap agent calls in try-catch blocks for production use.
3. **Tool Selection**: Use scoring to prioritize tools when multiple match.
4. **Memory Management**: Clear memory when needed to avoid context bloat.
5. **Testing**: Test agents with mock LLMs before using real API calls.

## Next Steps

After running the examples, you can:

- Customize the examples for your use case
- Create your own custom tools
- Build more complex agent graphs
- Integrate with your application
- Add error handling and retry logic
- Implement rate limiting for API calls

## Support

For questions or issues, please visit:
- GitHub: https://github.com/anoopg87/KAgentic
- Documentation: See README.md in the root directory
