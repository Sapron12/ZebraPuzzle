package org.qudisoft.io

import org.qudisoft.model.PuzzleSolution

/**
 * Interface for writing puzzle solutions to different outputs
 */
interface OutputWriter {
    fun writeOutput(solution: PuzzleSolution, destination: String)
}