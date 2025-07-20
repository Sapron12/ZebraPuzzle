package org.qudisoft.cli.commands

import org.qudisoft.ZebraPuzzleOrchestrator
import org.qudisoft.cli.Command
import org.qudisoft.cli.CliConfig
import org.qudisoft.cli.ComponentFactory


class SolveCommand(
    private val config: CliConfig,
    private val componentFactory: ComponentFactory
) : Command {
    
    override fun execute() {
        val inputReader = componentFactory.createInputReader(config.inputFormat)
        val puzzleSolver = componentFactory.createSolver(config.strategy)
        val outputWriter = componentFactory.createOutputWriter(config.outputFormat)
        
        val orchestrator = ZebraPuzzleOrchestrator(inputReader, puzzleSolver, outputWriter)
        orchestrator.solvePuzzle(config.inputFile, config.outputFile)
    }
}