---
name: add-vector-store
description: Add a new vector store backend to the KAgentic framework by implementing the VectorStore interface. Use when the user wants to integrate a vector database (like Weaviate, pgvector, Chroma, Milvus) for RAG or semantic search. This skill guides through implementation, metadata handling, and testing.
version: "1.0.0"
---

# Add Vector Store

Integrate new vector database backends into KAgentic for RAG and semantic search.

## When to Use This Skill

Trigger when you need to:
- "Add support for [vector database]"
- "Integrate [Weaviate/pgvector/Chroma/Milvus] as a vector store"
- "How do I plug in a custom vector database?"
- "Create a vector store for [service]"

## Overview

This skill helps you:
1. Implement the `VectorStore` interface (5 methods)
2. Handle document upsert and batch operations
3. Implement semantic search with metadata filtering
4. Store and retrieve document text alongside vectors
5. Write tests for the new backend

## VectorStore Interface

```kotlin
interface VectorStore {
    suspend fun upsert(document: VectorDocument)
    suspend fun upsertAll(documents: List<VectorDocument>)
    suspend fun search(query: List<Float>, topK: Int, filter: Map<String, String> = emptyMap()): List<VectorSearchResult>
    suspend fun delete(id: String)
    suspend fun deleteAll()
}
```

## Key Data Types

```kotlin
data class VectorDocument(
    val id: String,
    val vector: List<Float>,
    val text: String? = null,
    val metadata: Map<String, String> = emptyMap()
)

data class VectorSearchResult(
    val id: String,
    val score: Float,
    val text: String? = null,
    val metadata: Map<String, String> = emptyMap()
)
```

## Implementation Steps

### Step 1: Create a New Module

```
kagentic-vector-yourdb/
├── build.gradle.kts
└── src/
    ├── main/kotlin/vectorstore/yourdb/YourDbVectorStore.kt
    └── test/kotlin/vectorstore/yourdb/YourDbVectorStoreTest.kt
```

**build.gradle.kts:**
```kotlin
plugins {
    kotlin("jvm") version "1.9.23"
    id("maven-publish")
}

group = "com.agentic"
version = "0.1.0"

repositories { mavenCentral() }

dependencies {
    implementation(project(":agentic-library"))
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.23")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
}

kotlin { jvmToolchain(21) }
tasks.test { useJUnitPlatform() }
```

**Register in `settings.gradle.kts`:**
```kotlin
include(":kagentic-vector-yourdb")
```

### Step 2: Implement the VectorStore

```kotlin
package vectorstore.yourdb

import core.Logger
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import llm.retryWithBackoff
import vectorstore.VectorDocument
import vectorstore.VectorSearchResult
import vectorstore.VectorStore

class YourDbVectorStore(
    private val host: String,
    private val collectionName: String,
    private val apiKey: String? = null,
    private val logger: Logger? = null,
    private val logEnabled: Boolean = false
) : VectorStore {

    private val client = HttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun upsert(document: VectorDocument) = upsertAll(listOf(document))

    override suspend fun upsertAll(documents: List<VectorDocument>) {
        if (documents.isEmpty()) return
        // Build request body per your DB's API spec
        // Store doc.text under TEXT_PAYLOAD_KEY in the metadata/payload
        retryWithBackoff(maxRetries = 3) {
            val response = client.post("$host/collections/$collectionName/points") {
                apiKey?.let { header("api-key", it) }
                contentType(ContentType.Application.Json)
                setBody(buildUpsertBody(documents))
            }
            checkResponse(response, "upsert")
        }
    }

    override suspend fun search(
        query: List<Float>,
        topK: Int,
        filter: Map<String, String>
    ): List<VectorSearchResult> {
        require(topK > 0) { "topK must be greater than 0" }
        return retryWithBackoff(maxRetries = 3) {
            val response = client.post("$host/collections/$collectionName/points/search") {
                apiKey?.let { header("api-key", it) }
                contentType(ContentType.Application.Json)
                setBody(buildSearchBody(query, topK, filter))
            }
            checkResponse(response, "search")
            parseSearchResponse(response.bodyAsText())
        }
    }

    override suspend fun delete(id: String) {
        retryWithBackoff(maxRetries = 3) {
            val response = client.post("$host/collections/$collectionName/points/delete") {
                apiKey?.let { header("api-key", it) }
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("points", buildJsonArray { add(id) })
                }.toString())
            }
            checkResponse(response, "delete")
        }
    }

    override suspend fun deleteAll() {
        // Per your DB's API: truncate collection, clear namespace, etc.
    }

    private fun buildUpsertBody(documents: List<VectorDocument>): String { TODO() }
    private fun buildSearchBody(query: List<Float>, topK: Int, filter: Map<String, String>): String { TODO() }
    private fun parseSearchResponse(body: String): List<VectorSearchResult> { TODO() }

    private suspend fun checkResponse(response: HttpResponse, op: String) {
        if (!response.status.isSuccess()) {
            throw IllegalStateException("YourDb $op failed [${response.status}]: ${response.bodyAsText()}")
        }
    }

    companion object {
        const val TEXT_PAYLOAD_KEY = "__text__"
    }
}
```

