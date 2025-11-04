package tools

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class FileReaderToolTest {

    @Test
    fun testCanHandleFileRead() {
        val tool = FileReaderTool()
        assertTrue(tool.canHandle("read file test.txt"))
        assertTrue(tool.canHandle("example.txt"))
        assertTrue(tool.canHandle("document.md"))
        assertTrue(tool.canHandle("data.csv"))
    }

    @Test
    fun testCannotHandleNonFileQuery() {
        val tool = FileReaderTool()
        assertFalse(tool.canHandle("search something"))
        assertFalse(tool.canHandle("2+2"))
    }

    @Test
    fun testScoreFileRead() {
        val tool = FileReaderTool()
        assertTrue(tool.score("read file test.txt") == 10)
        assertTrue(tool.score("example.txt") == 8)
        assertTrue(tool.score("hello") == 1)
    }

    @Test
    fun testHandleExistingFile(@TempDir tempDir: File) {
        val tool = FileReaderTool()
        val testFile = File(tempDir, "test.txt")
        testFile.writeText("Hello, World!")

        val result = runBlocking { tool.handle(testFile.absolutePath) }
        assertTrue(result == "Hello, World!")
    }

    @Test
    fun testHandleNonExistentFile() {
        val tool = FileReaderTool()
        val result = runBlocking { tool.handle("/nonexistent/file.txt") }
        assertTrue(result.contains("File not found"))
    }
}
