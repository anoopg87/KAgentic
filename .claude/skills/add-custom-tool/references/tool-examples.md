# Tool Examples from KAgentic

This document analyzes the 4 built-in tools in KAgentic to identify common patterns.

## 1. CalculatorTool

**Location:** `/home/user/KAgentic/agentic-library/src/main/kotlin/tools/CalculatorTool.kt`

**Purpose:** Evaluates mathematical expressions safely

**Pattern Matching:**
```kotlin
override fun canHandle(input: String): Boolean {
    return input.matches(Regex("[\\d\\s\\+\\-\\*/\\(\\)\\.]+"))
}
```

**Scoring:**
```kotlin
override fun score(input: String): Int {
    return when {
        input.matches(Regex("[\\d\\s\\+\\-\\*/\\(\\)\\.]+")) -> 10
        input.contains("calculate", ignoreCase = true) -> 7
        else -> 1
    }
}
```

**Key Features:**
- Uses exp4j library for safe evaluation
- Simple regex for pure math expressions
- High score (10) for exact math patterns
- Medium score (7) for "calculate" keyword

## 2. WebSearchTool

**Location:** `/home/user/KAgentic/agentic-library/src/main/kotlin/tools/WebSearchTool.kt`

**Purpose:** Performs web searches via DuckDuckGo API

**Pattern Matching:**
```kotlin
override fun canHandle(input: String): Boolean {
    return input.contains("search", ignoreCase = true) || 
           input.contains("find", ignoreCase = true)
}
```

**Scoring:**
```kotlin
override fun score(input: String): Int {
    return when {
        input.contains("search", ignoreCase = true) -> 10
        input.contains("find", ignoreCase = true) -> 7
        else -> 1
    }
}
```

**Key Features:**
- Keyword-based matching ("search", "find")
- HTTP GET request to external API
- URL encoding for query parameters
- Connection management (disconnect after use)

## 3. FileReaderTool

**Location:** `/home/user/KAgentic/agentic-library/src/main/kotlin/tools/FileReaderTool.kt`

**Purpose:** Reads local text files

**Pattern Matching:**
```kotlin
override fun canHandle(input: String): Boolean {
    return input.contains("read file", ignoreCase = true) || 
           input.matches(Regex(".*\\.(txt|md|csv)$"))
}
```

**Scoring:**
```kotlin
override fun score(input: String): Int {
    return when {
        input.contains("read file", ignoreCase = true) -> 10
        input.matches(Regex(".*\\.(txt|md|csv)$")) -> 8
        else -> 1
    }
}
```

**Key Features:**
- Combined keyword + file extension matching
- File existence validation
- Support for multiple file types (.txt, .md, .csv)
- Descriptive error messages

## 4. APICallerTool

**Location:** `/home/user/KAgentic/agentic-library/src/main/kotlin/tools/APICallerTool.kt`

**Purpose:** Makes HTTP GET requests to APIs

**Pattern Matching:**
```kotlin
override fun canHandle(input: String): Boolean {
    return input.contains("call api", ignoreCase = true) || 
           input.startsWith("http")
}
```

**Scoring:**
```kotlin
override fun score(input: String): Int {
    return when {
        input.contains("call api", ignoreCase = true) -> 10
        input.startsWith("http") -> 8
        else -> 1
    }
}
```

**Key Features:**
- Keyword + URL pattern matching
- Generic HTTP client (HttpURLConnection)
- Handles any HTTP-based API
- Simple GET request implementation

## Common Patterns Across All Tools

### 1. Pattern Matching Strategy
- **Keyword matching**: Most common (search, find, read file, call api)
- **Regex patterns**: For format validation (math expressions, file extensions, URLs)
- **Combined approach**: Keywords OR patterns for flexibility

### 2. Scoring Strategy
- **10**: Explicit/exact match (keyword or format)
- **7-8**: Strong secondary match
- **1**: Default fallback

### 3. Error Handling
- All tools use try-catch blocks
- Return error messages as strings (not exceptions)
- Include specific error details in messages

### 4. Implementation Patterns
- API tools: Use HttpURLConnection or Ktor
- File tools: Use java.io.File
- Computation tools: Use third-party libraries (exp4j)

### 5. Async Support
- All handle() methods are suspend functions
- Allow for I/O operations without blocking

## Usage Examples

### Example 1: Math Calculation
```kotlin
val calc = CalculatorTool()
println(calc.canHandle("2+2"))  // true
println(calc.score("2+2"))      // 10
val result = runBlocking { calc.handle("2+2") }
println(result)  // "Result: 4.0"
```

### Example 2: Web Search
```kotlin
val search = WebSearchTool()
println(search.canHandle("search Kotlin"))  // true
println(search.score("search Kotlin"))      // 10
val result = runBlocking { search.handle("search Kotlin") }
// Returns JSON from DuckDuckGo
```

### Example 3: File Reading
```kotlin
val reader = FileReaderTool()
println(reader.canHandle("readme.md"))  // true
println(reader.score("readme.md"))      // 8
val content = runBlocking { reader.handle("readme.md") }
// Returns file contents or error
```

### Example 4: API Call
```kotlin
val api = APICallerTool()
println(api.canHandle("https://api.github.com"))  // true
println(api.score("https://api.github.com"))      // 8
val response = runBlocking { api.handle("https://api.github.com") }
// Returns API response
```

## Design Recommendations

Based on these 4 tools:

1. **Keep canHandle() fast** - No expensive operations
2. **Use clear scoring hierarchy** - 10 for exact, 7-8 for strong, 1 for weak
3. **Validate inputs** - Check before processing
4. **Descriptive errors** - Help users understand what went wrong
5. **Document with KDoc** - Include usage examples
6. **Test thoroughly** - Cover positive and negative cases
