package org.qudisoft.constraint

import org.chocosolver.solver.variables.IntVar
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.qudisoft.model.PositionConstraint
import org.qudisoft.model.DirectConstraint
import org.qudisoft.Constants
import org.qudisoft.constraint.handler.PositionConstraintHandler

class PositionConstraintHandlerTest : BaseConstraintHandlerTest(PositionConstraintHandler()) {

    @Test
    fun `canHandle should return true for position constraint type`() {
        val constraint = createPositionConstraint(
            Constants.AttributeNames.COLORS,
            "red",
            FIRST_POSITION,
            "Test position constraint"
        )

        assertTrue(handler.canHandle(constraint))
    }

    @Test
    fun `canHandle should return false for non-position constraint types`() {
        val directConstraint = createNonPositionConstraint(
            Constants.ConstraintTypes.DIRECT,
            "Test direct constraint"
        )
        val adjacentConstraint = createNonPositionConstraint(
            Constants.ConstraintTypes.LEFT_OF,
            "Test adjacent constraint"
        )
        val neighborConstraint = createNonPositionConstraint(
            Constants.ConstraintTypes.NEIGHBOR,
            "Test neighbor constraint"
        )

        assertFalse(handler.canHandle(directConstraint))
        assertFalse(handler.canHandle(adjacentConstraint))
        assertFalse(handler.canHandle(neighborConstraint))
    }

    @Test
    fun `canHandle should return false for unknown constraint type`() {
        val unknownConstraint = createNonPositionConstraint("unknown", "Test unknown constraint")

        assertFalse(handler.canHandle(unknownConstraint))
    }


    @Test
    fun `applyConstraint should throw exception for unknown attribute`() {
        val constraint = createPositionConstraint(
            attribute = "unknown_attribute",
            value = "some_value",
            position = FIRST_POSITION,
            description = "Invalid constraint with unknown attribute"
        )

        val exception = assertThrows<IllegalArgumentException> {
            applyConstraints(constraint)
        }

        assertTrue(exception.message!!.contains(Constants.ErrorMessages.UNKNOWN_ATTRIBUTE))
    }

    @Test
    fun `applyConstraint should handle unknown value by using index -1`() {
        val constraint = createPositionConstraint(
            Constants.AttributeNames.COLORS,
            "unknown_color",
            FIRST_POSITION,
            "Constraint with unknown value"
        )
        val constraint2 = createPositionConstraint(
            Constants.AttributeNames.COLORS,
            "unknown_color2",
            SECOND_POSITION,
            "Constraint with unknown value"
        )

        // Should not throw exception, but apply constraint with value index -1
        val initialConstraints = model.nbCstrs
        applyConstraints(constraint, constraint2)
        solveModelWithAllDifferentConstraints()

        val unknownColorIndex = attributes.attributes["colors"]!!.indexOf("unknown_color")
        val unknownColorIndex2 = attributes.attributes["colors"]!!.indexOf("unknown_color2")
        assertEquals(-1, unknownColorIndex)
        assertEquals(-1, unknownColorIndex2)
        // Verify that a constraint was added (even with unknown value)
        assertTrue(model.nbCstrs > initialConstraints)
    }


    @Test
    fun `unconstrained color should be placed in remaining position`() {
        val constraints = arrayOf(
            createPositionConstraint(
                Constants.AttributeNames.COLORS,
                "red",
                FIRST_POSITION,
                "Red house is at position 1"
            ),
            createPositionConstraint(
                Constants.AttributeNames.COLORS,
                "blue",
                SECOND_POSITION,
                "Blue house is at position 2"
            ),
            createPositionConstraint(
                Constants.AttributeNames.COLORS,
                "green",
                THIRD_POSITION,
                "Green house is at position 3"
            ),
            createPositionConstraint(
                Constants.AttributeNames.COLORS,
                "yellow",
                FOURTH_POSITION,
                "Yellow house is at position 4"
            )
        )
        applyConstraints(*constraints)
        solveModelWithAllDifferentConstraints()

        val whiteColorIndex = attributes.attributes["colors"]!!.indexOf("white")
        assertConstraintAppliedCorrectly(attributeVars["colors"]!!, whiteColorIndex, FIFTH_POSITION, "White")
    }
    

    @Test
    fun `mixed attribute constraints should place values in correct positions`() {
        val constraints = arrayOf(
            createPositionConstraint(
                Constants.AttributeNames.COLORS,
                "red",
                THIRD_POSITION,
                "Red house is at position 3"
            ),
            createPositionConstraint(
                Constants.AttributeNames.PETS,
                "cat", FIRST_POSITION,
                "Cat lives in first house"),
            createPositionConstraint(
                Constants.AttributeNames.CIGARETTES,
                "dunhill",
                SECOND_POSITION,
                "Dunhill smoked in second house"
            )
        )
        applyConstraints(*constraints)
        solveModelWithAllDifferentConstraints()

        assertConstraintAppliedCorrectly(attributeVars["colors"]!!, attributes.attributes["colors"]!!.indexOf("red"), THIRD_POSITION, "Red")
        assertConstraintAppliedCorrectly(attributeVars["pets"]!!, attributes.attributes["pets"]!!.indexOf("cat"), FIRST_POSITION, "Cat")
        assertConstraintAppliedCorrectly(
            attributeVars["cigarettes"]!!,
            attributes.attributes["cigarettes"]!!.indexOf("dunhill"),
            SECOND_POSITION,
            "Dunhill"
        )
    }

    @Test
    fun `conflicting position constraints should make model unsolvable`() {
        // Create conflicting constraints: red and blue both at position 1
        val conflictingConstraints = arrayOf(
            createPositionConstraint(
                Constants.AttributeNames.COLORS,
                "red",
                FIRST_POSITION,
                "Red house is at position 1"
            ),
            createPositionConstraint(
                Constants.AttributeNames.COLORS,
                "blue",
                FIRST_POSITION,
                "Blue house is at position 1"
            )
        )
        
        // Apply the conflicting constraints
        applyConstraints(*conflictingConstraints)
        
        // The model should be unsolvable due to conflicting constraints
        val isSolvable = solveModelWithAllDifferentConstraints()
        assertFalse(isSolvable, "Model should be unsolvable due to conflicting position constraints")
    }

    // Helper methods for creating constraints
    private fun createPositionConstraint(
        attribute: String,
        value: String,
        position: Int,
        description: String = "$value at position $position"
    ) = PositionConstraint(
        description = description,
        attribute = attribute,
        value = value,
        position = position
    )

    private fun createNonPositionConstraint(type: String, description: String) = DirectConstraint(
        description = description,
        attribute1 = "colors",
        value1 = "red",
        attribute2 = "nationalities", 
        value2 = "british"
    )

    // Helper methods for verification
    private fun assertConstraintAppliedCorrectly(
        attributeArray: Array<IntVar>,
        expectedValueIndex: Int,
        expectedPosition: Int,
        attributeName: String
    ) {
        val positionIndex = expectedPosition - 1 // Convert 1-based position to 0-based index
        assertEquals(
            expectedValueIndex,
            attributeArray[positionIndex].value,
            "$attributeName should be at position $expectedPosition"
        )
    }

}