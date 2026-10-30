# Development Guide

This guide explains how to work on the 16-bit CPU Simulator and how to add functionality safely.

## Development environment

Install:

- JDK 17 or newer
- Git
- A Java-aware editor or IDE

The project does not use Maven or Gradle. Source files are compiled directly with `javac`.

If GNU Make is available, the repository also provides shortcuts:

```bash
make build
make test
make run
make clean
```

## Build and test

From the repository root:

```bash
mkdir out
javac -d out *.java
java -cp out simulator.TestRunner
```

Run the GUI with:

```bash
java -cp out simulator.GUI
```

The source files use the `simulator` package. Always compile with `-d out` or another output directory so Java creates the correct package structure.

Generated `.class` files and build directories are excluded by `.gitignore`.

The repository includes an [`.editorconfig`](../.editorconfig) file with shared formatting rules for source code, assembly programs, documentation, YAML, and Makefiles.

## Source organization

| File | Responsibility |
| --- | --- |
| `ALU.java` | Arithmetic, logical operations, and status flags |
| `Assembler.java` | Two-pass assembly parser and label resolution |
| `CPU.java` | Instruction execution, program counter, and stack behavior |
| `Instruction.java` | Instruction, register, opcode, and assembler IR definitions |
| `Memory.java` | General RAM and stack storage |
| `GUI.java` | Swing editor and execution interface |
| `TestALU.java` | ALU tests |
| `TestAssembler.java` | Assembler tests |
| `TestCPU.java` | CPU integration tests |
| `TestRunner.java` | Lightweight test runner |

Assembly examples are stored in [`samples/`](../samples/). Detailed behavior is documented in [`architecture.md`](architecture.md) and [`instruction-set.md`](instruction-set.md).

## Adding an instruction

Adding an instruction usually requires changes in four places.

### 1. Add the opcode

Add the opcode to the `OpCodes` enum in `Instruction.java`, in the appropriate category.

### 2. Parse the instruction

Add a case to the instruction parser in `Assembler.java`.

Use the existing helpers where possible:

- `parseTwoOperandInst` for register/immediate two-operand instructions
- `parseJumpInst` for labels and immediate control-flow targets

Validate operand counts and preserve source line information in error messages.

### 3. Execute the instruction

Add a case to `CPU.step`.

Consider:

- Which registers or memory locations are read
- Which values are changed
- Whether the program counter changes
- Whether ALU flags should be updated
- Whether operands must be limited to 16 bits
- Whether invalid addresses or stack boundaries need handling

### 4. Add tests and documentation

Add focused tests to `TestAssembler.java`, `TestCPU.java`, or `TestALU.java`.

Update:

- [`README.md`](../README.md)
- [`docs/instruction-set.md`](instruction-set.md)
- [`CHANGELOG.md`](../CHANGELOG.md)
- A sample program when the instruction is useful for beginners

## Adding assembler behavior

Assembler changes should test both successful and invalid input.

For a new syntax form, test:

- Normal valid input
- Whitespace and comma variations
- Case-insensitive input
- Labels where applicable
- Missing operands
- Extra operands
- Invalid registers
- Invalid immediate values
- Undefined or duplicate labels

Assembler errors should identify the source line whenever possible.

## Adding CPU behavior

CPU tests should verify observable state, not only that execution completes.

Check relevant:

- Register values
- `PC`
- `SP`
- Memory bytes
- ALU flags
- Branch targets
- Stack contents
- Halt behavior

For loops and branches, use a maximum step count so a broken condition cannot hang the test process.

## Test runner conventions

Tests are registered through `TestRunner`:

```java
runner.runTest("descriptive test name", () -> {
    runner.resetCPU();
    // Arrange, execute, and assert.
});
```

Use `runner.resetCPU()` at the beginning of CPU tests. This prevents state from leaking between tests.

Use:

- `assertEquals` for 16-bit values
- `assertEqualsExact` for values where signedness matters
- `assertTrue` and `assertFalse` for conditions
- `fail` when an expected exception or behavior does not occur

## Working with the GUI

When changing GUI behavior:

1. Compile the project.
2. Launch `simulator.GUI`.
3. Open or edit a sample assembly file.
4. Save the file.
5. Test **Step**, **Run**, and **Stop** where relevant.
6. Test **Reset** when changing execution-state behavior.
7. Confirm source highlighting, register display, and memory display remain consistent.

The GUI executes saved source files. Changes typed into the editor must be saved before running or stepping.

## Code quality rules

- Keep changes focused.
- Follow the existing package and naming conventions.
- Prefer clear, direct code over unnecessary abstractions.
- Preserve 16-bit masking behavior.
- Avoid adding dependencies when the JDK is sufficient.
- Do not commit generated files or IDE metadata.
- Update documentation for user-visible behavior.
- Add regression tests for fixed bugs.

## Validation checklist

Before opening a pull request, run:

```bash
git diff --check
javac -d out *.java
java -cp out simulator.TestRunner
```

If the change affects the GUI, also run:

```bash
java -cp out simulator.GUI
```

Review the final diff and confirm that only intended files are included.

## Pull requests

Pull requests should explain:

- What changed
- Why the change was needed
- How it was tested
- Whether documentation was updated

Use the repository pull request template and include the actual test results.
