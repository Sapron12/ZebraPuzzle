package org.qudisoft.constraint

import org.qudisoft.constraint.handler.DirectConstraintHandler
import org.qudisoft.constraint.handler.LeftOfConstraintHandler
import org.qudisoft.constraint.handler.NeighborConstraintHandler
import org.qudisoft.constraint.handler.PositionConstraintHandler
import org.qudisoft.constraint.handler.RightOfConstraintHandler
import org.qudisoft.constraint.handler.UnknownConstraintHandler
import org.qudisoft.model.PuzzleConstraint

class ConstraintHandlerFactory {

    // TODO: FUTURE DEVELOPMENT - can be improved with map-based lookup
    private val handlers = listOf(
        DirectConstraintHandler(),
        PositionConstraintHandler(),
        LeftOfConstraintHandler(),
        RightOfConstraintHandler(),
        NeighborConstraintHandler(),
        UnknownConstraintHandler()
    )

    fun getHandler(constraint: PuzzleConstraint): ConstraintHandler {
        return handlers.find { it.canHandle(constraint) }
            ?: throw IllegalArgumentException("No handler found for constraint type: ${constraint::class.simpleName}")
    }
}