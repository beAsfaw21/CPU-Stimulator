# 16-bit CPU Simulator

<div align="center">

### Build a tiny computer. Watch every instruction come alive.

An approachable Java project that turns CPU architecture into something you can **run, pause, inspect, and understand**.

[![Java](https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk)](https://www.oracle.com/java/technologies/downloads/)
[![Tests](https://img.shields.io/badge/tests-9%20passing-brightgreen)](#run-the-tests)
[![License](https://img.shields.io/badge/license-not%20specified-lightgrey)](#license)

</div>

---

## What is this?

Most computers hide their inner workings behind layers of software. This project removes those layers and gives you a small computer you can explore.

You write a few lines of assembly. The simulator then:

1. **Assembles** your text into instructions.
2. **Fetches** one instruction using the program counter.
3. **Decodes** what that instruction means.
4. **Executes** it using registers, the ALU, memory, or the stack.
5. **Shows the result** in a visual Swing interface.

It is a great starting point for learning assembly language, CPU design, interpreters, debugging, and the fetch-decode-execute cycle.

## See it in action

The GUI gives you a small laboratory for computer architecture:

- Edit or open an assembly program.
- Click **Step** to execute one instruction.
- Click **Run** to execute the program.
- Click **Stop** to halt execution.
- Watch registers and ALU flags change.
- Browse memory as 16-bit little-endian values.
- Search for a memory address in hexadecimal.
- See the current source line highlighted while stepping.

No Maven, Gradle, external libraries, or complex setup is required.

## Quick start: from zero to running CPU

### 1. Install Java

Install **JDK 17 or newer**. Verify that Java is available:

```bash
java -version
javac -version
```

### 2. Clone the project

```bash
git clone https://github.com/beAsfaw21/CPU-Stimulator.git
cd CPU-Stimulator
```

### 3. Compile it

The source files use the `simulator` Java package, so compile them into a package-aware output folder:

```bash
mkdir out
javac -d out *.java
```

### 4. Launch the visual simulator

```bash
java -cp out simulator.GUI
```

When the window opens, create or open a source file, save it, and then choose **Run** or **Step**. The GUI runs saved source files, so use **Save** before executing code typed directly into the editor.

### 5. Run the tests

```bash
java -cp out simulator.TestRunner
```

Expected result:

```text
--------------------------------------------------
Ran 9 tests, failures: 0
```

## Your first program

Try this tiny program. It puts two numbers into registers and adds them:

```asm
; first-program.asm
mov r0, #7
mov r1, #5
add r0, r1
```

After running it, `R0` contains `12`.

### A more interesting example: Fibonacci

This program calculates Fibonacci values in a loop. The final value in `R2` is `89`:

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

Use **Step** repeatedly and observe how the loop, registers, program counter, and flags work together.

## A beginner's mental model

| CPU idea | In this simulator |
| --- | --- |
| Register | A tiny, fast storage location such as `R0` or `R1` |
| ALU | The calculator that performs arithmetic and logic |
| `PC` | The address of the next instruction |
| `SP` | The top of the downward-growing stack |
| Memory | Byte-addressable storage for data |
| Instruction | A single operation the CPU can perform |
| Label | A readable name for an instruction address |
| Flag | A yes/no result used by conditional branches |

### Registers

The CPU currently exposes these general-purpose registers: `R0` through `R21`, and `R23` through `R31`, plus:

- `PC` — program counter
- `SP` — stack pointer, initialized to `0xFFFF`

### ALU flags

Arithmetic and logic operations update four flags:

- `Z` (**zero**) — the result is zero
- `N` (**negative**) — the result's high bit is set
- `C` (**carry**) — an addition carried out or a subtraction borrowed
- `V` (**overflow**) — signed arithmetic overflowed

`cmp` performs a comparison by updating flags without changing either register. Branches such as `breq` and `brne` read those flags.

### Memory and the stack

The address space is 64 KiB:

- `0x0000` to `0xEFFF` — 60 KiB of general RAM
- `0xF000` to `0xFFFF` — 4 KiB stack

Words are stored **little-endian**: the low byte comes first, followed by the high byte.

## Assembly language, explained

Instructions are case-insensitive. Separate operands with commas or whitespace. Comments start with `;`.

Immediate values begin with `#`:

```asm
mov r0, #42        ; decimal
mov r1, #0x2A      ; hexadecimal
mov r2, #0b101010  ; binary
sub r2, #-1        ; negative decimal
```

Labels can stand alone or share a line with an instruction:

```asm
start:
    mov r0, #1
    jmp done

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
| Jumps | `jmp`, `rjmp` | `jmp start`, `rjmp #-2` |
| Conditional branches | `breq`, `brne`, `brlo`, `brsh`, `brmi`, `brpl`, `brge`, `brlt` | `brne loop` |
| Stack | `push`, `pop` | `push #0x1234`, `pop` |
| Subroutines | `call`, `ret` | `call function`, `ret` |

`ld` and `st` transfer a 16-bit value using an address held in a register. `push` accepts an immediate value; `pop` advances the stack pointer.

## How the project is organized

```text
.
├── ALU.java           The calculator: arithmetic, logic, and flags
├── Assembler.java     Two-pass parser and label resolver
├── CPU.java           Fetch-decode-execute loop and stack behavior
├── GUI.java           Swing editor, controls, registers, and memory view
├── Instruction.java   Parsed instruction data structures
├── Memory.java        RAM and stack implementation
├── TestALU.java       ALU tests
├── TestAssembler.java Assembler and label tests
├── TestCPU.java       CPU integration tests
└── TestRunner.java    Lightweight test runner
```

The assembler performs two passes: it first records label addresses, then converts each source line into the executable intermediate representation used by the CPU.

## Run the tests

The repository includes tests for:

- Binary, hexadecimal, decimal, and negative immediates
- Label resolution
- Addition overflow and subtraction borrow
- Register arithmetic
- Little-endian load/store behavior
- Push/pop stack behavior
- Conditional branches
- A complete Fibonacci program

Run them after compiling:

```bash
java -cp out simulator.TestRunner
```

The process exits with status `1` if any test fails, so it can also be used in scripts and continuous integration.

## Troubleshooting

### `Could not find or load main class simulator.GUI`

Compile with `-d out` and run with `-cp out`:

```bash
javac -d out *.java
java -cp out simulator.GUI
```

### The GUI says no file is available

Save the text in the editor first. The **Run** and **Step** controls execute the currently saved file.

### A program runs forever

Use **Stop**, or add a terminating condition. A loop usually needs a counter and a conditional branch:

```asm
mov r0, #3
loop:
    sub r0, #1
    brne loop
```

### An assembly line fails

Check that:

- Registers are written as `r0` through `r31`, `pc`, or `sp`.
- Immediate values start with `#`.
- `ret` has no operand.
- `push` receives an immediate value.
- Labels are spelled consistently.

## Why use this project?

This is more than a calculator with registers. It is a compact playground for understanding the ideas behind real processors:

- How source code becomes executable instructions
- How a program counter controls execution
- How registers and memory exchange data
- How flags make decisions possible
- How a stack supports temporary values and subroutines
- How a debugger reveals machine state

If you are new to computer architecture, start with the first program, click **Step**, and watch `R0`, `PC`, and the flags change. That one experiment explains more than a page of theory.

## Author

**Befekadu Asfaw Tilahun**  
Electrical and Computer Engineering student, AAiT

## License

No license file is currently included in this repository. Add a license before distributing or reusing the project.
