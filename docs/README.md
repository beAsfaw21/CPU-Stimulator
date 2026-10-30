# Documentation

This directory contains the detailed documentation for the 16-bit CPU Simulator.

## Guides

### [Architecture](architecture.md)

Explains the internal CPU design, including:

- Assembly-to-execution pipeline
- Fetch-decode-execute cycle
- Registers and program counter
- ALU flags
- Memory map
- Stack and subroutine behavior

### [Instruction set reference](instruction-set.md)

Documents every supported instruction, including syntax, operands, flag behavior, control flow, memory access, stack operations, and subroutines.

### [Assembly language guide](assembly-guide.md)

Introduces assembly programming for beginners with examples covering:

- Registers
- Immediate values
- Labels
- Arithmetic
- Comparisons
- Loops
- Memory
- Stack operations
- Subroutines
- Debugging

### [Development guide](development-guide.md)

Explains how to build, test, extend, and contribute to the project. It includes guidance for adding instructions, writing tests, changing the assembler, and validating GUI changes.

## Examples

Runnable assembly examples are available in the repository's [`samples/`](../samples/) directory:

- [`addition.asm`](../samples/addition.asm)
- [`branching.asm`](../samples/branching.asm)
- [`fibonacci.asm`](../samples/fibonacci.asm)
- [`memory.asm`](../samples/memory.asm)
- [`subroutine.asm`](../samples/subroutine.asm)

## Recommended reading order

If you are new to the project:

1. Start with the [assembly language guide](assembly-guide.md).
2. Run the examples in [`samples/`](../samples/).
3. Read the [instruction set reference](instruction-set.md).
4. Read the [architecture guide](architecture.md).
5. Use the [development guide](development-guide.md) when modifying the code.
