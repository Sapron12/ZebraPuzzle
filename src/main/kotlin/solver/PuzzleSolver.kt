package org.qudisoft.solver

import org.qudisoft.model.PuzzleInput
import org.qudisoft.model.PuzzleSolution

/**
 * Strategy interface for different puzzle solving algorithms
 */
interface PuzzleSolver {
    fun solve(puzzleInput: PuzzleInput): PuzzleSolution
}