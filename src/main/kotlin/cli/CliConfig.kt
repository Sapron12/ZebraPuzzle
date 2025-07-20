package org.qudisoft.cli

import org.qudisoft.Constants

/**
 * Configuration data class for CLI arguments
 */
data class CliConfig(
    val inputFile: String = Constants.FileNames.DEFAULT_INPUT,
    val inputFormat: String = "json",
    val outputFile: String = Constants.FileNames.DEFAULT_OUTPUT,
    val logFile: String = "zebra-puzzle.log",
    val strategy: String = "choco",
    val outputFormat: String = "json",
    val clearLog: Boolean = true
)