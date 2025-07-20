package org.qudisoft.constraint.handler

import org.chocosolver.solver.Model
import org.chocosolver.solver.constraints.Constraint
import org.chocosolver.solver.variables.IntVar
import org.qudisoft.model.PuzzleConstraint
import org.qudisoft.model.NeighborConstraint
import org.qudisoft.model.PuzzleAttributes
import org.qudisoft.Constants
import org.qudisoft.constraint.BaseConstraintHandler
import org.slf4j.LoggerFactory

/**
 * For each house, checks both left and right neighbors
 * Creates OR constraint across all valid neighboring positions
 *
 * TODO: FUTURE DEVELOPMENT - Combine to SpatialConstraintHandler or AdjacencyConstraintHandler
 */
class NeighborConstraintHandler : BaseConstraintHandler() {
    
    private val logger = LoggerFactory.getLogger(NeighborConstraintHandler::class.java)
    
    override fun canHandle(constraint: PuzzleConstraint): Boolean {
        return constraint is NeighborConstraint
    }
    
    override fun applyConstraint(
        constraint: PuzzleConstraint,
        model: Model,
        attributes: PuzzleAttributes,
        attributeVars: Map<String, Array<IntVar>>
    ) {
        require(constraint is NeighborConstraint) { "Expected NeighborConstraint" }
        
        logger.debug("Applying neighbor constraint: ${constraint.attribute1}='${constraint.value1}' neighbor of ${constraint.attribute2}='${constraint.value2}'")
        
        val attr1 = getAttributeArray(constraint.attribute1, attributeVars)
        val attr2 = getAttributeArray(constraint.attribute2, attributeVars)
        val val1Index = getValueIndex(constraint.attribute1, constraint.value1, attributes)
        val val2Index = getValueIndex(constraint.attribute2, constraint.value2, attributes)
        
        logger.debug("Constraint indices: ${constraint.attribute1}[${val1Index}] neighbor of ${constraint.attribute2}[${val2Index}]")
        
        val neighborConstraints = mutableListOf<Constraint>()
        for (house in attr1.indices) {
            for (neighbor in listOf(house - 1, house + 1)) {
                if (neighbor in attr1.indices) {
                    val bothConditions = model.and(
                        model.arithm(attr1[house], Constants.Operators.EQUALS, val1Index),
                        model.arithm(attr2[neighbor], Constants.Operators.EQUALS, val2Index)
                    )
                    neighborConstraints.add(bothConditions)
                }
            }
        }
        model.or(*neighborConstraints.toTypedArray()).post()
        
        logger.debug("Successfully applied neighbor constraint for ${neighborConstraints.size} neighbor pairs")
    }
}