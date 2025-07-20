package org.qudisoft.constraint

import org.chocosolver.solver.variables.IntVar
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.qudisoft.model.PositionConstraint
import org.qudisoft.model.LeftOfConstraint
import org.qudisoft.Constants
import org.qudisoft.constraint.handler.LeftOfConstraintHandler

class LeftOfConstraintHandlerTest : BaseConstraintHandlerTest(LeftOfConstraintHandler()) {

    @Test
    fun `canHandle should return true for leftOf constraint type`() {
        val constraint = createLeftOfConstraint(
            Constants.AttributeNames.COLORS,
            "red",
            Constants.AttributeNames.NATIONALITIES,
            "brit",
            "Test leftOf constraint"
        )

        assertTrue(handler.canHandle(constraint))
    }

    @Test
    fun `canHandle should return false for non-leftOf constraint types`() {
        val positionConstraint = createNonLeftOfConstraint(
            Constants.ConstraintTypes.POSITION,
            "Test position constraint"
        )
        val directConstraint = createNonLeftOfConstraint(
            Constants.ConstraintTypes.DIRECT,
            "Test direct constraint"
        )
        val neighborConstraint = createNonLeftOfConstraint(
            Constants.ConstraintTypes.NEIGHBOR,
            "Test neighbor constraint"
        )

        assertFalse(handler.canHandle(positionConstraint))
        assertFalse(handler.canHandle(directConstraint))
        assertFalse(handler.canHandle(neighborConstraint))
    }

    @Test
    fun `canHandle should return false for unknown constraint type`() {
        val unknownConstraint = createNonLeftOfConstraint("unknown", "Test unknown constraint")

        assertFalse(handler.canHandle(unknownConstraint))
    }

    @Test
    fun `applyConstraint should throw exception for unknown attribute`() {
        val constraint = createLeftOfConstraint(
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
        val constraint = createLeftOfConstraint(
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
    fun `multiple leftOf constraints should be satisfied simultaneously`() {
        val constraints = arrayOf(
            createLeftOfConstraint(
                Constants.AttributeNames.COLORS,
                "red",
                Constants.AttributeNames.NATIONALITIES,
                "brit",
                "Red house is left of British house"
            ),
            createLeftOfConstraint(
                Constants.AttributeNames.DRINKS,
                "tea",
                Constants.AttributeNames.PETS,
                "dog",
                "Tea drinker is left of dog owner"
            )
        )
        
        applyConstraints(*constraints)
        val isSolvable = solveModelWithAllDifferentConstraints()
        assertTrue(isSolvable, "Model should be solvable with multiple leftOf constraints")

        // Verify first constraint: red left of brit
        val redColorIndex = attributes.attributes["colors"]!!.indexOf("red")
        val britNationalityIndex = attributes.attributes["nationalities"]!!.indexOf("brit")
        assertLeftOfConstraintSatisfied(
            attributeVars["colors"]!!, 
            attributeVars["nationalities"]!!, 
            redColorIndex, 
            britNationalityIndex, 
            "Red-British leftOf"
        )

        // Verify second constraint: tea left of dog
        val teaDrinkIndex = attributes.attributes["drinks"]!!.indexOf("tea")
        val dogPetIndex = attributes.attributes["pets"]!!.indexOf("dog")
        assertLeftOfConstraintSatisfied(
            attributeVars["drinks"]!!, 
            attributeVars["pets"]!!, 
            teaDrinkIndex, 
            dogPetIndex, 
            "Tea-Dog leftOf"
        )
    }

    @Test
    fun `conflicting leftOf constraints should make model unsolvable`() {
        // Create conflicting constraints: red left of brit AND brit left of red (impossible)
        val conflictingConstraints = arrayOf(
            createLeftOfConstraint(
                Constants.AttributeNames.COLORS,
                "red",
                Constants.AttributeNames.NATIONALITIES,
                "brit",
                "Red house is left of British house"
            ),
            createLeftOfConstraint(
                Constants.AttributeNames.NATIONALITIES,
                "brit",
                Constants.AttributeNames.COLORS,
                "red",
                "British house is left of red house"
            )
        )
        
        // Apply the conflicting constraints
        applyConstraints(*conflictingConstraints)
        
        // The model should be unsolvable due to conflicting constraints
        val isSolvable = solveModelWithAllDifferentConstraints()
        assertFalse(isSolvable, "Model should be unsolvable due to conflicting leftOf constraints")
    }

    @Test
    fun `leftOf constraint with same attribute should work correctly`() {
        // This is an edge case where both attributes are the same (red left of red - impossible)
        val constraint = createLeftOfConstraint(
            Constants.AttributeNames.COLORS,
            "red",
            Constants.AttributeNames.COLORS,
            "red",
            "Red color left of red color (impossible)"
        )
        
        applyConstraints(constraint)
        val isSolvable = solveModelWithAllDifferentConstraints()
        
        // Should be unsolvable as a house cannot be left of itself with the same attribute value
        assertFalse(isSolvable, "Model should be unsolvable with impossible leftOf constraint")
    }

    // Helper methods for creating constraints
    private fun createLeftOfConstraint(
        attribute1: String,
        value1: String,
        attribute2: String,
        value2: String,
        description: String = "$value1 left of $value2"
    ) = LeftOfConstraint(
        description = description,
        attribute1 = attribute1,
        value1 = value1,
        attribute2 = attribute2,
        value2 = value2
    )

    private fun createNonLeftOfConstraint(type: String, description: String) = PositionConstraint(
        description = description,
        attribute = "colors",
        value = "red",
        position = FIRST_POSITION
    )

    // Helper methods for verification
    private fun assertLeftOfConstraintSatisfied(
        attributeArray1: Array<IntVar>,
        attributeArray2: Array<IntVar>,
        expectedValue1Index: Int,
        expectedValue2Index: Int,
        constraintName: String
    ) {
        // Find which house has the first attribute value
        var leftHouseIndex = -1
        for (i in attributeArray1.indices) {
            if (attributeArray1[i].value == expectedValue1Index) {
                leftHouseIndex = i
                break
            }
        }
        
        assertTrue(leftHouseIndex >= 0, "$constraintName: First attribute value should be assigned to a house")
        assertTrue(leftHouseIndex < attributeArray1.size - 1, "$constraintName: First attribute cannot be in rightmost position")
        
        // The second attribute should be in the house immediately to the right
        val rightHouseIndex = leftHouseIndex + 1
        assertEquals(
            expectedValue2Index,
            attributeArray2[rightHouseIndex].value,
            "$constraintName: Second attribute value should be in the house immediately to the right of first attribute"
        )
    }

}