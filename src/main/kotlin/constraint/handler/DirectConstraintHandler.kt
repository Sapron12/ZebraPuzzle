package org.qudisoft.constraint.handler

import org.chocosolver.solver.Model
import org.chocosolver.solver.variables.IntVar
import org.qudisoft.model.PuzzleConstraint
import org.qudisoft.model.DirectConstraint
import org.qudisoft.model.PuzzleAttributes
import org.qudisoft.Constants
import org.qudisoft.constraint.BaseConstraintHandler
import org.slf4j.LoggerFactory

/**
 * Handles direct constraints where two attributes must be in the same house
 * If attribute1 has value1 in a house, then attribute2 must have value2 in the same house, and vice versa
 * Uses ifThen constraints to enforce this relationship across all houses
 */
class DirectConstraintHandler : BaseConstraintHandler() {
    
    private val logger = LoggerFactory.getLogger(DirectConstraintHandler::class.java)
    
    override fun canHandle(constraint: PuzzleConstraint): Boolean {
        return constraint is DirectConstraint
    }
    
    override fun applyConstraint(
        constraint: PuzzleConstraint,
        model: Model,
        attributes: PuzzleAttributes,
        attributeVars: Map<String, Array<IntVar>>
    ) {
        require(constraint is DirectConstraint) { "Expected DirectConstraint" }
        
        logger.debug("Applying direct constraint: ${constraint.attribute1}='${constraint.value1}' <-> ${constraint.attribute2}='${constraint.value2}'")

        val attr1 = getAttributeArray(constraint.attribute1, attributeVars)
        val attr2 = getAttributeArray(constraint.attribute2, attributeVars)
        val val1Index = getValueIndex(constraint.attribute1, constraint.value1, attributes)
        val val2Index = getValueIndex(constraint.attribute2, constraint.value2, attributes)
        
        logger.debug("Constraint indices: ${constraint.attribute1}[${val1Index}] <-> ${constraint.attribute2}[${val2Index}]")
        
        for (house in attr1.indices) {
            model.ifThen(
                model.arithm(attr1[house], Constants.Operators.EQUALS, val1Index),
                model.arithm(attr2[house], Constants.Operators.EQUALS, val2Index)
            )
            model.ifThen(
                model.arithm(attr2[house], Constants.Operators.EQUALS, val2Index),
                model.arithm(attr1[house], Constants.Operators.EQUALS, val1Index)
            )
        }
        
        logger.debug("Successfully applied direct constraint for ${attr1.size} houses")
    }
}