package org.qudisoft.cli

class CliException(message: String, val showHelp: Boolean = false) : Exception(message)