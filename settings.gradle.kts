pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

rootProject.name = "kagentic"
include(":agentic-library")
include(":kagentic-embeddings")
include(":kagentic-vector-pinecone")
include(":kagentic-vector-qdrant")