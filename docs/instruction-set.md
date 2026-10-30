# Instruction Set Reference

This document defines the assembly instructions supported by the 16-bit CPU Simulator.

## Syntax conventions

- Mnemonics are case-insensitive.
- Operands may be separated by commas or whitespace.
- Comments begin with `;`.
- Immediate values begin with `#`.
- Labels identify instruction addresses.
- Registers are written as `r0` through `r21`, `r23` through `r31`, `pc`, or `sp`.

Examples:

```asm
mov r0, #10
add r0, r1
brne loop
```

## Immediate values

The assembler accepts decimal, hexadecimal, binary, and negative decimal values:

```asm
mov r0, #42
mov r1, #0x2A
mov r2, #0b101010
sub r2, #-1
```

## Operand notation

| Notation | Meaning |
| --- | --- |
| `Rd` | Destination register |
| `Rs` | Source register |
| `Ra` | Register containing a memory address |
| `#value` | Immediate numeric value |
| `label` | Symbolic instruction address |

## Data movement

### `mov`

Copies a register or immediate value into a destination register.

```asm
mov Rd, Rs
mov Rd, #value
```

Examples:

```asm
mov r0, r1
mov r0, #123
```

The immediate form stores the lower 16 bits of the value. `mov` does not update ALU flags.

## Memory instructions

### `ld`

Loads a 16-bit little-endian value from memory into a register.

```asm
ld Rd, Ra
```

`Ra` contains the starting memory address. The low byte is read from `Ra`; the high byte is read from `Ra + 1`.

Example:

```asm
mov r0, #100
ld r1, r0
```

### `st`

Stores a 16-bit register value into memory in little-endian order.

```asm
st Ra, Rs
```

Example:

```asm
mov r0, #100
mov r1, #0x1234
st r0, r1
```

After this instruction:

```text
memory[100] = 0x34
memory[101] = 0x12
```

`ld` and `st` do not update ALU flags.

## Arithmetic instructions

### `add`

Adds a register or immediate value to the destination register.

```asm
add Rd, Rs
add Rd, #value
```

The result is limited to 16 bits and updates `Z`, `N`, `C`, and `V`.

### `sub`

Subtracts a register or immediate value from the destination register.

```asm
sub Rd, Rs
sub Rd, #value
```

The result is limited to 16 bits and updates `Z`, `N`, `C`, and `V`. The `C` flag indicates a borrow.

## Logical instructions

### `or`

Performs a bitwise OR:

```asm
or Rd, Rs
or Rd, #value
```

### `and`

Performs a bitwise AND:

```asm
and Rd, Rs
and Rd, #value
```

### `xor`

Performs a bitwise exclusive OR:

```asm
xor Rd, Rs
xor Rd, #value
```

The logical instructions update `Z` and `N`. They clear `C` and `V`.

## Comparison and flags

### `cmp`

Compares two registers by calculating `Rd - Rs` for flag purposes. It does not modify either register.

```asm
cmp Rd, Rs
```

The result updates `Z`, `N`, `C`, and `V`, allowing a conditional branch to follow.

Example:

```asm
cmp r0, r1
brne not_equal
```

### Status flags

| Flag | Set when |
| --- | --- |
| `Z` | The 16-bit result is zero |
| `N` | Bit 15 of the result is set |
| `C` | Addition carries or subtraction borrows |
| `V` | Signed arithmetic overflows |

## Control flow

### `jmp`

Jumps to an absolute instruction address or label:

```asm
jmp label
jmp #address
```

### `rjmp`

Adds a signed relative offset to the current program counter:

```asm
rjmp #offset
```

Use labels for ordinary absolute control flow and `rjmp` when a relative offset is required.

### Conditional branches

Conditional branches jump to an absolute instruction address or label when their condition is true:

```asm
breq label
brne label
brlo label
brsh label
brmi label
brpl label
brge label
brlt label
```

| Instruction | Branches when |
| --- | --- |
| `breq` | `Z` is set |
| `brne` | `Z` is clear |
| `brlo` | `C` is set |
| `brsh` | `C` is clear |
| `brmi` | `N` is set |
| `brpl` | `N` is clear |
| `brge` | `N` equals `V` |
| `brlt` | `N` differs from `V` |

Example:

```asm
mov r0, #3

loop:
    sub r0, #1
    brne loop
```

## Stack instructions

### `push`

Pushes a 16-bit immediate value onto the stack:

```asm
push #value
```

The stack pointer decreases by two bytes before the value is stored.

### `pop`

Removes the top 16-bit stack entry:

```asm
pop
```

The stack pointer increases by two bytes. The current implementation does not copy the popped value into a general-purpose register. Popping an empty stack raises a stack-underflow error.

## Subroutines

### `call`

Pushes the address of the next instruction onto the stack and jumps to a subroutine:

```asm
call label
call #address
```

### `ret`

Restores the return address from the stack:

```asm
ret
```

`ret` reads the little-endian 16-bit address at the current `SP`, advances
`SP` by two bytes, and resumes at that address. Returning from an empty stack
raises a stack-underflow error.

Example:

```asm
call double
mov r1, #7
jmp done

double:
    mov r0, #21
    add r0, r0
    ret

done:
```

## Instruction summary

| Instruction | Operands | Updates flags |
| --- | --- | --- |
| `mov` | Register, register or immediate | No |
| `ld` | Destination register, address register | No |
| `st` | Address register, source register | No |
| `add` | Destination register, register or immediate | `Z`, `N`, `C`, `V` |
| `sub` | Destination register, register or immediate | `Z`, `N`, `C`, `V` |
| `or` | Destination register, register or immediate | `Z`, `N`; clears `C`, `V` |
| `and` | Destination register, register or immediate | `Z`, `N`; clears `C`, `V` |
| `xor` | Destination register, register or immediate | `Z`, `N`; clears `C`, `V` |
| `cmp` | Two registers | `Z`, `N`, `C`, `V` |
| `jmp` | Label or immediate address | No |
| `rjmp` | Immediate offset | No |
| Conditional branch | Label or immediate address | No |
| `push` | Immediate value | No |
| `pop` | None | No |
| `call` | Label or immediate address | No |
| `ret` | None | No |
