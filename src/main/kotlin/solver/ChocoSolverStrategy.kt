package org.qudisoft.solver

import org.chocosolver.solver.Model
import org.chocosolver.solver.variables.IntVar
import org.qudisoft.constraint.ConstraintHandlerFactory
import org.qudisoft.model.*
import org.slf4j.LoggerFactory
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

/**
 * Choco Solver implementation of the puzzle solving strategy
 */
class ChocoSolverStrategy : PuzzleSolver {

    private val logger = LoggerFactory.getLogger(ChocoSolverStrategy::class.java)
    private val constraintFactory = ConstraintHandlerFactory()

    override fun solve(puzzleInput: PuzzleInput): PuzzleSolution {
        val puzzle = puzzleInput.puzzle
        val model = createModel(puzzle)
        val attributeVars = createAttributeVariables(model, puzzle)
        applyConstraints(model, puzzle, attributeVars)
        return solvePuzzle(model, puzzle, attributeVars)
    }

    private fun createModel(puzzle: PuzzleData): Model {
        logger.debug("Creating Choco solver model for puzzle: ${puzzle.description}")
        return Model("Zebra Puzzle")
    }

    private fun createAttributeVariables(model: Model, puzzle: PuzzleData): MutableMap<String, Array<IntVar>> {
        logger.debug("Creating variables for ${puzzle.attributes.attributes.size} attributes and ${puzzle.houses} houses")
        val attributeVars = mutableMapOf<String, Array<IntVar>>()

        for ((attributeName, attributeValues) in puzzle.attributes.attributes) {
            val vars = Array(puzzle.houses) {
                model.intVar("${attributeName}_$it", 0, attributeValues.size - 1)
            }
            attributeVars[attributeName] = vars
            logger.debug("Created variables for attribute '$attributeName' with ${attributeValues.size} possible values")

            // All different constraints - each attribute value appears exactly once
            model.allDifferent(*vars).post()
        }
        logger.info("Successfully created ${attributeVars.size} attribute variable arrays with all-different constraints")

        return attributeVars
    }

    private fun applyConstraints(
        model: Model,
        puzzle: PuzzleData,
        attributeVars: MutableMap<String, Array<IntVar>>
    ) {
        logger.debug("Applying ${puzzle.constraints.size} puzzle constraints")
        for ((index, constraint) in puzzle.constraints.withIndex()) {
            try {
                val handler = constraintFactory.getHandler(constraint)
                handler.applyConstraint(constraint, model, puzzle.attributes, attributeVars)
                logger.debug("Applied constraint ${index + 1}/${puzzle.constraints.size}: ${constraint::class.simpleName}")
            } catch (e: Exception) {
                logger.error("Failed to apply constraint ${index + 1}: ${constraint::class.simpleName}", e)
                throw e
            }
        }
        logger.info("Successfully applied all ${puzzle.constraints.size} constraints")
    }

    private fun solvePuzzle(
        model: Model,
        puzzle: PuzzleData,
        attributeVars: MutableMap<String, Array<IntVar>>
    ): PuzzleSolution {
        logger.debug("Starting constraint solving process")
        // TODO: FUTURE DEVELOPMENT - look and findOptimalSolution() solution or findAllOptimalSolutions for multiple response
        val solution = model.solver.findSolution()

        return if (solution != null) {
            logger.info("Puzzle solved successfully, building solution")
            buildSolution(puzzle, attributeVars)
        } else {
            logger.warn("No solution found for the puzzle")
            PuzzleSolution(
                description = puzzle.description,
                solved = false,
                houses = emptyList(),
                answers = emptyList(),
            )
        }
    }

    private fun buildHouses(
        puzzle: PuzzleData,
        attributeVars: Map<String, Array<IntVar>>
    ) = List(puzzle.houses) { houseIndex ->
        val houseAttributes = attributeVars.mapValues { (attributeName, vars) ->
            val attributeValues = puzzle.attributes.attributes[attributeName]!!
            attributeValues[vars[houseIndex].value]
        }
        HouseSolution(houseIndex + 1, houseAttributes)
    }


    private fun buildAnswers(puzzle: PuzzleData, houses: List<HouseSolution>) = puzzle
        .questions
        .mapIndexed { index, question ->
            logger.debug("Processing question ${index + 1}/${puzzle.questions.size}: '${question.description}'")
            val answer = findAnswerForQuestion(question, houses)
            logger.debug("Answer for '${question.description}': ${answer ?: "No answer found"}")
            QuestionAnswer(question.description, answer)
        }


    private fun buildSolution(
        puzzle: PuzzleData,
        attributeVars: Map<String, Array<IntVar>>
    ): PuzzleSolution {
        val houses = buildHouses(puzzle, attributeVars)
        val answers = buildAnswers(puzzle, houses)

        logger.info("Successfully built solution with ${houses.size} houses and ${answers.size} answers")
        return PuzzleSolution(
            description = puzzle.description,
            solved = true,
            houses = houses,
            answers = answers
        )
    }

    private fun findAnswerForQuestion(question: Question, houses: List<HouseSolution>): String? {
        logger.debug("Finding answer for question: targetAttribute='${question.targetAttribute}', givenAttribute='${question.givenAttribute}', givenValue='${question.givenValue}'")
        val foundHouse = houses.find { house ->
            house.attributes[question.givenAttribute] == question.givenValue
        }
        return when (question.targetAttribute) {
            "house" -> {
                foundHouse?.house?.toString()
            }

            else -> {
                foundHouse?.attributes?.get(question.targetAttribute)
            }
        }
    }
}