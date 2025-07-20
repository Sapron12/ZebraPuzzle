package org.qudisoft.constraint.handler

import org.chocosolver.solver.Model
import org.chocosolver.solver.constraints.Constraint
import org.chocosolver.solver.variables.IntVar
import org.qudisoft.model.PuzzleConstraint
import org.qudisoft.model.LeftOfConstraint
import org.qudisoft.model.PuzzleAttributes
import org.qudisoft.Constants
import org.qudisoft.constraint.BaseConstraintHandler
import org.slf4j.LoggerFactory

/**
 * Checks all adjacent house pairs where the first house is immediately to the left of the second
 * Creates OR constraint across all valid adjacent positions
 * 
 * TODO: FUTURE DEVELOPMENT - Combine to SpatialConstraintHandler or AdjacencyConstraintHandler
 */
class LeftOfConstraintHandler : BaseConstraintHandler() {
    
    private val logger = LoggerFactory.getLogger(LeftOfConstraintHandler::class.java)
    
    override fun canHandle(constraint: PuzzleConstraint): Boolean {
        return constraint is LeftOfConstraint
    }
    
    override fun applyConstraint(
        constraint: PuzzleConstraint,
        model: Model,
        attributes: PuzzleAttributes,
        attributeVars: Map<String, Array<IntVar>>
    ) {
        require(constraint is LeftOfConstraint) { "Expected LeftOfConstraint" }
        
        logger.debug("Applying left-of constraint: ${constraint.attribute1}='${constraint.value1}' left of ${constraint.attribute2}='${constraint.value2}'")
        
        val attr1 = getAttributeArray(constraint.attribute1, attributeVars)
        val attr2 = getAttributeArray(constraint.attribute2, attributeVars)
        val val1Index = getValueIndex(constraint.attribute1, constraint.value1, attributes)
        val val2Index = getValueIndex(constraint.attribute2, constraint.value2, attributes)
        
        logger.debug("Constraint indices: ${constraint.attribute1}[${val1Index}] left of ${constraint.attribute2}[${val2Index}]")
        
        val adjacentConstraints = mutableListOf<Constraint>()
        for (house in 0 until attr1.size - 1) {
            val bothConditions = model.and(
                model.arithm(attr1[house], Constants.Operators.EQUALS, val1Index),
                model.arithm(attr2[house + 1], Constants.Operators.EQUALS, val2Index)
            )
            adjacentConstraints.add(bothConditions)
        }
        model.or(*adjacentConstraints.toTypedArray()).post()
        
        logger.debug("Successfully applied left-of constraint for ${adjacentConstraints.size} adjacent house pairs")
    }
}