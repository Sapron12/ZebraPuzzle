package org.qudisoft.cli

import java.io.File

class LogManager {
    fun setupLogging(config: CliConfig) {
        if (config.clearLog) {
            val logFile = File(config.logFile)
            if (logFile.exists()) {
                logFile.delete()
            }
        }
    }
}