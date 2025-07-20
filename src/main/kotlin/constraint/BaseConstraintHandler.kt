package org.qudisoft.constraint

import org.chocosolver.solver.variables.IntVar
import org.qudisoft.model.PuzzleAttributes
import org.qudisoft.Constants

abstract class BaseConstraintHandler : ConstraintHandler {
    
    protected fun getAttributeArray(
        attributeName: String,
        attributeVars: Map<String, Array<IntVar>>
    ): Array<IntVar> {
        return attributeVars[attributeName]
            ?: throw IllegalArgumentException("${Constants.ErrorMessages.UNKNOWN_ATTRIBUTE}: $attributeName")
    }
    
    protected fun getValueIndex(attributeName: String, value: String, attributes: PuzzleAttributes): Int {
        val attributeList = attributes.attributes[attributeName]
            ?: throw IllegalArgumentException("${Constants.ErrorMessages.UNKNOWN_ATTRIBUTE}: $attributeName")
        return attributeList.indexOf(value)
    }
}