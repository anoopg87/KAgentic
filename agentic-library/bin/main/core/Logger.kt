package core
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.io.File

interface Logger {
    suspend fun log(message: String)
}

class ConsoleLogger : Logger {
    override suspend fun log(message: String) {
        println("[Agentic] $message")
    }
}

class FileLogger(private val file: File) : Logger {
    override suspend fun log(message: String) {
        file.appendText("[Agentic] $message\n")
    }
}


class RemoteLogger(private val endpoint: String) : Logger {
    private val client = HttpClient()
    override suspend fun log(message: String) {
        // Fire-and-forget, non-blocking
        client.post(endpoint) {
            contentType(ContentType.Application.Json)
            setBody("{\"log\": \"[Agentic] $message\"}")
        }
    }
}
