package org.qudisoft

import org.qudisoft.io.InputReader
import org.qudisoft.io.OutputWriter
import org.qudisoft.solver.PuzzleSolver
import org.slf4j.LoggerFactory

class ZebraPuzzleOrchestrator(
    private val inputReader: InputReader,
    private val puzzleSolver: PuzzleSolver,
    private val outputWriter: OutputWriter
) {
    
    private val logger = LoggerFactory.getLogger(ZebraPuzzleOrchestrator::class.java)

    fun solvePuzzle(inputSource: String, outputDestination: String) {
        logger.info("Starting puzzle solving process - Input: $inputSource, Output: $outputDestination")
        
        try {
            logger.debug("Reading puzzle input from: $inputSource")
            val puzzleInput = inputReader.readInput(inputSource)
            logger.info("Successfully read puzzle input - Houses: ${puzzleInput.puzzle.houses}, Constraints: ${puzzleInput.puzzle.constraints.size}")
            
            logger.debug("Starting puzzle solving process")
            val solution = puzzleSolver.solve(puzzleInput)
            logger.info("Puzzle solving completed - Solved: ${solution.solved}")
            
            logger.debug("Writing solution to: $outputDestination")
            outputWriter.writeOutput(solution, outputDestination)
            logger.info("Successfully wrote solution to output")
            
            logger.info("Puzzle solving process completed successfully")
            
        } catch (e: Exception) {
            logger.error("Error occurred during puzzle processing: ${e.message}", e)
        }
    }
}