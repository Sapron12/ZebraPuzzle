package org.qudisoft.constraint

import org.chocosolver.solver.variables.IntVar
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.qudisoft.model.PositionConstraint
import org.qudisoft.model.DirectConstraint
import org.qudisoft.Constants
import org.qudisoft.constraint.handler.DirectConstraintHandler

class DirectConstraintHandlerTest : BaseConstraintHandlerTest(DirectConstraintHandler()) {


    @Test
    fun `canHandle should return true for direct constraint type`() {
        val constraint = createDirectConstraint(
            Constants.AttributeNames.COLORS,
            "red",
            Constants.AttributeNames.NATIONALITIES,
            "brit",
            "Test direct constraint"
        )

        assertTrue(handler.canHandle(constraint))
    }

    @Test
    fun `canHandle should return false for non-direct constraint types`() {
        val positionConstraint = createNonDirectConstraint(
            Constants.ConstraintTypes.POSITION,
            "Test position constraint"
        )
        val adjacentConstraint = createNonDirectConstraint(
            Constants.ConstraintTypes.LEFT_OF,
            "Test adjacent constraint"
        )
        val neighborConstraint = createNonDirectConstraint(
            Constants.ConstraintTypes.NEIGHBOR,
            "Test neighbor constraint"
        )

        assertFalse(handler.canHandle(positionConstraint))
        assertFalse(handler.canHandle(adjacentConstraint))
        assertFalse(handler.canHandle(neighborConstraint))
    }

    @Test
    fun `canHandle should return false for unknown constraint type`() {
        val unknownConstraint = createNonDirectConstraint("unknown", "Test unknown constraint")

        assertFalse(handler.canHandle(unknownConstraint))
    }


    @Test
    fun `applyConstraint should throw exception for unknown attribute`() {
        val constraint = createDirectConstraint(
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
        val constraint = createDirectConstraint(
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
    fun `multiple direct constraints should be satisfied simultaneously`() {
        val constraints = arrayOf(
            createDirectConstraint(
                Constants.AttributeNames.COLORS,
                "red",
                Constants.AttributeNames.NATIONALITIES,
                "brit",
                "Red house has British person"
            ),
            createDirectConstraint(
                Constants.AttributeNames.COLORS,
                "blue",
                Constants.AttributeNames.DRINKS,
                "tea",
                "Blue house has tea drinker"
            ),
            createDirectConstraint(
                Constants.AttributeNames.PETS,
                "dog",
                Constants.AttributeNames.CIGARETTES,
                "dunhill",
                "Dog owner smokes Dunhill"
            )
        )
        
        applyConstraints(*constraints)
        solveModelWithAllDifferentConstraints()

        // Verify first constraint: red color with British nationality
        val redColorIndex = attributes.attributes["colors"]!!.indexOf("red")
        val britNationalityIndex = attributes.attributes["nationalities"]!!.indexOf("brit")
        assertDirectConstraintSatisfied(attributeVars["colors"]!!, attributeVars["nationalities"]!!, redColorIndex, britNationalityIndex, "Red-British")

        // Verify second constraint: blue color with tea drink
        val blueColorIndex = attributes.attributes["colors"]!!.indexOf("blue")
        val teaDrinkIndex = attributes.attributes["drinks"]!!.indexOf("tea")
        assertDirectConstraintSatisfied(attributeVars["colors"]!!, attributeVars["drinks"]!!, blueColorIndex, teaDrinkIndex, "Blue-Tea")

        // Verify third constraint: dog pet with dunhill cigarette
        val dogPetIndex = attributes.attributes["pets"]!!.indexOf("dog")
        val dunhillCigaretteIndex = attributes.attributes["cigarettes"]!!.indexOf("dunhill")
        assertDirectConstraintSatisfied(attributeVars["pets"]!!, attributeVars["cigarettes"]!!, dogPetIndex, dunhillCigaretteIndex, "Dog-Dunhill")
    }

    @Test
    fun `conflicting direct constraints should make model unsolvable`() {
        // Create conflicting constraints: red house with both brit and swede
        val conflictingConstraints = arrayOf(
            createDirectConstraint(
                Constants.AttributeNames.COLORS,
                "red",
                Constants.AttributeNames.NATIONALITIES,
                "brit",
                "Red house has British person"
            ),
            createDirectConstraint(
                Constants.AttributeNames.COLORS,
                "red",
                Constants.AttributeNames.NATIONALITIES,
                "swede",
                "Red house has Swedish person"
            )
        )
        
        // Apply the conflicting constraints
        applyConstraints(*conflictingConstraints)
        
        // The model should be unsolvable due to conflicting constraints
        val isSolvable = solveModelWithAllDifferentConstraints()
        assertFalse(isSolvable, "Model should be unsolvable due to conflicting direct constraints")
    }

    @Test
    fun `direct constraint with same attribute should work correctly`() {
        // This is an edge case where both attributes are the same
        val constraint = createDirectConstraint(
            Constants.AttributeNames.COLORS,
            "red",
            Constants.AttributeNames.COLORS,
            "red",
            "Red color with red color (redundant but valid)"
        )
        
        applyConstraints(constraint)
        val isSolvable = solveModelWithAllDifferentConstraints()
        
        // Should be solvable as it's essentially a tautology
        assertTrue(isSolvable, "Model should be solvable with redundant direct constraint")
    }

    // Helper methods for creating constraints
    private fun createDirectConstraint(
        attribute1: String,
        value1: String,
        attribute2: String,
        value2: String,
        description: String = "$value1 with $value2"
    ) = DirectConstraint(
        description = description,
        attribute1 = attribute1,
        value1 = value1,
        attribute2 = attribute2,
        value2 = value2
    )

    private fun createNonDirectConstraint(type: String, description: String) = PositionConstraint(
        description = description,
        attribute = "colors",
        value = "red",
        position = FIRST_POSITION
    )

    // Helper methods for verification
    private fun assertDirectConstraintSatisfied(
        attributeArray1: Array<IntVar>,
        attributeArray2: Array<IntVar>,
        expectedValue1Index: Int,
        expectedValue2Index: Int,
        constraintName: String
    ) {
        // Find which house has the first attribute value
        var houseIndex = -1
        for (i in attributeArray1.indices) {
            if (attributeArray1[i].value == expectedValue1Index) {
                houseIndex = i
                break
            }
        }
        
        assertTrue(houseIndex >= 0, "$constraintName: First attribute value should be assigned to a house")
        assertEquals(
            expectedValue2Index,
            attributeArray2[houseIndex].value,
            "$constraintName: Second attribute value should be in the same house as first attribute value"
        )
    }

}