# 16-bit CPU Simulator

A small, educational 16-bit CPU implemented in Java. The project includes a custom assembly language, a two-pass assembler, an executable CPU model, an ALU with processor flags, simulated memory, and a Swing-based debugger-style interface.

The simulator is designed to make the fetch-decode-execute cycle visible and easy to experiment with. Assembly programs can be edited, assembled, stepped through, and inspected while register and memory state changes.

## Features

- **16-bit execution** with 16-bit registers and arithmetic results.
- **Custom assembler** with labels, comments, decimal/hexadecimal/binary immediates, and case-insensitive mnemonics.
- **ALU operations** for addition, subtraction, comparison, bitwise OR/AND/XOR, and status flags.
- **Control flow** with absolute jumps, relative jumps, conditional branches, subroutine calls, and returns.
- **Memory model** with 60 KiB of general RAM and a 4 KiB stack region.
- **Little-endian word access** for `ld` and `st` instructions.
- **Interactive Swing GUI** for opening assembly files, running or stepping programs, viewing registers, and browsing memory.
- **Built-in tests** covering the assembler, ALU, CPU execution, memory access, stack operations, branching, and a Fibonacci program.

## Architecture

The simulator is split into focused components:

| Component | Responsibility |
| --- | --- |
| `CPU` | Fetches instructions, updates the program counter, executes operations, and manages the stack |
| `Assembler` | Converts assembly source into an intermediate representation in two passes |
| `ALU` | Performs arithmetic and logical operations and updates `Z`, `N`, `C`, and `V` flags |
| `Memory` | Provides byte-addressable RAM and stack storage |
| `Instruction` / `AsmIR` | Represent parsed instructions, source-line mappings, and resolved labels |
| `GUI` | Provides source editing, execution controls, register display, and memory inspection |
| `TestRunner` | Runs the project’s lightweight test suite without an external test framework |

### Registers and flags

The CPU exposes general-purpose registers (`R0` through `R31`), a program counter (`PC`), and a stack pointer (`SP`). The stack pointer starts at `0xFFFF` and grows downward.

ALU operations update these flags:

- `Z` — result is zero
- `N` — result has its sign bit set
- `C` — carry or borrow occurred
- `V` — signed arithmetic overflow occurred

Memory addresses from `0x0000` through `0xEFFF` are general RAM. Addresses from `0xF000` through `0xFFFF` are reserved for the stack.

## Assembly language

Assembly is one instruction per line. Commas and whitespace may be used as separators, and comments begin with `;`.

Immediate values must start with `#`:

```asm
mov r0, #10       ; decimal
mov r1, #0x1234   ; hexadecimal
mov r2, #0b1010   ; binary
sub r2, #-1       ; signed decimal
```

Labels can be placed on their own line or before an instruction:

```asm
mov r0, #0
mov r1, #1
mov r3, #10

.loop:
    mov r2, r0
    add r2, r1
    mov r0, r1
    mov r1, r2
    sub r3, #1
    brne .loop
```

### Instruction reference

| Category | Instructions | Operand form |
| --- | --- | --- |
| Data movement | `mov` | `mov register, register` or `mov register, #value` |
| Memory | `ld`, `st` | `ld destination, address-register`; `st address-register, source` |
| Arithmetic | `add`, `sub` | `operation destination, register` or `operation destination, #value` |
| Logic | `or`, `and`, `xor` | `operation destination, register` or `operation destination, #value` |
| Comparison | `cmp` | `cmp register, register` |
| Unconditional control flow | `jmp`, `rjmp` | `operation label` or `operation #value` |
| Conditional control flow | `breq`, `brne`, `brlo`, `brsh`, `brmi`, `brpl`, `brge`, `brlt` | `operation label` or `operation #value` |
| Stack | `push`, `pop` | `push #value`; `pop` |
| Subroutines | `call`, `ret` | `call label` or `call #value`; `ret` |

`cmp` updates the ALU flags without storing a result. Conditional branches use those flags. `ld` and `st` transfer 16-bit values as two adjacent bytes in little-endian order.

## Requirements

- Java Development Kit (JDK) **17 or newer**
- A graphical desktop environment to run the Swing interface

No external libraries or build tools are required.

## Getting started

Clone the repository:

```bash
git clone https://github.com/beAsfaw21/CPU-Stimulator.git
cd CPU-Stimulator
```

Compile the sources into a separate package-aware output directory:

```bash
mkdir out
javac -d out *.java
```

### Run the graphical simulator

```bash
java -cp out simulator.GUI
```

In the GUI:

1. Open an assembly source file, or edit the text area directly.
2. Use **Run** to execute the assembled program.
3. Use **Step** to execute one instruction at a time.
4. Inspect the register panel and memory grid after each step.

The sample source format is plain text; files do not need a special extension.

### Run the test suite

```bash
java -cp out simulator.TestRunner
```

The runner exits with status `1` when a test fails, which makes it suitable for use in scripts or CI:

```text
--------------------------------------------------
Ran 9 tests, failures: 0
```

## Project layout

```text
.
├── ALU.java          Arithmetic, logic, and status flags
├── Assembler.java    Two-pass assembly parser and label resolver
├── CPU.java          Instruction execution and stack behavior
├── GUI.java          Swing editor, controls, registers, and memory view
├── Instruction.java  Instruction and assembler IR data structures
├── Memory.java       RAM and stack implementation
├── TestALU.java      ALU tests
├── TestAssembler.java Assembler tests
├── TestCPU.java      CPU integration tests
└── TestRunner.java   Test entry point and assertions
```

Compiled `.class` files are generated artifacts. Prefer compiling to `out/` as shown above rather than placing generated files beside the source files.

## Learning goals

This project is useful for exploring:

- How an assembler resolves labels in multiple passes
- How a program counter drives instruction execution
- How registers, memory, and a stack interact
- How ALU flags enable conditional branching
- How a debugger can visualize machine state

## Author

**Befekadu Asfaw Tilahun**  
Electrical and Computer Engineering student, AAiT

## License

No license file is currently included in the repository. Add a license before distributing or reusing the project.
