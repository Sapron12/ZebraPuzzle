package org.qudisoft.cli

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArgumentParserTest {
    
    private val parser = ArgumentParser()
    
    @Test
    fun `should parse empty arguments and return default config`() {
        val config = parser.parse(emptyArray())
        
        assertEquals("input.json", config.inputFile)
        assertEquals("json", config.inputFormat)
        assertEquals("output.json", config.outputFile)
        assertEquals("zebra-puzzle.log", config.logFile)
        assertEquals("choco", config.strategy)
        assertEquals("console", config.outputFormat)
        assertTrue(config.clearLog)
    }
    
    @Test
    fun `should parse input file argument`() {
        val config = parser.parse(arrayOf("-i", "test.json"))
        assertEquals("test.json", config.inputFile)
        
        val configLong = parser.parse(arrayOf("--input", "test2.json"))
        assertEquals("test2.json", configLong.inputFile)
    }
    
    @Test
    fun `should parse input format argument`() {
        val config = parser.parse(arrayOf("-if", "xml"))
        assertEquals("xml", config.inputFormat)
        
        val configLong = parser.parse(arrayOf("--input-format", "yaml"))
        assertEquals("yaml", configLong.inputFormat)
    }
    
    @Test
    fun `should parse output format argument`() {
        val config = parser.parse(arrayOf("-of", "json"))
        assertEquals("json", config.outputFormat)
        
        val configLong = parser.parse(arrayOf("--output-format", "xml"))
        assertEquals("xml", configLong.outputFormat)
    }
    
    @Test
    fun `should parse no-clear-log flag`() {
        val config = parser.parse(arrayOf("--no-clear-log"))
        assertFalse(config.clearLog)
    }
    
    @Test
    fun `should parse multiple arguments`() {
        val config = parser.parse(arrayOf(
            "-i", "input.json",
            "-o", "output.json",
            "-s", "choco",
            "--no-clear-log",
            "-of", "json"
        ))
        
        assertEquals("input.json", config.inputFile)
        assertEquals("output.json", config.outputFile)
        assertEquals("choco", config.strategy)
        assertEquals("json", config.outputFormat)
        assertFalse(config.clearLog)
    }
    
    @Test
    fun `should throw CliException for help argument`() {
        val exception = assertThrows<CliException> {
            parser.parse(arrayOf("-h"))
        }
        assertEquals("Help requested", exception.message)
        assertTrue(exception.showHelp)
        
        val exceptionLong = assertThrows<CliException> {
            parser.parse(arrayOf("--help"))
        }
        assertEquals("Help requested", exceptionLong.message)
        assertTrue(exceptionLong.showHelp)
    }
}