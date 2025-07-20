package org.qudisoft.constraint.handler

import org.chocosolver.solver.Model
import org.chocosolver.solver.variables.IntVar
import org.qudisoft.model.PuzzleConstraint
import org.qudisoft.model.PositionConstraint
import org.qudisoft.model.PuzzleAttributes
import org.qudisoft.Constants
import org.qudisoft.constraint.BaseConstraintHandler
import org.slf4j.LoggerFactory

/**
 * Handles position constraints where an attribute must be at a specific position
 * Directly assigns a specific value to a specific house position
 * Uses simple arithmetic constraint to fix the value
 */
class PositionConstraintHandler : BaseConstraintHandler() {
    
    private val logger = LoggerFactory.getLogger(PositionConstraintHandler::class.java)
    
    override fun canHandle(constraint: PuzzleConstraint): Boolean {
        return constraint is PositionConstraint
    }
    
    override fun applyConstraint(
        constraint: PuzzleConstraint,
        model: Model,
        attributes: PuzzleAttributes,
        attributeVars: Map<String, Array<IntVar>>
    ) {
        require(constraint is PositionConstraint) { "Expected PositionConstraint" }
        
        logger.debug("Applying position constraint: ${constraint.attribute}='${constraint.value}' at position ${constraint.position}")
        
        val attr = getAttributeArray(constraint.attribute, attributeVars)
        val valueIndex = getValueIndex(constraint.attribute, constraint.value, attributes)
        val position = constraint.position - 1 // Convert to 0-based index
        
        logger.debug("Constraint indices: ${constraint.attribute}[${valueIndex}] at position ${position} (0-based)")
        
        model.arithm(attr[position], Constants.Operators.EQUALS, valueIndex).post()
        
        logger.debug("Successfully applied position constraint")
    }
}