package org.qudisoft.io.console

import org.qudisoft.io.OutputWriter
import org.qudisoft.model.PuzzleSolution

class ConsoleOutputWriter: OutputWriter {
    override fun writeOutput(solution: PuzzleSolution, destination: String) {
        printToConsole(solution)
    }

    private fun printToConsole(solution: PuzzleSolution) {
        println("=".repeat(60))
        println("ZEBRA PUZZLE SOLUTION")
        println("=".repeat(60))
        println("Description: ${solution.description}")
        println("Solved: ${solution.solved}")
        println()

        if (solution.solved) {
            printHousesTable(solution)
            printAnswers(solution)
        } else {
            println("❌ No solution found!")
        }
        println("=".repeat(60))
    }

    private fun printHousesTable(solution: PuzzleSolution) {
        val attributeNames = validateHousesData(solution) ?: return
        val attributeColumnWidths = calculateColumnWidths(solution, attributeNames)
        val totalWidth = calculateTotalWidth(attributeColumnWidths, attributeNames)
        
        printTableHeader(attributeNames, attributeColumnWidths, totalWidth)
        printHouseRows(solution, attributeNames, attributeColumnWidths)
        printTableFooter(totalWidth)
    }

    private fun validateHousesData(solution: PuzzleSolution): List<String>? {
        if (solution.houses.isEmpty()) {
            println("No houses to display.")
            return null
        }

        val attributeNames = solution.houses.flatMap { it.attributes.keys }.distinct().sorted()
        if (attributeNames.isEmpty()) {
            println("No attributes to display.")
            return null
        }

        return attributeNames
    }

    private fun calculateColumnWidths(solution: PuzzleSolution, attributeNames: List<String>): Map<String, Int> {
        return attributeNames.associateWith { attributeName ->
            val headerWidth = attributeName.length
            val maxValueWidth = solution.houses.maxOfOrNull { house ->
                (house.attributes[attributeName] ?: "N/A").length
            } ?: 3
            maxOf(headerWidth, maxValueWidth, 6) + 2 // minimum 6 chars + padding
        }
    }

    private fun calculateTotalWidth(attributeColumnWidths: Map<String, Int>, attributeNames: List<String>): Int {
        val houseColumnWidth = 7
        return houseColumnWidth + attributeColumnWidths.values.sum() + attributeNames.size + 1
    }

    private fun printTableHeader(attributeNames: List<String>, attributeColumnWidths: Map<String, Int>, totalWidth: Int) {
        println("Houses:")
        println("-".repeat(totalWidth))

        val headerFormat = "| %-5s |" + attributeNames.joinToString("") { attributeName ->
            " %-${attributeColumnWidths[attributeName]!! - 2}s |"
        }
        println(headerFormat.format("House", *attributeNames.map { it.replaceFirstChar { char -> char.uppercase() } }.toTypedArray()))
        println("-".repeat(totalWidth))
    }

    private fun printHouseRows(solution: PuzzleSolution, attributeNames: List<String>, attributeColumnWidths: Map<String, Int>) {
        for (house in solution.houses) {
            val rowFormat = "| %-5d |" + attributeNames.joinToString("") { attributeName ->
                " %-${attributeColumnWidths[attributeName]!! - 2}s |"
            }
            val values = attributeNames.map { attributeName ->
                house.attributes[attributeName] ?: "N/A"
            }.toTypedArray()
            println(rowFormat.format(house.house, *values))
        }
    }

    private fun printTableFooter(totalWidth: Int) {
        println("-".repeat(totalWidth))
        println()
    }

    private fun printAnswers(solution: PuzzleSolution) {
        if (solution.answers.isNotEmpty()) {
            println("ANSWERS:")
            println("-".repeat(40))
            solution.answers.forEach { answer ->
                if (answer.answer != null) {
                    println("-- ${answer.question}: ${answer.answer}")
                } else {
                    println("-- ${answer.question}: No answer found")
                }
            }
            println()
        }
    }
}