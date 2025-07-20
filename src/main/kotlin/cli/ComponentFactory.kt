package org.qudisoft.cli

import org.qudisoft.io.InputReader
import org.qudisoft.io.console.ConsoleOutputWriter
import org.qudisoft.io.json.JsonInputReader
import org.qudisoft.io.json.JsonOutputWriter
import org.qudisoft.io.OutputWriter
import org.qudisoft.solver.ChocoSolverStrategy
import org.qudisoft.solver.PuzzleSolver
import org.slf4j.LoggerFactory

/**
 * Component factory for creating application components
 */
class ComponentFactory {

    private val logger = LoggerFactory.getLogger(ComponentFactory::class.java)

    fun createInputReader(inputFormat: String): InputReader {
        return when (inputFormat) {
            "json" -> JsonInputReader()
            else -> {
                logger.debug("Unknown input format: $inputFormat. Using default json format.")
                JsonInputReader()
            }
        }
    }
    
    fun createSolver(strategy: String): PuzzleSolver {
        return when (strategy.lowercase()) {
            "choco" -> ChocoSolverStrategy()
            "bruteforce" -> {
                throw IllegalArgumentException("BruteForceSolverStrategy not yet implemented")
            }
            else -> {
                logger.debug("Unknown strategy: $strategy. Using default Choco solver.")
                ChocoSolverStrategy()
            }
        }
    }
    
    fun createOutputWriter(format: String): OutputWriter {
        return when (format.lowercase()) {
            "console" -> ConsoleOutputWriter()
            "json" -> JsonOutputWriter()
            else -> {
                logger.debug("Unknown output format: $format. Using json output.")
                JsonOutputWriter()
            }
        }
    }
}