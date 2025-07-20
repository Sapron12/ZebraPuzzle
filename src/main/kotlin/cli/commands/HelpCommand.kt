package org.qudisoft.cli.commands

import org.qudisoft.cli.Command

class HelpCommand : Command {
    override fun execute() {
        printHelp()
    }

    private fun printHelp() {
        println("""
        Zebra Puzzle Solver - CLI Interface
        
        Usage: java -jar zebra-puzzle.jar [OPTIONS]
        
        Options:
          -i, --input <file>              Input JSON file (default: input.json)
          -o, --output <file>             Output file (default: output.json)
          -l, --log <file>                Log file (default: zebra-puzzle.log)
          -s, --strategy <name>           Solver strategy: choco, bruteforce (default: choco)
          -if, --input-format <format>    Input format: json (default: json)
          -of, --output-format <format>   Output format: console, json (default: console)
          --no-clear-log                  Don't clear log file on startup
          -h, --help                      Show this help message
        
        Examples:
          java -jar zebra-puzzle.jar
          java -jar zebra-puzzle.jar -i input10.json -s choco -of json
    """.trimIndent())
    }
}