package org.qudisoft.io

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.qudisoft.io.json.JsonInputReader
import org.qudisoft.model.UnknownConstraint
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JsonInputReaderTest {
    
    @Test
    fun `should parse 5 house puzzle from input json`() {
        val inputReader = JsonInputReader()
        val inputFile = this::class.java.classLoader.getResource("input.json")?.file
            ?: throw IllegalStateException("input.json not found in test resources")
        
        val puzzleInput = assertDoesNotThrow {
            inputReader.readInput(inputFile)
        }
        
        // Verify basic puzzle structure
        assertEquals("Classic Zebra Puzzle (Einstein's Riddle)", puzzleInput.puzzle.description)
        assertEquals(5, puzzleInput.puzzle.houses)
        
        // Verify attributes
        val attributes = puzzleInput.puzzle.attributes.attributes
        assertEquals(5, attributes.size)
        assertTrue(attributes.containsKey("colors"))
        assertTrue(attributes.containsKey("nationalities"))
        assertTrue(attributes.containsKey("drinks"))
        assertTrue(attributes.containsKey("cigarettes"))
        assertTrue(attributes.containsKey("pets"))
        
        // Verify each attribute has 5 values
        attributes.values.forEach { values ->
            assertEquals(5, values.size)
        }
        
        // Verify specific attribute values
        assertEquals(listOf("Red", "Green", "White", "Yellow", "Blue"), attributes["colors"])
        assertEquals(listOf("British", "Swedish", "Danish", "Norwegian", "German"), attributes["nationalities"])
        
        // Verify constraints
        assertEquals(15, puzzleInput.puzzle.constraints.size)
        
        // Verify questions
        assertEquals(4, puzzleInput.puzzle.questions.size)
        val firstQuestion = puzzleInput.puzzle.questions[0]
        assertEquals("Who owned Zebra?", firstQuestion.description)
        assertEquals("nationalities", firstQuestion.targetAttribute)
        assertEquals("pets", firstQuestion.givenAttribute)
        assertEquals("Zebra", firstQuestion.givenValue)
    }
    
    @Test
    fun `should parse 10 house puzzle from input json`() {
        val inputReader = JsonInputReader()
        val inputFile = this::class.java.classLoader.getResource("input10.json")?.file
            ?: throw IllegalStateException("input10.json not found in test resources")
        
        val puzzleInput = assertDoesNotThrow {
            inputReader.readInput(inputFile)
        }
        
        // Verify basic puzzle structure
        assertTrue(puzzleInput.puzzle.description.contains("Extended Zebra Puzzle"))
        assertTrue(puzzleInput.puzzle.description.contains("10"))
        assertTrue(puzzleInput.puzzle.description.contains("House Edition"))
        assertEquals(10, puzzleInput.puzzle.houses)
        
        // Verify attributes
        val attributes = puzzleInput.puzzle.attributes.attributes
        assertEquals(6, attributes.size) // 5 original + cryptos
        assertTrue(attributes.containsKey("colors"))
        assertTrue(attributes.containsKey("nationalities"))
        assertTrue(attributes.containsKey("drinks"))
        assertTrue(attributes.containsKey("cigarettes"))
        assertTrue(attributes.containsKey("pets"))
        assertTrue(attributes.containsKey("cryptos"))
        
        // Verify each attribute has 10 values
        attributes.values.forEach { values ->
            assertEquals(10, values.size)
        }
        
        // Verify specific attribute values for extended puzzle
        assertEquals(listOf("Red", "Green", "White", "Yellow", "Blue", "Orange", "Purple", "Black", "Brown", "Pink"), attributes["colors"])
        assertEquals(listOf("British", "Swedish", "Danish", "Norwegian", "German", "French", "Italian", "Spanish", "Dutch", "Canadian"), attributes["nationalities"])
        assertEquals(listOf("Bitcoin", "Ethereum", "Litecoin", "Ripple", "Dogecoin", "Cardano", "Polkadot", "Solana", "Chainlink", "Stellar"), attributes["cryptos"])
        
        // Verify constraints
        assertEquals(29, puzzleInput.puzzle.constraints.size)
        
        // Verify questions
        assertEquals(4, puzzleInput.puzzle.questions.size)
        val firstQuestion = puzzleInput.puzzle.questions[0]
        assertEquals("Who owned Zebra?", firstQuestion.description)
        assertEquals("nationalities", firstQuestion.targetAttribute)
        assertEquals("pets", firstQuestion.givenAttribute)
        assertEquals("Zebra", firstQuestion.givenValue)
    }
}