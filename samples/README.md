# Sample Programs

These assembly programs are small examples you can open in the simulator GUI and run one instruction at a time.

## Running a sample

Compile and start the simulator from the repository root:

```bash
javac -d out *.java
java -cp out simulator.GUI
```

In the GUI:

1. Select **Open**.
2. Choose a file from this directory.
3. Select **Save** if you edit the program.
4. Select **Step** to execute one instruction at a time, or **Run** to execute the program.
5. Adjust **Max steps** when testing loops or programs that may not halt.

If GNU Make is available, you can start the GUI with:

```bash
make run
```

## Examples

| File | Expected result | Concepts |
| --- | --- | --- |
| [`addition.asm`](addition.asm) | `R0 = 12` | Registers and addition |
| [`branching.asm`](branching.asm) | `R2 = 1` | Comparison and conditional branching |
| [`fibonacci.asm`](fibonacci.asm) | `R2 = 89` | Labels, loops, and `brne` |
| [`memory.asm`](memory.asm) | `R2 = 0x1234` | Memory access and little-endian words |
| [`subroutine.asm`](subroutine.asm) | `R0 = 42`, `R1 = 7` | `call`, `ret`, and the stack |

## Learning order

If you are new to assembly, try the examples in this order:

1. `addition.asm`
2. `branching.asm`
3. `fibonacci.asm`
4. `memory.asm`
5. `subroutine.asm`

For explanations of the syntax, see the [assembly language guide](../docs/assembly-guide.md). For a complete list of instructions, see the [instruction set reference](../docs/instruction-set.md).
