import core.AgentFramework
import llm.LLMProvider
import memory.ConversationMemory
import tools.ToolHandler
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Custom Tool Example
 *
 * This example demonstrates how to create your own custom tools
 * by implementing the ToolHandler interface.
 */

/**
 * Custom tool that provides the current date and time
 */
class DateTimeTool : ToolHandler {
    override fun canHandle(input: String): Boolean {
        return input.contains("time", ignoreCase = true) ||
               input.contains("date", ignoreCase = true) ||
               input.contains("today", ignoreCase = true)
    }

    override fun score(input: String): Int {
        return when {
            input.contains("what time", ignoreCase = true) -> 10
            input.contains("what date", ignoreCase = true) -> 10
            input.contains("today", ignoreCase = true) -> 8
            input.contains("time", ignoreCase = true) -> 5
            input.contains("date", ignoreCase = true) -> 5
            else -> 1
        }
    }

    override suspend fun handle(input: String): String {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return "Current date and time: ${now.format(formatter)}"
    }
}

/**
 * Custom tool that provides weather information (mock)
 */
class WeatherTool : ToolHandler {
    override fun canHandle(input: String): Boolean {
        return input.contains("weather", ignoreCase = true)
    }

    override fun score(input: String): Int {
        return when {
            input.contains("what's the weather", ignoreCase = true) -> 10
            input.contains("weather", ignoreCase = true) -> 8
            else -> 1
        }
    }

    override suspend fun handle(input: String): String {
        // Mock weather data (in real implementation, call weather API)
        return "Weather: Sunny, 72°F (22°C), Light breeze"
    }
}

/**
 * Custom tool that translates text (mock)
 */
class TranslatorTool : ToolHandler {
    override fun canHandle(input: String): Boolean {
        return input.contains("translate", ignoreCase = true)
    }

    override fun score(input: String): Int {
        return when {
            input.startsWith("translate", ignoreCase = true) -> 10
            input.contains("translate", ignoreCase = true) -> 7
            else -> 1
        }
    }

    override suspend fun handle(input: String): String {
        // Mock translation (in real implementation, call translation API)
        val text = input.removePrefix("translate").trim()
        return "Translated: '$text' -> '¡Hola! (Spanish mock translation)"
    }
}

fun main() = runBlocking {
    println("=== Custom Tool Example ===\n")

    // Create mock LLM
    val mockLLM = object : LLMProvider {
        override suspend fun generate(input: String): String {
            return "Processed with custom tools: $input"
        }
    }

    // Create custom tools
    val customTools = listOf(
        DateTimeTool(),
        WeatherTool(),
        TranslatorTool()
    )

    // Create agent with custom tools
    val agent = AgentFramework(
        llm = mockLLM,
        tools = customTools,
        memory = ConversationMemory()
    )

    // Test custom tools
    println("Query 1: What time is it?")
    val response1 = agent.chat("What time is it?")
    println("Response: $response1\n")

    println("Query 2: What's the weather today?")
    val response2 = agent.chat("What's the weather today?")
    println("Response: $response2\n")

    println("Query 3: Translate 'Hello, how are you?'")
    val response3 = agent.chat("Translate 'Hello, how are you?'")
    println("Response: $response3\n")

    println("=== Custom tools demonstration complete ===")
    println("\nKey Takeaways:")
    println("1. Implement ToolHandler interface with canHandle, score, and handle methods")
    println("2. Use scoring to prioritize tools when multiple can handle the same input")
    println("3. Tools can integrate with external APIs (weather, translation, etc.)")
    println("4. Tools are automatically selected based on user input")
}
