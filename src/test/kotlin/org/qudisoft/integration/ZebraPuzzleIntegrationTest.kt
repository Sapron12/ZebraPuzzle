package org.qudisoft.integration

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.io.TempDir
import org.qudisoft.ZebraPuzzleOrchestrator
import org.qudisoft.cli.ComponentFactory
import java.io.File
import java.nio.file.Path
import kotlin.test.assertTrue

/**
 * Integration tests for the ZebraPuzzle application using different input files
 * to ensure the complete application flow works correctly.
 * TODO: FEATURE DEVELOPMENT - add new tests with expected solution and reorganize
 */
class ZebraPuzzleIntegrationTest {
    
    private val componentFactory = ComponentFactory()
    
    @Test
    fun `should solve classic 5-house zebra puzzle from input json`(@TempDir tempDir: Path) {
        // Arrange
        val inputFile = this::class.java.classLoader.getResource("input.json")?.file
            ?: throw IllegalStateException("input.json not found in test resources")
        val outputFile = tempDir.resolve("output_classic.json").toString()
        
        val inputReader = componentFactory.createInputReader("json")
        val puzzleSolver = componentFactory.createSolver("choco")
        val outputWriter = componentFactory.createOutputWriter("json")
        val orchestrator = ZebraPuzzleOrchestrator(inputReader, puzzleSolver, outputWriter)
        
        assertDoesNotThrow {
            orchestrator.solvePuzzle(inputFile, outputFile)
        }
        
        // Verify output file was created
        val outputFileObj = File(outputFile)
        assertTrue(outputFileObj.exists(), "Output file should be created")
        assertTrue(outputFileObj.length() > 0, "Output file should not be empty")
        
        // Verify output contains expected structure (basic validation)
        val outputContent = outputFileObj.readText()
        assertTrue(outputContent.contains("\"solved\""), "Output should contain solved status")
        assertTrue(outputContent.contains("\"houses\""), "Output should contain houses information")
    }
    
    @Test
    fun `should handle 3-house puzzle with unknown constraint from input3 json`(@TempDir tempDir: Path) {
        val inputFile = this::class.java.classLoader.getResource("input3.json")?.file
            ?: throw IllegalStateException("input3.json not found in test resources")
        val outputFile = tempDir.resolve("output_3house.json").toString()
        
        val inputReader = componentFactory.createInputReader("json")
        val puzzleSolver = componentFactory.createSolver("choco")
        val outputWriter = componentFactory.createOutputWriter("json")
        val orchestrator = ZebraPuzzleOrchestrator(inputReader, puzzleSolver, outputWriter)
        
        assertDoesNotThrow {
            orchestrator.solvePuzzle(inputFile, outputFile)
        }
        
        val outputFileObj = File(outputFile)
        assertTrue(outputFileObj.exists(), "Output file should be created")
        assertTrue(outputFileObj.length() > 0, "Output file should not be empty")
        
        // Verify output contains expected structure
        val outputContent = outputFileObj.readText()
        assertTrue(outputContent.contains("\"solved\""), "Output should contain solved status")
        assertTrue(outputContent.contains("\"houses\""), "Output should contain houses information")
    }
    
    @Test
    fun `should solve extended 10-house zebra puzzle from input10 json`(@TempDir tempDir: Path) {
        val inputFile = this::class.java.classLoader.getResource("input10.json")?.file
            ?: throw IllegalStateException("input10.json not found in test resources")
        val outputFile = tempDir.resolve("output_10house.json").toString()
        
        val inputReader = componentFactory.createInputReader("json")
        val puzzleSolver = componentFactory.createSolver("choco")
        val outputWriter = componentFactory.createOutputWriter("json")
        val orchestrator = ZebraPuzzleOrchestrator(inputReader, puzzleSolver, outputWriter)
        
        assertDoesNotThrow {
            orchestrator.solvePuzzle(inputFile, outputFile)
        }
        
        // Verify output file was created
        val outputFileObj = File(outputFile)
        assertTrue(outputFileObj.exists(), "Output file should be created")
        assertTrue(outputFileObj.length() > 0, "Output file should not be empty")
        
        // Verify output contains expected structure
        val outputContent = outputFileObj.readText()
        assertTrue(outputContent.contains("\"solved\""), "Output should contain solved status")
        assertTrue(outputContent.contains("\"houses\""), "Output should contain houses information")
        
        // Count the number of house objects in the houses array
        val houseCount = outputContent.split("\"house\":").size - 1
        assertTrue(houseCount == 10, "Output should contain 10 house objects, but found $houseCount")
    }
}