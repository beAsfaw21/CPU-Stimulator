# CPU Architecture

This document describes the internal architecture of the 16-bit CPU Simulator.

## Overview

The simulator models a small 16-bit processor with:

- A custom assembly language
- A two-pass assembler
- 16-bit general-purpose registers
- An arithmetic logic unit (ALU)
- A program counter (`PC`)
- A stack pointer (`SP`)
- 64 KiB of byte-addressable memory
- A 4 KiB downward-growing stack
- A Swing interface for source-level inspection and execution

The main execution flow is:

```text
Assembly source
      |
      v
   Assembler
      |
      v
     AsmIR
      |
      v
      CPU
  fetch -> decode -> execute
```

## Source-to-execution pipeline

### 1. Assembly source

The user writes one instruction per line. The assembler accepts comments, labels, registers, and immediate values.

```asm
mov r0, #3
loop:
    sub r0, #1
    brne loop
```

### 2. First assembler pass

The first pass scans the source and records the program-counter address of each label. Labels do not occupy instruction slots.

For the example above:

```text
loop -> 1
```

### 3. Second assembler pass

The second pass converts each instruction into an `Instruction` object. Symbolic branch targets are replaced with the corresponding numeric program-counter address.

The result is stored in `AsmIR`, which contains:

- `program` — ordered instructions
- `lines` — original source-line numbers
- `labels_to_pc` — resolved label addresses

The source-line mapping is used by the GUI to highlight the instruction currently being executed.

## CPU execution cycle

The CPU executes one instruction at a time through `CPU.step`.

### Fetch

The CPU reads the instruction at the address stored in `PC` and increments `PC` so it points to the next instruction.

### Decode

The instruction opcode determines which operation to perform and how its operands are interpreted.

### Execute

The CPU performs the operation. Depending on the instruction, it may:

- Read or write registers
- Call the ALU
- Read or write memory
- Update status flags
- Change the program counter
- Push or pop stack data

Execution stops when `PC` reaches the end of the assembled program or when the CPU is explicitly halted.

## Registers

The CPU currently exposes:

- `R0` through `R21`
- `R23` through `R31`
- `PC` — program counter
- `SP` — stack pointer

The current register definition does not include `R22`.

All register values are treated as 16-bit values. Results are masked to the lower 16 bits where appropriate, so arithmetic wraps around at `0xFFFF`.

### Program counter

`PC` contains the index of the next instruction in the assembled program.

For example, after executing instruction `0`, the CPU normally continues with instruction `1`. Jump, branch, call, and return instructions can replace the normal sequential value.

### Stack pointer

`SP` starts at `0xFFFF`. The stack grows downward:

```text
push -> SP = SP - 2
pop  -> SP = SP + 2
```

Each stack entry is one 16-bit value stored as two bytes.

## ALU and flags

The ALU performs arithmetic and bitwise operations. Operations update four flags:

| Flag | Meaning |
| --- | --- |
| `Z` | The 16-bit result is zero |
| `N` | The result's high bit (`0x8000`) is set |
| `C` | Addition produced a carry or subtraction produced a borrow |
| `V` | Signed arithmetic overflow occurred |

`cmp` performs subtraction for flag calculation but does not store the subtraction result.

Conditional branches use these flags:

| Instruction | Condition |
| --- | --- |
| `breq` | `Z` is set |
| `brne` | `Z` is clear |
| `brlo` | `C` is set |
| `brsh` | `C` is clear |
| `brmi` | `N` is set |
| `brpl` | `N` is clear |
| `brge` | `N` equals `V` |
| `brlt` | `N` differs from `V` |

## Memory map

The memory model contains 64 KiB of addressable space:

| Address range | Size | Purpose |
| --- | ---: | --- |
| `0x0000`–`0xEFFF` | 60 KiB | General-purpose RAM |
| `0xF000`–`0xFFFF` | 4 KiB | Stack |

Memory is byte-addressable. A 16-bit word occupies two consecutive addresses.

### Little-endian words

For the value `0x1234`:

```text
address 100 -> 0x34  (low byte)
address 101 -> 0x12  (high byte)
```

The `ld` and `st` instructions use this little-endian representation.

## Stack and subroutines

`push` writes a value to the stack and decreases `SP`. `pop` advances `SP` by two bytes.

`call`:

1. Saves the address of the next instruction on the stack.
2. Transfers control to the target address.

`ret`:

1. Reads the saved return address from the stack.
2. Restores it to `PC`.
3. Advances `SP`.

The CPU raises a stack-overflow error when a push would move below the stack region.

## Component responsibilities

| Class | Responsibility |
| --- | --- |
| `Assembler` | Parses assembly source and resolves labels |
| `AsmIR` | Stores assembled instructions and source mappings |
| `Instruction` | Represents one parsed instruction |
| `CPU` | Executes instructions and manages registers and stack |
| `ALU` | Performs arithmetic and logical operations |
| `Memory` | Provides RAM and stack byte access |
| `GUI` | Provides editing, stepping, execution, and state inspection |
| `TestRunner` | Runs the project test classes |

## Debugging with the GUI

The GUI exposes the execution state without requiring a traditional debugger:

1. Open or edit an assembly file.
2. Save the file.
3. Select **Step** to execute one instruction.
4. Observe the highlighted source line.
5. Inspect registers and memory.
6. Use **Run** to continue execution or **Stop** to halt it.

This makes it possible to connect each assembly instruction to its effect on CPU state.
