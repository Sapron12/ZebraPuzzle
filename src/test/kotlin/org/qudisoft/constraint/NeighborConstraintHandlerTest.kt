package org.qudisoft.constraint

import org.chocosolver.solver.variables.IntVar
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.qudisoft.model.PositionConstraint
import org.qudisoft.model.NeighborConstraint
import org.qudisoft.Constants
import org.qudisoft.constraint.handler.NeighborConstraintHandler

class NeighborConstraintHandlerTest : BaseConstraintHandlerTest(NeighborConstraintHandler()) {

    @Test
    fun `canHandle should return true for neighbor constraint type`() {
        val constraint = createNeighborConstraint(
            Constants.AttributeNames.COLORS,
            "red",
            Constants.AttributeNames.NATIONALITIES,
            "brit",
            "Test neighbor constraint"
        )

        assertTrue(handler.canHandle(constraint))
    }

    @Test
    fun `canHandle should return false for non-neighbor constraint types`() {
        val positionConstraint = createNonNeighborConstraint(
            Constants.ConstraintTypes.POSITION,
            "Test position constraint"
        )
        val directConstraint = createNonNeighborConstraint(
            Constants.ConstraintTypes.DIRECT,
            "Test direct constraint"
        )
        val adjacentConstraint = createNonNeighborConstraint(
            Constants.ConstraintTypes.LEFT_OF,
            "Test adjacent constraint"
        )

        assertFalse(handler.canHandle(positionConstraint))
        assertFalse(handler.canHandle(directConstraint))
        assertFalse(handler.canHandle(adjacentConstraint))
    }

    @Test
    fun `canHandle should return false for unknown constraint type`() {
        val unknownConstraint = createNonNeighborConstraint("unknown", "Test unknown constraint")

        assertFalse(handler.canHandle(unknownConstraint))
    }

