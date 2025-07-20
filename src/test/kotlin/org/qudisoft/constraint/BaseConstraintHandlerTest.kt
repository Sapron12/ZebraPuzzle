package org.qudisoft.constraint

import org.chocosolver.solver.Model
import org.chocosolver.solver.variables.IntVar
import org.junit.jupiter.api.BeforeEach
import org.qudisoft.model.PuzzleConstraint
import org.qudisoft.model.PuzzleAttributes

/**
 * Base test class containing common functionality for constraint handler tests.
 */
abstract class BaseConstraintHandlerTest(val handler: ConstraintHandler) {

    protected lateinit var model: Model
    protected lateinit var attributes: PuzzleAttributes
    protected lateinit var attributeVars: Map<String, Array<IntVar>>

    companion object {
        protected const val HOUSE_COUNT = 5
        protected const val MIN_ATTRIBUTE_INDEX = 0
        protected const val MAX_ATTRIBUTE_INDEX = 4
        protected const val FIRST_POSITION = 1
        protected const val SECOND_POSITION = 2
        protected const val THIRD_POSITION = 3
        protected const val FOURTH_POSITION = 4
        protected const val FIFTH_POSITION = 5
    }

    @BeforeEach
    fun setUp() {
        model = Model("TestModel")
        attributes = createTestAttributes()
        initializeVariableArrays()
    }


    protected fun createTestAttributes() = PuzzleAttributes(
        attributes = mapOf(
            "colors" to listOf("red", "blue", "green", "yellow", "white"),
            "nationalities" to listOf("brit", "swede", "dane", "norwegian", "german"),
            "drinks" to listOf("tea", "coffee", "milk", "beer", "water"),
            "cigarettes" to listOf("pall mall", "dunhill", "blend", "blue master", "prince"),
            "pets" to listOf("dog", "bird", "cat", "horse", "fish")
        )
    )

    protected fun initializeVariableArrays() {
        attributeVars = attributes.attributes.keys.associateWith { attributeName ->
            Array(HOUSE_COUNT) { houseIndex ->
                model.intVar("${attributeName.removeSuffix("s")}_$houseIndex", MIN_ATTRIBUTE_INDEX, MAX_ATTRIBUTE_INDEX)
            }
        }
    }

    protected fun solveModelWithAllDifferentConstraints(): Boolean {
        attributeVars.values.forEach { attributeArray ->
            model.allDifferent(*attributeArray).post()
        }
        return model.solver.solve()
    }

    /**
     * Common method to apply constraints using the handler
     */
    protected fun applyConstraints(vararg constraints: PuzzleConstraint) {
        val attributeVars = createAttributeVarsMap()
        constraints.forEach {
            handler.applyConstraint(it, model, attributes, attributeVars)
        }
    }

    /**
     * Common helper method to create the attribute variables map
     */
    protected fun createAttributeVarsMap(): Map<String, Array<IntVar>> {
        return attributeVars
    }
}