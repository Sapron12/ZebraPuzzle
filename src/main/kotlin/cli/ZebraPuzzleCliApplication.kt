package org.qudisoft.cli

import org.qudisoft.cli.commands.HelpCommand
import org.qudisoft.cli.commands.SolveCommand
import org.qudisoft.io.json.JsonOutputWriter
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/**
 * Main CLI Application class that orchestrates the entire application flow
 */
class ZebraPuzzleCliApplication {
    private val argumentParser = ArgumentParser()
    private val componentFactory = ComponentFactory()
    private val logManager = LogManager()

    private val logger = LoggerFactory.getLogger(ZebraPuzzleCliApplication::class.java)

    /**
     * Main entry point for the CLI application
     */
    fun run(args: Array<String>) {
        try {
            val config = argumentParser.parse(args)
            logManager.setupLogging(config)
            val command = createCommand(config)
            command.execute()
        } catch (e: CliException) {
            handleCliError(e)
        } catch (e: Exception) {
            handleUnexpectedError(e)
        }
    }
    
    private fun createCommand(config: CliConfig): Command {
        return when {
            config.inputFile.isEmpty() -> HelpCommand()
            else -> SolveCommand(config, componentFactory)
        }
    }
    
    private fun handleCliError(e: CliException) {
        logger.warn("Error: ${e.message}")
        if (e.showHelp) {
            HelpCommand().execute()
        }
        exitProcess(1)
    }
    
    private fun handleUnexpectedError(e: Exception) {
        logger.warn("Unexpected error occurred: ${e.message}")
        exitProcess(1)
    }
}