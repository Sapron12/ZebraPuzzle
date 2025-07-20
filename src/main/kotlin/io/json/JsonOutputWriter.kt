package org.qudisoft.io.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.qudisoft.io.OutputWriter
import org.qudisoft.model.PuzzleSolution
import java.io.File
import org.slf4j.LoggerFactory

class JsonOutputWriter : OutputWriter {
    
    private val logger = LoggerFactory.getLogger(JsonOutputWriter::class.java)
    
    override fun writeOutput(solution: PuzzleSolution, destination: String) {
        logger.debug("Writing puzzle solution to: $destination")
        writeToJsonFile(solution, destination)
        logger.info("Successfully wrote solution to file: $destination")
    }
    
    private fun writeToJsonFile(solution: PuzzleSolution, filename: String) {
        logger.debug("Serializing solution to JSON - Solved: ${solution.solved}, Houses: ${solution.houses.size}")
        val jsonString = Json { prettyPrint = true }.encodeToString(solution)
        logger.debug("Generated JSON string with ${jsonString.length} characters")
        
        File(filename).writeText(jsonString)
        logger.debug("Successfully wrote JSON content to file: $filename")
    }
}