    @Test
    fun `applyConstraint should throw exception for unknown attribute`() {
        val constraint = createNeighborConstraint(
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
        val constraint = createNeighborConstraint(
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
    fun `neighbor constraint should place attributes in adjacent houses`() {
        val constraint = createNeighborConstraint(
            Constants.AttributeNames.COLORS,
            "red",
            Constants.AttributeNames.NATIONALITIES,
            "brit",
            "Red house is adjacent to British house"
        )
        
        applyConstraints(constraint)
        val isSolvable = solveModelWithAllDifferentConstraints()
        assertTrue(isSolvable, "Model should be solvable with neighbor constraint")

        // Verify the neighbor constraint: red should be adjacent to brit
        val redColorIndex = attributes.attributes["colors"]!!.indexOf("red")
        val britNationalityIndex = attributes.attributes["nationalities"]!!.indexOf("brit")
        assertNeighborConstraintSatisfied(
            attributeVars["colors"]!!, 
            attributeVars["nationalities"]!!, 
            redColorIndex, 
            britNationalityIndex, 
            "Red-British neighbor"
        )
    }

    @Test
    fun `multiple neighbor constraints should be satisfied simultaneously`() {
        val constraints = arrayOf(
            createNeighborConstraint(
                Constants.AttributeNames.COLORS,
                "red",
                Constants.AttributeNames.NATIONALITIES,
                "brit",
                "Red house is adjacent to British house"
            ),
            createNeighborConstraint(
                Constants.AttributeNames.DRINKS,
                "tea",
                Constants.AttributeNames.PETS,
                "dog",
                "Tea drinker is adjacent to dog owner"
            )
        )
        
        applyConstraints(*constraints)
        val isSolvable = solveModelWithAllDifferentConstraints()
        assertTrue(isSolvable, "Model should be solvable with multiple neighbor constraints")

        // Verify first constraint: red adjacent to brit
        val redColorIndex = attributes.attributes["colors"]!!.indexOf("red")
        val britNationalityIndex = attributes.attributes["nationalities"]!!.indexOf("brit")
        assertNeighborConstraintSatisfied(
            attributeVars["colors"]!!, 
            attributeVars["nationalities"]!!, 
            redColorIndex, 
            britNationalityIndex, 
            "Red-British neighbor"
        )

        // Verify second constraint: tea adjacent to dog
        val teaDrinkIndex = attributes.attributes["drinks"]!!.indexOf("tea")
        val dogPetIndex = attributes.attributes["pets"]!!.indexOf("dog")
        assertNeighborConstraintSatisfied(
            attributeVars["drinks"]!!, 
            attributeVars["pets"]!!, 
            teaDrinkIndex, 
            dogPetIndex, 
            "Tea-Dog neighbor"
        )
    }

    @Test
    fun `conflicting neighbor constraints should make model unsolvable`() {
        // Create impossible scenario with circular adjacency requirements
        val conflictingConstraints = arrayOf(
            createNeighborConstraint(
                Constants.AttributeNames.COLORS,
                "red",
                Constants.AttributeNames.NATIONALITIES,
                "brit",
                "Red house is adjacent to British house"
            ),
            createNeighborConstraint(
                Constants.AttributeNames.NATIONALITIES,
                "brit",
                Constants.AttributeNames.DRINKS,
                "tea",
                "British house is adjacent to tea drinker"
            ),
            createNeighborConstraint(
                Constants.AttributeNames.DRINKS,
                "tea",
                Constants.AttributeNames.PETS,
                "dog",
                "Tea drinker is adjacent to dog owner"
            ),
            createNeighborConstraint(
                Constants.AttributeNames.PETS,
                "dog",
                Constants.AttributeNames.CIGARETTES,
                "dunhill",
                "Dog owner is adjacent to Dunhill smoker"
            ),
            createNeighborConstraint(
                Constants.AttributeNames.CIGARETTES,
                "dunhill",
                Constants.AttributeNames.COLORS,
                "red",
                "Dunhill smoker is adjacent to red house"
            )
        )
        
        // Apply the conflicting constraints (creates a circular chain that's impossible in 5 houses)
        applyConstraints(*conflictingConstraints)
        
        // The model should be unsolvable due to conflicting constraints
        val isSolvable = solveModelWithAllDifferentConstraints()
        assertFalse(isSolvable, "Model should be unsolvable due to conflicting neighbor constraints")
    }

    @Test
    fun `neighbor constraint with overlapping requirements should work correctly`() {
        // Test neighbor constraints that share common attributes
        val constraints = arrayOf(
            createNeighborConstraint(
                Constants.AttributeNames.COLORS,
                "red",
                Constants.AttributeNames.NATIONALITIES,
                "brit",
                "Red house is adjacent to British house"
            ),
            createNeighborConstraint(
                Constants.AttributeNames.COLORS,
                "red",
                Constants.AttributeNames.DRINKS,
                "tea",
                "Red house is adjacent to tea drinker"
            )
        )
        
        applyConstraints(*constraints)
        val isSolvable = solveModelWithAllDifferentConstraints()
        assertTrue(isSolvable, "Model should be solvable with overlapping neighbor constraints")

        // Verify both constraints are satisfied
        val redColorIndex = attributes.attributes["colors"]!!.indexOf("red")
        val britNationalityIndex = attributes.attributes["nationalities"]!!.indexOf("brit")
        val teaDrinkIndex = attributes.attributes["drinks"]!!.indexOf("tea")
        
        assertNeighborConstraintSatisfied(
            attributeVars["colors"]!!, 
            attributeVars["nationalities"]!!, 
            redColorIndex, 
            britNationalityIndex, 
            "Red-British neighbor"
        )
        
        assertNeighborConstraintSatisfied(
            attributeVars["colors"]!!, 
            attributeVars["drinks"]!!, 
            redColorIndex, 
            teaDrinkIndex, 
            "Red-Tea neighbor"
        )
    }

    @Test
    fun `neighbor constraint with same attribute should work correctly`() {
        // This is an edge case where both attributes are the same (red adjacent to red - impossible)
        val constraint = createNeighborConstraint(
            Constants.AttributeNames.COLORS,
            "red",
            Constants.AttributeNames.COLORS,
            "red",
            "Red color adjacent to red color (impossible)"
        )
        
        applyConstraints(constraint)
        val isSolvable = solveModelWithAllDifferentConstraints()
        
        // Should be unsolvable as a house cannot be adjacent to itself with the same attribute value
        assertFalse(isSolvable, "Model should be unsolvable with impossible neighbor constraint")
    }

    // Helper methods for creating constraints
    private fun createNeighborConstraint(
        attribute1: String,
        value1: String,
        attribute2: String,
        value2: String,
        description: String = "$value1 adjacent to $value2"
    ) = NeighborConstraint(
        description = description,
        attribute1 = attribute1,
        value1 = value1,
        attribute2 = attribute2,
        value2 = value2
    )

    private fun createNonNeighborConstraint(type: String, description: String) = PositionConstraint(
        description = description,
        attribute = "colors",
        value = "red",
        position = FIRST_POSITION
    )

    // Helper methods for verification
    private fun assertNeighborConstraintSatisfied(
        attributeArray1: Array<IntVar>,
        attributeArray2: Array<IntVar>,
        expectedValue1Index: Int,
        expectedValue2Index: Int,
        constraintName: String
    ) {
        // Find which house has the first attribute value
        var house1Index = -1
        for (i in attributeArray1.indices) {
            if (attributeArray1[i].value == expectedValue1Index) {
                house1Index = i
                break
            }
        }
        
        assertTrue(house1Index >= 0, "$constraintName: First attribute value should be assigned to a house")
        
        // Find which house has the second attribute value
        var house2Index = -1
        for (i in attributeArray2.indices) {
            if (attributeArray2[i].value == expectedValue2Index) {
                house2Index = i
                break
            }
        }
        
        assertTrue(house2Index >= 0, "$constraintName: Second attribute value should be assigned to a house")
        
        // The houses should be adjacent (difference of 1)
        val distance = kotlin.math.abs(house1Index - house2Index)
        assertEquals(
            1,
            distance,
            "$constraintName: Houses should be adjacent (distance of 1), but found distance $distance between positions ${house1Index + 1} and ${house2Index + 1}"
        )
    }

}