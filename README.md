# 16-bit CPU Simulator

A Java-based educational simulator for exploring CPU architecture, assembly language, and the fetch-decode-execute cycle.

The project provides a custom assembler, a 16-bit CPU model, an arithmetic logic unit (ALU), simulated memory, a stack, a Swing GUI, and a lightweight test suite.

## Table of contents

- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
- [Assembly language](#assembly-language)
- [CPU architecture](#cpu-architecture)
- [Project structure](#project-structure)
- [Testing](#testing)
- [Changelog](#changelog)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [Author](#author)
- [License](#license)

## Features

- 16-bit registers and arithmetic
- Custom two-pass assembler with label resolution
- Decimal, hexadecimal, binary, and negative immediate values
- General-purpose registers, program counter, and stack pointer
- Arithmetic operations: addition and subtraction
- Logical operations: OR, AND, and XOR
- Comparison and conditional branching
- 60 KiB of general-purpose memory and a 4 KiB stack
- Little-endian 16-bit load and store operations
- Stack operations and subroutine calls
- Swing GUI with:
  - Assembly source editor
  - Open and save controls
  - Run, step, and stop controls
  - Current source-line highlighting
  - Register display
  - Memory viewer and hexadecimal address search
- Tests for the assembler, ALU, CPU, memory, stack, branches, and Fibonacci execution

## Requirements

- Java Development Kit (JDK) 17 or newer
- A graphical desktop environment for the Swing interface

The project has no external dependencies and does not require Maven or Gradle.

## Installation

Clone the repository:

```bash
git clone https://github.com/beAsfaw21/CPU-Stimulator.git
cd CPU-Stimulator
```

Compile the source files into an output directory:

```bash
mkdir out
javac -d out *.java
```

The `-d out` option is required because the Java source files belong to the `simulator` package.

## Usage

### Start the graphical interface

```bash
java -cp out simulator.GUI
```

The GUI starts with an empty editor. You can either open an existing assembly file or type a program directly into the editor.

To execute a program:

1. Type or open assembly source.
2. Select **Save** if you edited the source in the application.
3. Select **Step** to execute one instruction.
4. Select **Run** to execute the program continuously.
5. Select **Stop** to halt execution.
6. Inspect the registers and memory after execution.

The current source line is highlighted while stepping. The memory search field accepts hexadecimal addresses without the `0x` prefix.

### Run a small program

The following program adds `7` and `5`:

```asm
mov r0, #7
mov r1, #5
add r0, r1
```

After execution, register `R0` contains `12`.

### Run a loop

This example calculates a Fibonacci value:

```asm
mov r0, #0       ; previous value
mov r1, #1       ; current value
mov r3, #10      ; loop counter

.loop:
    mov r2, r0
    add r2, r1
    mov r0, r1
    mov r1, r2
    sub r3, #1
    brne .loop
```

After execution, `R2` contains `89`.

### Try the sample programs

The [`samples/`](samples/) directory contains ready-to-run assembly programs:

| File | Demonstrates |
| --- | --- |
| [`addition.asm`](samples/addition.asm) | Register values and arithmetic |
| [`branching.asm`](samples/branching.asm) | Comparisons and conditional branches |
| [`fibonacci.asm`](samples/fibonacci.asm) | Labels, loops, and `brne` |
| [`memory.asm`](samples/memory.asm) | 16-bit little-endian memory access |
| [`subroutine.asm`](samples/subroutine.asm) | `call`, `ret`, and the stack |

Open any sample in the GUI, save it if necessary, and select **Run** or **Step**.

## Assembly language

Assembly instructions are case-insensitive. Operands can be separated with commas or whitespace. Comments begin with a semicolon (`;`).

### Immediate values

Immediate values must begin with `#`:

```asm
mov r0, #42        ; decimal
mov r1, #0x2A      ; hexadecimal
mov r2, #0b101010  ; binary
sub r2, #-1        ; negative decimal
```

### Labels

Labels identify instruction addresses and can be used by jump and branch instructions:

```asm
start:
    mov r0, #1
    jmp done

done:
    mov r1, #2
```

Labels may also appear before an instruction on the same line:

```asm
done: mov r1, #2
```

### Instruction reference

| Category | Instructions | Examples |
| --- | --- | --- |
| Data movement | `mov` | `mov r0, r1`, `mov r0, #10` |
| Memory | `ld`, `st` | `ld r1, r0`, `st r0, r1` |
| Arithmetic | `add`, `sub` | `add r0, r1`, `sub r0, #1` |
| Logic | `or`, `and`, `xor` | `xor r0, r1`, `and r0, #0xFF` |
| Comparison | `cmp` | `cmp r0, r1` |
| Unconditional control flow | `jmp`, `rjmp` | `jmp start`, `rjmp #-2` |
| Conditional control flow | `breq`, `brne`, `brlo`, `brsh`, `brmi`, `brpl`, `brge`, `brlt` | `brne loop` |
| Stack | `push`, `pop` | `push #0x1234`, `pop` |
| Subroutines | `call`, `ret` | `call function`, `ret` |

`cmp` updates the ALU flags without modifying either operand. Conditional branches use those flags.

`ld` loads a 16-bit value from the address in a register. `st` stores a 16-bit value at the address in a register. Both instructions use little-endian byte order.

## CPU architecture

### Components

| Component | Description |
| --- | --- |
| CPU | Fetches, decodes, and executes assembled instructions |
| Assembler | Converts assembly source into an executable intermediate representation |
| ALU | Performs arithmetic and logical operations and updates status flags |
| Memory | Stores bytes in general RAM and the stack region |
| Registers | Store operands, results, the program counter, and the stack pointer |
| GUI | Displays source code, execution state, registers, and memory |

### Registers

The CPU exposes these general-purpose registers:

- `R0` through `R21`
- `R23` through `R31`
- `PC` — program counter
- `SP` — stack pointer

The current implementation does not define `R22`.

`SP` starts at `0xFFFF` and the stack grows downward. `PC` identifies the next instruction to execute.

### ALU flags

The ALU maintains four flags:

- `Z` — set when the result is zero
- `N` — set when the result has its sign bit set
- `C` — set when an addition produces a carry or a subtraction produces a borrow
- `V` — set when signed arithmetic overflows

### Memory map

The simulator provides a 64 KiB address space:

| Address range | Purpose |
| --- | --- |
| `0x0000`–`0xEFFF` | 60 KiB of general-purpose RAM |
| `0xF000`–`0xFFFF` | 4 KiB stack |

16-bit words are stored in little-endian order: the lower byte is stored at the lower address.

## Project structure

```text
.
├── ALU.java            Arithmetic, logic, and status flags
├── Assembler.java      Two-pass parser and label resolver
├── CPU.java            Instruction execution and stack behavior
├── GUI.java            Swing interface and execution controls
├── Instruction.java    Instruction and assembler data structures
├── Memory.java         RAM and stack implementation
├── TestALU.java        ALU tests
├── TestAssembler.java  Assembler tests
├── TestCPU.java        CPU integration tests
├── TestRunner.java     Test runner and assertions
└── samples/            Example assembly programs
```

## Testing

Run the complete test suite:

```bash
java -cp out simulator.TestRunner
```

The test suite covers:

- Immediate values in binary, hexadecimal, decimal, and negative form
- Label definition and resolution
- Addition overflow
- Subtraction borrow
- Register arithmetic
- Little-endian load and store behavior
- Push and pop operations
- Conditional branches
- Relative jumps and subroutine calls
- Stack overflow detection
- Fibonacci execution

The process exits with status `1` when one or more tests fail.

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for the release history and upcoming changes.

## Troubleshooting

### `Could not find or load main class simulator.GUI`

Recompile using the package-aware output directory:

```bash
javac -d out *.java
java -cp out simulator.GUI
```

### The GUI does not run my edited program

Save the source using the **Save** button before selecting **Run** or **Step**. The GUI executes the currently saved file.

### A program does not stop

Use the **Stop** button or add a terminating branch condition. For example:

```asm
mov r0, #3

loop:
    sub r0, #1
    brne loop
```

### An assembly instruction is rejected

Check the following:

- Use registers defined by the simulator.
- Prefix immediate values with `#`.
- Use `ret` without operands.
- Use an immediate value with `push`.
- Check label spelling and capitalization.
- Ensure each instruction has the required number of operands.

## Contributing

Contributions are welcome.

1. Fork the repository.
2. Create a feature branch:

   ```bash
   git checkout -b feature/your-feature
   ```

3. Make focused changes.
4. Compile the project and run the test suite.
5. Update the README when behavior or usage changes.
6. Open a pull request with a clear description of the change.

Please keep changes focused and add tests for new assembler or CPU behavior where appropriate.

## Author

Befekadu Asfaw Tilahun
Electrical and Computer Engineering student, AAiT

## License

This project is licensed under the [MIT License](LICENSE).

You are free to use, copy, modify, distribute, and sublicense the project, provided that the original copyright and license notices are included.
