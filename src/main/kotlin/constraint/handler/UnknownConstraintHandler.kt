package org.qudisoft.constraint.handler

import org.chocosolver.solver.Model
import org.chocosolver.solver.variables.IntVar
import org.qudisoft.constraint.BaseConstraintHandler
import org.qudisoft.model.PuzzleConstraint
import org.qudisoft.model.PuzzleAttributes
import org.qudisoft.model.UnknownConstraint
import org.slf4j.LoggerFactory

/**
 * Acts as a fallback handler for unrecognized constraint types
 * Logs a warning message with constraint details
 * Does not apply any actual constraints to the model
 */
class UnknownConstraintHandler : BaseConstraintHandler() {

    private val logger = LoggerFactory.getLogger(UnknownConstraintHandler::class.java)

    override fun canHandle(constraint: PuzzleConstraint): Boolean {
        return constraint is UnknownConstraint
    }

    override fun applyConstraint(
        constraint: PuzzleConstraint,
        model: Model,
        attributes: PuzzleAttributes,
        attributeVars: Map<String, Array<IntVar>>
    ) {
        if (constraint is UnknownConstraint) {
            logger.warn("Skipping unknown constraint type '${constraint.type}': ${constraint.description}")
            if (constraint.properties.isNotEmpty()) {
                logger.debug("Unknown constraint properties: ${constraint.properties}")
            }
            logger.debug("Successfully handled unknown constraint (no model constraints applied)")
        }
    }
}