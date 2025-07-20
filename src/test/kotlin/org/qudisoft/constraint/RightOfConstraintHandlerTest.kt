package org.qudisoft.constraint

import org.chocosolver.solver.variables.IntVar
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.qudisoft.model.PositionConstraint
import org.qudisoft.model.RightOfConstraint
import org.qudisoft.Constants
import org.qudisoft.constraint.handler.RightOfConstraintHandler

class RightOfConstraintHandlerTest : BaseConstraintHandlerTest(RightOfConstraintHandler()) {

    @Test
    fun `canHandle should return true for rightOf constraint type`() {
        val constraint = createRightOfConstraint(
            Constants.AttributeNames.COLORS,
            "red",
            Constants.AttributeNames.NATIONALITIES,
            "brit",
            "Test rightOf constraint"
        )

        assertTrue(handler.canHandle(constraint))
    }

    @Test
    fun `canHandle should return false for non-rightOf constraint types`() {
        val positionConstraint = createNonRightOfConstraint(
            Constants.ConstraintTypes.POSITION,
            "Test position constraint"
        )
        val directConstraint = createNonRightOfConstraint(
            Constants.ConstraintTypes.DIRECT,
            "Test direct constraint"
        )
        val neighborConstraint = createNonRightOfConstraint(
            Constants.ConstraintTypes.NEIGHBOR,
            "Test neighbor constraint"
        )

        assertFalse(handler.canHandle(positionConstraint))
        assertFalse(handler.canHandle(directConstraint))
        assertFalse(handler.canHandle(neighborConstraint))
    }

    @Test
    fun `canHandle should return false for unknown constraint type`() {
        val unknownConstraint = createNonRightOfConstraint("unknown", "Test unknown constraint")

        assertFalse(handler.canHandle(unknownConstraint))
    }

    @Test
    fun `applyConstraint should throw exception for unknown attribute`() {
        val constraint = createRightOfConstraint(
            attribute1 = "unknown_attribute",
            value1 = "some_value",
            attribute2 = Constants.AttributeNames.NATIONALITIES,
            value2 = "brit",
            description = "Invalid constraint with unknown attribute1"
        )

        val exception = assertThrows<IllegalArgumentException> {
            applyConstraints(constraint)
        }

        assertTrue(exception.message!!.contains(Constants.ErrorMessages.UNKNOWN_ATTRIBUTE))
    }

    @Test
    fun `applyConstraint should handle unknown value by using index -1`() {
        val constraint = createRightOfConstraint(
            Constants.AttributeNames.COLORS,
            "unknown_color",
            Constants.AttributeNames.NATIONALITIES,
            "brit",
            "Constraint with unknown value1"
        )

        // Should not throw exception, but apply constraint with value index -1
        val initialConstraints = model.nbCstrs
        applyConstraints(constraint)
        solveModelWithAllDifferentConstraints()

        val unknownColorIndex = attributes.attributes["colors"]!!.indexOf("unknown_color")
        assertEquals(-1, unknownColorIndex)
        // Verify that constraints were added (even with unknown value)
        assertTrue(model.nbCstrs > initialConstraints)
    }

    @Test
    fun `multiple rightOf constraints should be satisfied simultaneously`() {
        val constraints = arrayOf(
            createRightOfConstraint(
                Constants.AttributeNames.COLORS,
                "red",
                Constants.AttributeNames.NATIONALITIES,
                "brit",
                "Red house is right of British house"
            ),
            createRightOfConstraint(
                Constants.AttributeNames.DRINKS,
                "tea",
                Constants.AttributeNames.PETS,
                "dog",
                "Tea drinker is right of dog owner"
            )
        )
        
        applyConstraints(*constraints)
        val isSolvable = solveModelWithAllDifferentConstraints()
        assertTrue(isSolvable, "Model should be solvable with multiple rightOf constraints")

        // Verify first constraint: red right of brit
        val redColorIndex = attributes.attributes["colors"]!!.indexOf("red")
        val britNationalityIndex = attributes.attributes["nationalities"]!!.indexOf("brit")
        assertRightOfConstraintSatisfied(
            attributeVars["colors"]!!, 
            attributeVars["nationalities"]!!, 
            redColorIndex, 
            britNationalityIndex, 
            "Red-British rightOf"
        )

        // Verify second constraint: tea right of dog
        val teaDrinkIndex = attributes.attributes["drinks"]!!.indexOf("tea")
        val dogPetIndex = attributes.attributes["pets"]!!.indexOf("dog")
        assertRightOfConstraintSatisfied(
            attributeVars["drinks"]!!, 
            attributeVars["pets"]!!, 
            teaDrinkIndex, 
            dogPetIndex, 
            "Tea-Dog rightOf"
        )
    }

    @Test
    fun `conflicting rightOf constraints should make model unsolvable`() {
        // Create conflicting constraints: red right of brit AND brit right of red (impossible)
        val conflictingConstraints = arrayOf(
            createRightOfConstraint(
                Constants.AttributeNames.COLORS,
                "red",
                Constants.AttributeNames.NATIONALITIES,
                "brit",
                "Red house is right of British house"
            ),
            createRightOfConstraint(
                Constants.AttributeNames.NATIONALITIES,
                "brit",
                Constants.AttributeNames.COLORS,
                "red",
                "British house is right of red house"
            )
        )
        
        applyConstraints(*conflictingConstraints)
        val isSolvable = solveModelWithAllDifferentConstraints()
        assertFalse(isSolvable, "Model should be unsolvable due to conflicting rightOf constraints")
    }

    @Test
    fun `rightOf constraint with same attribute should work correctly`() {
        // This is an edge case where both attributes are the same (red right of red - impossible)
        val constraint = createRightOfConstraint(
            Constants.AttributeNames.COLORS,
            "red",
            Constants.AttributeNames.COLORS,
            "red",
            "Red color right of red color (impossible)"
        )
        
        applyConstraints(constraint)
        val isSolvable = solveModelWithAllDifferentConstraints()
        assertFalse(isSolvable, "Model should be unsolvable with impossible rightOf constraint")
    }

    // Helper methods for creating constraints
    private fun createRightOfConstraint(
        attribute1: String,
        value1: String,
        attribute2: String,
        value2: String,
        description: String = "$value1 right of $value2"
    ) = RightOfConstraint(
        description = description,
        attribute1 = attribute1,
        value1 = value1,
        attribute2 = attribute2,
        value2 = value2
    )

    private fun createNonRightOfConstraint(type: String, description: String) = PositionConstraint(
        description = description,
        attribute = "colors",
        value = "red",
        position = FIRST_POSITION
    )

    // Helper methods for verification
    private fun assertRightOfConstraintSatisfied(
        attributeArray1: Array<IntVar>,
        attributeArray2: Array<IntVar>,
        expectedValue1Index: Int,
        expectedValue2Index: Int,
        constraintName: String
    ) {
        // Find which house has the first attribute value (should be on the right)
        var rightHouseIndex = -1
        for (i in attributeArray1.indices) {
            if (attributeArray1[i].value == expectedValue1Index) {
                rightHouseIndex = i
                break
            }
        }
        
        assertTrue(rightHouseIndex >= 0, "$constraintName: First attribute value should be assigned to a house")
        assertTrue(rightHouseIndex > 0, "$constraintName: First attribute cannot be in leftmost position")
        
        // The second attribute should be in the house immediately to the left
        val leftHouseIndex = rightHouseIndex - 1
        assertEquals(
            expectedValue2Index,
            attributeArray2[leftHouseIndex].value,
            "$constraintName: Second attribute value should be in the house immediately to the left of first attribute"
        )
    }

}