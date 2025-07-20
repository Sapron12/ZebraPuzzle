package org.qudisoft

/**
 * Centralized constants for the Zebra Puzzle application
 */
object Constants {

    object ConstraintTypes {
        const val DIRECT = "direct"
        const val LEFT_OF = "leftOf"
        const val NEIGHBOR = "neighbor"
        const val POSITION = "position"
    }

    object AttributeNames {
        const val COLORS = "colors"
        const val NATIONALITIES = "nationalities"
        const val DRINKS = "drinks"
        const val CIGARETTES = "cigarettes"
        const val PETS = "pets"
    }

    object FileNames {
        const val DEFAULT_INPUT = "input.json"
        const val DEFAULT_OUTPUT = "output.json"
        const val TEN_HOUSES_INPUT = "input10.json"
        const val TEN_HOUSES_OUTPUT = "output10.json"

    }

    object ErrorMessages {
        const val UNKNOWN_ATTRIBUTE = "Unknown attribute"
    }

    object Operators {
        const val EQUALS = "="
    }
}