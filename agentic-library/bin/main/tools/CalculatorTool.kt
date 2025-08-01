/**
 * CalculatorTool provides math expression evaluation for agentic workflows.
 * Uses exp4j for safe calculation.
 *
 * Usage Example:
 * ```kotlin
 * val calc = CalculatorTool()
 * val canHandle = calc.canHandle("2+2") // true
 * val result = runBlocking { calc.handle("2+2") } // "Result: 4.0"
 * ```
 */
package tools

import net.objecthunter.exp4j.ExpressionBuilder

class CalculatorTool: ToolHandler {
    override fun canHandle(input: String): Boolean {
        // Simple check: contains digits and math operators
        return input.matches(Regex("[\\d\\s\\+\\-\\*/\\(\\)\\.]+"))
    }

    override fun score(input: String): Int {
        // High score for explicit math expressions, medium for 'calculate', low otherwise
        return when {
            input.matches(Regex("[\\d\\s\\+\\-\\*/\\(\\)\\.]+")) -> 10
            input.contains("calculate", ignoreCase = true) -> 7
            else -> 1
        }
    }

    override suspend fun handle(input: String): String {
        return try {
            val result = ExpressionBuilder(input).build().evaluate()
            "Result: $result"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