### Step 3: Text Storage Pattern

All built-in stores use a reserved payload key `__text__` to store the document's original text alongside the vector. Follow this pattern:

**On upsert:**
```kotlin
doc.text?.let { put(TEXT_PAYLOAD_KEY, it) }  // store in metadata/payload
```

**On search result parsing:**
```kotlin
val text = payload?.get(TEXT_PAYLOAD_KEY)?.jsonPrimitive?.content
val metaMap = payload
    ?.filterKeys { it != TEXT_PAYLOAD_KEY }   // strip internal key
    ?.mapValues { (_, v) -> v.jsonPrimitive.content }
    ?: emptyMap()
```

### Step 4: Wire Into AgentFramework (RAG)

Once implemented, plug it into an agent for automatic RAG:

```kotlin
val embedder = OpenAIEmbeddingProvider(apiKey = System.getenv("OPENAI_API_KEY"))
val store = YourDbVectorStore(host = "http://localhost:8080", collectionName = "docs")

val agent = AgentFramework(
    llm = ClaudeLLM(apiKey = System.getenv("CLAUDE_API_KEY")),
    embeddingProvider = embedder,
    vectorStore = store,
    ragTopK = 5
)
```

AgentFramework will automatically embed the user input, retrieve top-K documents, and inject them as context before calling the LLM.

### Step 5: Write Tests

```kotlin
class YourDbVectorStoreTest {

    @Test
    fun testInstantiation_NoApiKey() {
        val store = YourDbVectorStore(host = "http://localhost:8080", collectionName = "test")
        assertNotNull(store)
    }

    @Test
    fun testInstantiation_WithApiKey() {
        val store = YourDbVectorStore(
            host = "https://cloud.yourdb.io",
            collectionName = "prod",
            apiKey = "test-key"
        )
        assertNotNull(store)
    }

    @Test
    fun testImplementsVectorStore() {
        val store: VectorStore = YourDbVectorStore(host = "http://localhost:8080", collectionName = "test")
        assertNotNull(store)
    }

    @Test
    fun testTextPayloadKey_IsStable() {
        assertEquals("__text__", YourDbVectorStore.TEXT_PAYLOAD_KEY)
    }
}
```

## Existing Implementations for Reference

- `InMemoryVectorStore` — `agentic-library/src/main/kotlin/vectorstore/InMemoryVectorStore.kt` (simplest, good starting point)
- `PineconeVectorStore` — `kagentic-vector-pinecone/src/main/kotlin/vectorstore/pinecone/PineconeVectorStore.kt`
- `QdrantVectorStore` — `kagentic-vector-qdrant/src/main/kotlin/vectorstore/qdrant/QdrantVectorStore.kt`

## Troubleshooting

**Search returns empty results**
- Verify the collection/index exists and has documents
- Check vector dimensions match the embedding model
- Log the raw response to inspect the payload structure

**Text is null in results**
- Ensure `TEXT_PAYLOAD_KEY` is stored on upsert
- Verify the key is being read from the correct payload field on parse

**Auth errors**
- Check the API key header name — it varies by provider (api-key, Authorization, x-api-key)

## Related Skills

- **add-llm-provider**: Add new LLM providers
- **add-custom-tool**: Create custom tools that use vector stores
- **understand-agent-flow**: Learn how RAG is wired into AgentFramework
