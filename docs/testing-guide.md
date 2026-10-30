# Testing Guide

This guide explains how to build, run, and extend the test suite for the 16-bit CPU Simulator.

## Test framework

The project uses a lightweight test runner implemented in [`TestRunner.java`](../TestRunner.java). It does not require JUnit, Maven, Gradle, or any external dependency.

Tests are registered by:

- [`TestALU.java`](../TestALU.java)
- [`TestAssembler.java`](../TestAssembler.java)
- [`TestCPU.java`](../TestCPU.java)

## Running the tests

Compile the project:

```bash
javac -d out *.java
```

Run the test suite:

```bash
java -cp out simulator.TestRunner
```

If GNU Make is available:

```bash
make test
```

The suite should report:

```text
Ran 30 tests, failures: 0
```

The process exits with status `1` when at least one test fails.

## Test categories

### ALU tests

[`TestALU.java`](../TestALU.java) verifies:

- Addition overflow
- Carry behavior
- Subtraction borrow
- Zero and negative flags
- Signed overflow flags

### Assembler tests

[`TestAssembler.java`](../TestAssembler.java) verifies:

- Decimal immediates
- Hexadecimal immediates
- Binary immediates
- Negative immediates
- Label resolution
- Comments
- Labels on instruction lines
- Invalid instruction errors
- Missing operands
- Duplicate labels

### CPU tests

[`TestCPU.java`](../TestCPU.java) verifies:

- Register movement and arithmetic
- Little-endian load and store behavior
- Push and pop operations
- Conditional branches
- Fibonacci execution
- Relative jumps
- Subroutine calls and returns
- Borrow-based branching
- Stack overflow detection

## Test structure

Use the shared `TestRunner` instance and reset CPU state before each CPU test:

```java
runner.runTest("CPU example", () -> {
    runner.resetCPU();
    AsmIR code = Assembler.assemble("mov r0, #1");
    runner.runAsmProgramSafely(code, 100);
    runner.assertEquals(1, CPU.registers[Registers.R0.ordinal()],
            "r0 should contain 1");
});
```

`resetCPU()` creates fresh memory, registers, ALU state, and halt state. This prevents one test from affecting another.

## Assertions

Use the assertion that matches the value being tested:

| Assertion | Use |
| --- | --- |
| `assertEquals` | 16-bit values where only the lower 16 bits matter |
| `assertEqualsExact` | Exact signed or non-masked values |
| `assertTrue` | Conditions that must be true |
| `assertFalse` | Conditions that must be false |
| `fail` | Explicitly mark an unexpected successful operation |

## Testing CPU programs safely

Always provide a maximum step count when running assembled programs:

```java
runner.runAsmProgramSafely(code, 100);
```

This prevents a broken loop from hanging the test process indefinitely.

Choose a limit large enough for the expected program, but small enough to expose accidental infinite loops quickly.

## Adding a test

1. Choose the appropriate test class.
2. Reset CPU state when testing CPU behavior.
3. Use the smallest program that demonstrates the behavior.
4. Assert observable state such as registers, flags, memory, `PC`, or `SP`.
5. Add an invalid-input test when changing parser behavior.
6. Run the complete suite.

For a new instruction, test at least:

- Valid register operands
- Valid immediate operands, if supported
- Result values
- Flag changes, if applicable
- Program-counter behavior
- Invalid operand counts
- Invalid operand types

## Continuous integration

The GitHub Actions workflow in [`.github/workflows/java-ci.yml`](../.github/workflows/java-ci.yml):

1. Checks out the repository.
2. Installs JDK 17.
3. Compiles all Java sources.
4. Runs `simulator.TestRunner`.

Pull requests should not be considered ready until the same commands pass locally.

## Validation checklist

Before submitting a change:

```bash
git diff --check
javac -d out *.java
java -cp out simulator.TestRunner
```

If the change affects the GUI, also launch:

```bash
java -cp out simulator.GUI
```

Verify the affected workflow manually and include the result in the pull request description.
