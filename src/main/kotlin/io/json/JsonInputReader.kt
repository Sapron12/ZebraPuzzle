package org.qudisoft.io.json

import org.qudisoft.io.InputReader
import org.qudisoft.model.*
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream

/**
 * TODO: FUTURE DEVELOPMENT - Attributes parser:
 *   Add attributes parser to collect them from constraints and not require them at the input
 */
class JsonInputReader : InputReader {
    
    private val logger = LoggerFactory.getLogger(JsonInputReader::class.java)
    private val jsonParser = JsonParser()
    
    override fun readInput(source: String): PuzzleInput {
        logger.debug("Reading puzzle input from file: $source")
        
        val jsonString = readFromFileSystem(source)
            ?: readFromResources(source)
            ?: throw IllegalArgumentException("File not found: $source (neither in file system nor in resources)")
        
        return jsonString
            .also { logger.debug("Successfully read ${it.length} characters from input file") }
            .let(jsonParser::parseJsonString)
    }
    
    private fun readFromFileSystem(source: String): String? = 
        File(source)
            .takeIf { it.exists() }
            ?.also { logger.debug("Reading from file system: ${it.absolutePath}") }
            ?.readText()
    
    private fun readFromResources(source: String): String? = 
        this::class.java.classLoader.getResourceAsStream(source)
            ?.also { logger.debug("Reading from resources: $source") }
            ?.use { inputStream ->
                inputStream.bufferedReader().readText()
            }
            .also { if (it == null) logger.debug("File not found in resources") }
}