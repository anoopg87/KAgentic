package llm

import kotlinx.coroutines.delay

/**
 * RetryHelper provides utility functions for retrying operations with exponential backoff.
 * Useful for handling transient network errors when calling LLM APIs.
 *
 * Usage Example:
 * ```kotlin
 * val result = retryWithExponentialBackoff(maxRetries = 3) {
 *     // Call API that might fail transiently
 *     callLLMAPI()
 * }
 * ```
 */
object RetryHelper {
    /**
     * Retries a suspending operation with exponential backoff.
     *
     * @param maxRetries Maximum number of retry attempts (default 3)
     * @param initialDelayMs Initial delay in milliseconds before first retry (default 1000ms)
     * @param maxDelayMs Maximum delay in milliseconds between retries (default 10000ms)
     * @param factor Exponential backoff factor (default 2.0)
     * @param retryOn Predicate to determine if exception should trigger retry (default: all exceptions)
     * @param block The suspending operation to retry
     * @return Result of successful operation
     * @throws Exception The last exception if all retries fail
     */
    suspend fun <T> retryWithExponentialBackoff(
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000,
        maxDelayMs: Long = 10000,
        factor: Double = 2.0,
        retryOn: (Exception) -> Boolean = { true },
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        var lastException: Exception? = null

        repeat(maxRetries + 1) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                lastException = e

                // Don't retry if this is the last attempt
                if (attempt >= maxRetries) {
                    throw e
                }

                // Don't retry if exception doesn't match retry condition
                if (!retryOn(e)) {
                    throw e
                }

                // Wait before retrying
                delay(currentDelay)

                // Increase delay exponentially, capped at maxDelay
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMs)
            }
        }

        // This should never be reached, but required for compilation
        throw lastException ?: Exception("Unknown error during retry")
    }
}

/**
 * Extension function for convenient retry with default parameters.
 */
suspend fun <T> retryWithBackoff(
    maxRetries: Int = 3,
    block: suspend () -> T
): T = RetryHelper.retryWithExponentialBackoff(maxRetries = maxRetries, block = block)
