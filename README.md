# Zebra Puzzle Solver

A command-line application for solving Zebra Puzzles (also known as Einstein's Riddle) using constraint programming.

## Using the JAR File

### Prerequisites

- Java 17 or higher

### Running the Application

Run the application using the following command:

```
java -jar zebra-puzzle-1.0-SNAPSHOT.jar [options]
```

#### Command-line Options

The application accepts the following command-line options:

- `--input <file>` - Path to the input file (default: `input.json`)
- `--input-format <format>` - Format of the input file (default: `json`)
- `--output <file>` - Path to the output file (default: `output.json`)
- `--output-format <format>` - Format of the output file (default: `json`)
- `--strategy <strategy>` - Solver strategy to use (default: `choco`)
- `--log-file <file>` - Path to the log file (default: `zebra-puzzle.log`)
- `--no-clear-log` - Whether to clear the log file (default: `сlearing logs before start`)

#### Examples

Solve a puzzle using the default input and output files:

```
java -jar zebra-puzzle-1.0-SNAPSHOT.jar
```

Solve a puzzle using a custom input file:

```
java -jar zebra-puzzle-1.0-SNAPSHOT.jar --input input.json --output-format console
```

Solve a puzzle and write the output to a custom file:

```
java -jar zebra-puzzle-1.0-SNAPSHOT.jar --input input.json --output my-solution.json
```

### Constraint Types

The application supports the following constraint types:

- `direct` - Direct association between two attributes
- `leftOf` - One attribute is to the left of another
- `rightOf` - One attribute is to the right of another
- `neighbor` - One attribute is to the right or to the left of another
- `position` - An attribute is at a specific position
