package org.qudisoft.io.json

import org.qudisoft.io.InputReader
import org.qudisoft.model.*
import org.slf4j.LoggerFactory
import java.io.File

/**
 * TODO: FUTURE DEVELOPMENT - Attributes parser:
 *   Add attributes parser to collect them from constraints and not require them at the input
 */
class JsonInputReader : InputReader {
    
    private val logger = LoggerFactory.getLogger(JsonInputReader::class.java)
    private val jsonParser = JsonParser()
    
    override fun readInput(source: String): PuzzleInput {
        logger.debug("Reading puzzle input from file: $source")
        val jsonString = File(source).readText()
        logger.debug("Successfully read ${jsonString.length} characters from input file")
        
        return jsonParser.parseJsonString(jsonString)
    }
}