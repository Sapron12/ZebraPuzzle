package org.qudisoft.constraint.handler

import org.chocosolver.solver.Model
import org.chocosolver.solver.constraints.Constraint
import org.chocosolver.solver.variables.IntVar
import org.qudisoft.model.PuzzleConstraint
import org.qudisoft.model.RightOfConstraint
import org.qudisoft.model.PuzzleAttributes
import org.qudisoft.Constants
import org.qudisoft.constraint.BaseConstraintHandler
import org.slf4j.LoggerFactory

/**
 * Checks all adjacent house pairs where the first house is immediately to the right of the second
 * Creates OR constraint across all valid adjacent positions
 * For each valid pair, ensures both conditions are met simultaneously
 * 
 * TODO: FUTURE DEVELOPMENT - Combine to SpatialConstraintHandler or AdjacencyConstraintHandler
 */
class RightOfConstraintHandler : BaseConstraintHandler() {
    
    private val logger = LoggerFactory.getLogger(RightOfConstraintHandler::class.java)
    
    override fun canHandle(constraint: PuzzleConstraint): Boolean {
        return constraint is RightOfConstraint
    }
    
    override fun applyConstraint(
        constraint: PuzzleConstraint,
        model: Model,
        attributes: PuzzleAttributes,
        attributeVars: Map<String, Array<IntVar>>
    ) {
        require(constraint is RightOfConstraint) { "Expected RightOfConstraint" }
        
        logger.debug("Applying right-of constraint: ${constraint.attribute1}='${constraint.value1}' right of ${constraint.attribute2}='${constraint.value2}'")
        
        val attr1 = getAttributeArray(constraint.attribute1, attributeVars)
        val attr2 = getAttributeArray(constraint.attribute2, attributeVars)
        val val1Index = getValueIndex(constraint.attribute1, constraint.value1, attributes)
        val val2Index = getValueIndex(constraint.attribute2, constraint.value2, attributes)

        logger.debug("Constraint indices: ${constraint.attribute1}[${val1Index}] right of ${constraint.attribute2}[${val2Index}]")

        val adjacentConstraints = mutableListOf<Constraint>()
        for (house in 0 until attr1.size - 1) {
            val bothConditions = model.and(
                model.arithm(attr1[house + 1], Constants.Operators.EQUALS, val1Index),
                model.arithm(attr2[house], Constants.Operators.EQUALS, val2Index)
            )
            adjacentConstraints.add(bothConditions)
        }
        model.or(*adjacentConstraints.toTypedArray()).post()
        
        logger.debug("Successfully applied right-of constraint for ${adjacentConstraints.size} adjacent house pairs")
    }
}