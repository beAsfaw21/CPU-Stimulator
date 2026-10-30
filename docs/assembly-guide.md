# Assembly Language Guide

This guide introduces the assembly language supported by the 16-bit CPU Simulator.

## How a program is written

An assembly program is a sequence of instructions. Each instruction usually occupies one line:

```asm
mov r0, #7
mov r1, #5
add r0, r1
```

The CPU executes instructions in order unless a jump, branch, call, or return changes the program counter.

## Comments

Comments begin with a semicolon. The assembler ignores everything from the semicolon to the end of the line.

```asm
; This line is a comment.
mov r0, #10 ; This sets R0 to 10.
```

Blank lines and comment-only lines are ignored.

## Registers

Use registers to store values while a program runs. The simulator currently defines:

- `r0` through `r21`
- `r23` through `r31`
- `pc` — program counter
- `sp` — stack pointer

Register names are case-insensitive:

```asm
mov r0, #10
mov R1, r0
```

The current implementation does not define `r22`.

## Immediate values

An immediate value is written directly in the instruction and must begin with `#`:

```asm
mov r0, #25
```

The assembler supports several number formats:

```asm
mov r0, #42        ; decimal
mov r1, #0x2A      ; hexadecimal
mov r2, #0b101010  ; binary
mov r3, #-5        ; negative decimal
```

The `#` prefix distinguishes a value from a register. For example:

```asm
add r0, r1   ; add the value in R1
add r0, #1   ; add the immediate value 1
```

## Labels

Labels give names to instruction addresses. They make programs easier to read than numeric jump addresses.

```asm
start:
    mov r0, #1
    jmp done

done:
    mov r1, #2
```

Labels may also share a line with an instruction:

```asm
done: mov r1, #2
```

Labels are resolved by the assembler before execution. A label-only line does not become an instruction and does not increase the program counter.

## Moving and calculating values

Use `mov` to copy a register or load an immediate value:

```asm
mov r0, #10
mov r1, r0
```

Use `add` and `sub` for arithmetic:

```asm
add r0, r1
add r0, #5
sub r0, r1
sub r0, #1
```

Arithmetic results are limited to 16 bits. Arithmetic updates the ALU flags, which can be used by conditional branches.

## Comparing values

`cmp` compares two registers by calculating `destination - source` for flag purposes. It does not modify either register:

```asm
cmp r0, r1
```

Follow `cmp` with a conditional branch:

```asm
cmp r0, r1
breq equal

mov r2, #0
jmp done

equal:
mov r2, #1

done:
```

## Loops

A loop normally uses a counter, `sub`, and `brne`:

```asm
mov r0, #3

loop:
    sub r0, #1
    brne loop
```

The loop runs three times. `sub` sets `Z` when `r0` reaches zero, and `brne` continues looping while `Z` is clear.

## Memory access

Memory instructions use a register as the address:

```asm
mov r0, #100       ; address
mov r1, #0x1234   ; value
st r0, r1         ; store R1 at address 100
ld r2, r0         ; load the value at address 100 into R2
```

The simulator stores 16-bit words in little-endian order:

```text
memory[100] = 0x34
memory[101] = 0x12
```

## Stack and subroutines

`push` accepts an immediate value and stores it on the stack:

```asm
push #0x1234
pop
```

The current `pop` instruction removes the top stack entry by advancing `sp`; it does not copy the value into a general-purpose register.

Use `call` and `ret` to execute a subroutine:

```asm
call add_two
mov r1, #99
jmp done

add_two:
    mov r0, #1
    add r0, #1
    ret

done:
```

`call` saves the return address on the stack and jumps to the target label. `ret` restores the saved address and continues execution after the call.

## Relative jumps

`rjmp` changes the program counter by a signed immediate offset:

```asm
rjmp #-2
```

Use labels for most loops and branches because labels are easier to maintain when instructions are added or removed.

## A complete example

The following program compares two values, stores the larger value in `r2`, and writes it to memory:

```asm
mov r0, #12
mov r1, #7
cmp r0, r1
brlt use_r1

mov r2, r0
jmp store_result

use_r1:
mov r2, r1

store_result:
mov r3, #100
st r3, r2
```

After execution:

- `r2` contains `12`
- `memory[100]` contains `0x0C`
- `memory[101]` contains `0x00`

## Debugging assembly programs

When a program does not behave as expected:

1. Run it one instruction at a time with **Step**.
2. Check the highlighted source line.
3. Inspect the registers after each instruction.
4. Check the ALU flags after `add`, `sub`, or `cmp`.
5. Confirm that branch targets use the intended label.
6. Use the memory search field to inspect a stored address.

For loops, verify that the counter changes and that the branch condition eventually becomes false.

## Common errors

### Missing `#`

Incorrect:

```asm
mov r0, 10
```

Correct:

```asm
mov r0, #10
```

### Invalid register

Use a register defined by the simulator. `r22` is not currently defined.

### Wrong operand count

`ret` takes no operands:

```asm
ret
```

`mov` requires two operands:

```asm
mov r0, #1
```

### Undefined label

The label must be spelled exactly as it is referenced:

```asm
jmp finish

finish:
```

### Infinite loop

Make sure a loop changes the value used by its branch condition:

```asm
mov r0, #3
loop:
    sub r0, #1
    brne loop
```
