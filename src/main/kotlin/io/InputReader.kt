package org.qudisoft.io

import org.qudisoft.model.PuzzleInput

/**
 * Interface for reading puzzle input from different sources
 */
interface InputReader {
    fun readInput(source: String): PuzzleInput
}