package org.qudisoft.constraint

import org.chocosolver.solver.Model
import org.chocosolver.solver.variables.IntVar
import org.qudisoft.model.PuzzleConstraint
import org.qudisoft.model.PuzzleAttributes


interface ConstraintHandler {
    fun canHandle(constraint: PuzzleConstraint): Boolean
    fun applyConstraint(
        constraint: PuzzleConstraint,
        model: Model,
        attributes: PuzzleAttributes,
        attributeVars: Map<String, Array<IntVar>>
    )
}