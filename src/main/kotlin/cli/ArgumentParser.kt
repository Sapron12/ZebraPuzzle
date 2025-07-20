package org.qudisoft.cli

class ArgumentParser {
    
    fun parse(args: Array<String>): CliConfig {
        var config = CliConfig()
        
        var i = 0
        while (i < args.size) {
            when (args[i]) {
                "-i", "--input" -> {
                    config = config.copy(inputFile = getNextArgument(args, i, args[i]))
                    i++
                }
                "-if", "--input-format" -> {
                    config = config.copy(inputFormat = getNextArgument(args, i, args[i]))
                    i++
                }
                "-o", "--output" -> {
                    config = config.copy(outputFile = getNextArgument(args, i, args[i]))
                    i++
                }
                "-l", "--log" -> {
                    config = config.copy(logFile = getNextArgument(args, i, args[i]))
                    i++
                }
                "-s", "--strategy" -> {
                    config = config.copy(strategy = getNextArgument(args, i, args[i]))
                    i++
                }
                "-of", "--output-format" -> {
                    config = config.copy(outputFormat = getNextArgument(args, i, args[i]))
                    i++
                }
                "--no-clear-log" -> {
                    config = config.copy(clearLog = false)
                }
                "-h", "--help" -> {
                    throw CliException("Help requested", showHelp = true)
                }
                else -> {
                    throw CliException("Unknown option: ${args[i]}", showHelp = true)
                }
            }
            i++
        }
        
        return config
    }
    
    private fun getNextArgument(args: Array<String>, currentIndex: Int, optionName: String): String {
        if (currentIndex + 1 >= args.size) {
            throw CliException("$optionName requires a value", showHelp = true)
        }
        return args[currentIndex + 1]
    }
}