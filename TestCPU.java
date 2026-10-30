package simulator;

public class TestCPU {
    public static void registerTests(TestRunner runner) {
        runner.runTest("CPU.MOV + ADD", () -> {
            runner.resetCPU();
            AsmIR code = Assembler.assemble("mov r1, #10\nmov r2, #5\nadd r1, r2");
            runner.runAsmProgramSafely(code, 100);
            runner.assertEquals(15, CPU.registers[Registers.R1.ordinal()], "r1 should equal 15 after add");
        });

        runner.runTest("CPU.LOAD/STORE little-endian", () -> {
            runner.resetCPU();
            // store value 0x1234 at address 100, then load it into r2
            AsmIR code = Assembler.assemble("mov r0, #100\nmov r1, #0x1234\nst r0, r1\nmov r1, #0\nld r2, r0");
            runner.runAsmProgramSafely(code, 200);

            int low = CPU.memory.read(100) & 0xff;
            int high = CPU.memory.read(101) & 0xff;
            runner.assertEquals(0x34, low, "low byte at memory[100]");
            runner.assertEquals(0x12, high, "high byte at memory[101]");
            runner.assertEquals(0x1234, CPU.registers[Registers.R2.ordinal()] & 0xffff, "loaded value into r2");
        });

        runner.runTest("CPU.PUSH/POP", () -> {
            runner.resetCPU();
            AsmIR code = Assembler.assemble("push #0xF000\n mov r2, sp\nld r1, r2\nmov r0, #0\n st r0, r1\npop");
            runner.runAsmProgramSafely(code, 200);
            
            int low = CPU.memory.read(0) & 0xff;
            int high = CPU.memory.read(1) & 0xff;
            runner.assertEquals(0x00, low, "low byte at memory[0]");
            runner.assertEquals(0xf0, high, "high byte at memory[1]");
            runner.assertEquals(0xf000, CPU.registers[Registers.R1.ordinal()], "loaded value into r1");
            runner.assertEquals(0xffff, CPU.registers[Registers.SP.ordinal()], "SP should equal STACK_TOP(i.e. 65535)");
        });

        runner.runTest("CPU.BREQ/BRNE using CMP", () -> {
            runner.resetCPU();
            AsmIR code = Assembler.assemble("mov r0, #0\nmov r1, #5\ncmp r0, r0\nbreq skip\nmov r1, #1\nskip: mov r2, #2");
            runner.runAsmProgramSafely(code, 100);
            runner.assertEquals(5, CPU.registers[Registers.R1.ordinal()], "r1 should remain 5 (jump taken)");
            runner.assertEquals(2, CPU.registers[Registers.R2.ordinal()], "r2 should be set to 2");
        });

        runner.runTest("Fibonacci", () -> {
            runner.resetCPU();
            AsmIR code = Assembler.assemble("mov r0, #0\nmov r1, #1\nmov r3, #10\n.loop:\nmov r2, r0\n"+
                    "add r2, r1\nmov r0, r1\nmov r1, r2\nsub r3, #1\nbrne .loop");
            
            runner.runAsmProgramSafely(code, 200);
            runner.assertEquals(89, CPU.registers[Registers.R2.ordinal()], "r2 should hold the 11th fibonacci number 89.");
        });

        runner.runTest("CPU.RJMP skips the next instruction", () -> {
            runner.resetCPU();
            AsmIR code = Assembler.assemble(
                    "mov r0, #1\nrjmp #1\nmov r0, #99\nmov r1, #2");
            runner.runAsmProgramSafely(code, 100);
            runner.assertEquals(1, CPU.registers[Registers.R0.ordinal()],
                    "rjmp should skip the following instruction");
            runner.assertEquals(2, CPU.registers[Registers.R1.ordinal()],
                    "execution should continue after the jump target");
        });

        runner.runTest("CPU.JMP and backward RJMP control flow", () -> {
            runner.resetCPU();
            AsmIR absolute = Assembler.assemble(
                    "jmp target\nmov r0, #99\ntarget: mov r0, #7");
            runner.runAsmProgramSafely(absolute, 100);
            runner.assertEquals(7, CPU.registers[Registers.R0.ordinal()],
                    "jmp should transfer control to its absolute target");

            runner.resetCPU();
            AsmIR relative = Assembler.assemble(
                    "mov r0, #1\nrjmp #-1");
            CPU.step(relative);
            CPU.step(relative);
            runner.assertEquals(1, CPU.registers[Registers.PC.ordinal()],
                    "negative rjmp should move backward from the next instruction");
        });

        runner.runTest("CPU logical register and immediate operations", () -> {
            runner.resetCPU();
            AsmIR or = Assembler.assemble("mov r0, #0x00F0\nmov r1, #0x0F0F\nor r0, r1");
            runner.runAsmProgramSafely(or, 100);
            runner.assertEquals(0x0FFF, CPU.registers[Registers.R0.ordinal()],
                    "register OR should use the source register");

            runner.resetCPU();
            AsmIR xor = Assembler.assemble("mov r0, #0x0F0F\nxor r0, #0x00FF");
            runner.runAsmProgramSafely(xor, 100);
            runner.assertEquals(0x0FF0, CPU.registers[Registers.R0.ordinal()],
                    "immediate XOR should use the immediate value");

            runner.resetCPU();
            AsmIR and = Assembler.assemble("mov r0, #0x0FFF\nand r0, #0x00F0");
            runner.runAsmProgramSafely(and, 100);
            runner.assertEquals(0x00F0, CPU.registers[Registers.R0.ordinal()],
                    "immediate AND should use the immediate value");
            runner.assertFalse(CPU.alu.C, "logical operations should clear carry");
            runner.assertFalse(CPU.alu.V, "logical operations should clear overflow");
        });

        runner.runTest("CPU logical immediate operations update zero and negative flags", () -> {
            runner.resetCPU();
            AsmIR code = Assembler.assemble(
                    "mov r0, #0x8000\nand r0, #0x8000\nxor r0, #0x8000\nor r0, #0x8000");
            runner.runAsmProgramSafely(code, 100);
            runner.assertEquals(0x8000, CPU.registers[Registers.R0.ordinal()],
                    "logical immediate result");
            runner.assertFalse(CPU.alu.Z, "non-zero logical result should clear zero");
            runner.assertTrue(CPU.alu.N, "bit 15 should set negative");
        });

        runner.runTest("CPU.CALL and RET preserve the return address", () -> {
            runner.resetCPU();
            AsmIR code = Assembler.assemble(
                    "call function\nmov r0, #7\njmp done\n"
                            + "function: mov r1, #3\nret\n"
                            + "done: mov r2, #9");
            runner.runAsmProgramSafely(code, 100);
            runner.assertEquals(7, CPU.registers[Registers.R0.ordinal()],
                    "execution should resume after call");
            runner.assertEquals(3, CPU.registers[Registers.R1.ordinal()],
                    "function should execute");
            runner.assertEquals(9, CPU.registers[Registers.R2.ordinal()],
                    "execution should reach the instruction after the function");
            runner.assertEquals(0xFFFF, CPU.registers[Registers.SP.ordinal()],
                    "stack pointer should be restored after ret");
        });

        runner.runTest("CPU nested CALL and RET restore stack order", () -> {
            runner.resetCPU();
            AsmIR code = Assembler.assemble(
                    "call outer\nmov r0, #9\njmp done\n"
                            + "outer: call inner\nmov r1, #2\nret\n"
                            + "inner: mov r2, #1\nret\n"
                            + "done:");
            runner.runAsmProgramSafely(code, 100);
            runner.assertEquals(9, CPU.registers[Registers.R0.ordinal()],
                    "outer return should resume at the caller");
            runner.assertEquals(2, CPU.registers[Registers.R1.ordinal()],
                    "outer subroutine should resume after inner return");
            runner.assertEquals(1, CPU.registers[Registers.R2.ordinal()],
                    "inner subroutine should execute");
            runner.assertEquals(0xFFFF, CPU.registers[Registers.SP.ordinal()],
                    "nested returns should restore the original stack pointer");
        });

        runner.runTest("CPU CALL stores a little-endian return address", () -> {
            runner.resetCPU();
            AsmIR code = Assembler.assemble(
                    "call target\nmov r0, #1\n"
                            + "target: mov r1, sp\nret");
            CPU.step(code);
            int stackAddress = CPU.registers[Registers.SP.ordinal()];
            int low = CPU.memory.read(stackAddress) & 0xFF;
            int high = CPU.memory.read(stackAddress + 1) & 0xFF;
            runner.assertEquals(1, low | (high << 8),
                    "call should store the address of the next instruction");
            runner.assertEquals(0xFFFD, stackAddress,
                    "call should reserve one 16-bit stack entry");
        });

        runner.runTest("CPU.BRLO branches after a borrow", () -> {
            runner.resetCPU();
            AsmIR code = Assembler.assemble(
                    "mov r0, #1\nmov r1, #2\ncmp r0, r1\n"
                            + "brlo borrowed\nmov r2, #0\njmp done\n"
                            + "borrowed: mov r2, #1\ndone:");
            runner.runAsmProgramSafely(code, 100);
            runner.assertEquals(1, CPU.registers[Registers.R2.ordinal()],
                    "brlo should branch when comparison borrows");
        });

        runner.runTest("CPU.BRSH branches when there is no borrow", () -> {
            runner.resetCPU();
            AsmIR code = Assembler.assemble(
                    "mov r0, #2\nmov r1, #1\ncmp r0, r1\n"
                            + "brsh no_borrow\nmov r2, #0\njmp done\n"
                            + "no_borrow: mov r2, #1\ndone:");
            runner.runAsmProgramSafely(code, 100);
            runner.assertEquals(1, CPU.registers[Registers.R2.ordinal()],
                    "brsh should branch when comparison does not borrow");
        });

        runner.runTest("CPU.BRMI and BRPL use the sign flag", () -> {
            runner.resetCPU();
            AsmIR negative = Assembler.assemble(
                    "mov r0, #0\nsub r0, #1\nbrmi negative\n"
                            + "mov r1, #0\njmp done\nnegative: mov r1, #1\ndone:");
            runner.runAsmProgramSafely(negative, 100);
            runner.assertEquals(1, CPU.registers[Registers.R1.ordinal()],
                    "brmi should branch for a negative result");

            runner.resetCPU();
            AsmIR positive = Assembler.assemble(
                    "mov r0, #1\nsub r0, #0\nbrpl positive\n"
                            + "mov r1, #0\njmp done\npositive: mov r1, #1\ndone:");
            runner.runAsmProgramSafely(positive, 100);
            runner.assertEquals(1, CPU.registers[Registers.R1.ordinal()],
                    "brpl should branch for a non-negative result");
        });

        runner.runTest("CPU.BRGE and BRLT use signed comparison flags", () -> {
            runner.resetCPU();
            AsmIR greaterOrEqual = Assembler.assemble(
                    "mov r0, #5\nmov r1, #5\ncmp r0, r1\n"
                            + "brge greater_or_equal\nmov r2, #0\njmp done\n"
                            + "greater_or_equal: mov r2, #1\ndone:");
            runner.runAsmProgramSafely(greaterOrEqual, 100);
            runner.assertEquals(1, CPU.registers[Registers.R2.ordinal()],
                    "brge should branch when signed comparison is greater or equal");

            runner.resetCPU();
            AsmIR lessThan = Assembler.assemble(
                    "mov r0, #4\nmov r1, #5\ncmp r0, r1\n"
                            + "brlt less_than\nmov r2, #0\njmp done\n"
                            + "less_than: mov r2, #1\ndone:");
            runner.runAsmProgramSafely(lessThan, 100);
            runner.assertEquals(1, CPU.registers[Registers.R2.ordinal()],
                    "brlt should branch when signed comparison is less than");
        });

        runner.runTest("CPU detects stack overflow", () -> {
            runner.resetCPU();
            CPU.registers[Registers.SP.ordinal()] = 0xF000;
            try {
                CPU.push_stack(1);
                runner.fail("push below stack boundary should fail");
            } catch (RuntimeException e) {
                runner.assertEqualsExact(0xF000, CPU.registers[Registers.SP.ordinal()],
                        "stack pointer should remain unchanged after overflow");
                runner.assertTrue(e.getMessage().contains("STACK OVERFLOW"),
                        "error should identify stack overflow");
            }
        });

        runner.runTest("CPU rejects POP and RET stack underflow", () -> {
            runner.resetCPU();
            AsmIR pop = Assembler.assemble("pop");
            try {
                CPU.step(pop);
                runner.fail("pop on an empty stack should fail");
            } catch (RuntimeException e) {
                runner.assertTrue(e.getMessage().contains("STACK UNDERFLOW"),
                        "pop error should identify stack underflow");
                runner.assertEqualsExact(0xFFFF, CPU.registers[Registers.SP.ordinal()],
                        "pop underflow should not change SP");
            }

            runner.resetCPU();
            AsmIR ret = Assembler.assemble("ret");
            try {
                CPU.step(ret);
                runner.fail("ret on an empty stack should fail");
            } catch (RuntimeException e) {
                runner.assertTrue(e.getMessage().contains("STACK UNDERFLOW"),
                        "ret error should identify stack underflow");
                runner.assertEqualsExact(0xFFFF, CPU.registers[Registers.SP.ordinal()],
                        "ret underflow should not change SP");
            }
        });

        runner.runTest("Memory rejects invalid addresses", () -> {
            Memory memory = new Memory();
            memory.write(0x0000, (byte) 0x12);
            memory.write(0xFFFF, (byte) 0x34);
            runner.assertEqualsExact(0x12, memory.read(0x0000) & 0xFF,
                    "lowest memory address should be valid");
            runner.assertEqualsExact(0x34, memory.read(0xFFFF) & 0xFF,
                    "highest memory address should be valid");

            try {
                memory.read(-1);
                runner.fail("negative read address should be rejected");
            } catch (IllegalArgumentException e) {
                runner.assertTrue(e.getMessage().contains("out of range"),
                        "negative address error should be descriptive");
            }

            try {
                memory.write(0x10000, (byte) 0);
                runner.fail("address above 16-bit range should be rejected");
            } catch (IllegalArgumentException e) {
                runner.assertTrue(e.getMessage().contains("out of range"),
                        "high address error should be descriptive");
            }
        });

        runner.runTest("CPU rejects 16-bit word access across memory boundary", () -> {
            runner.resetCPU();
            AsmIR code = Assembler.assemble(
                    "mov r0, #0xFFFF\nmov r1, #0x1234\nst r0, r1");
            try {
                runner.runAsmProgramSafely(code, 100);
                runner.fail("word store past the address space should be rejected");
            } catch (IllegalArgumentException e) {
                runner.assertTrue(e.getMessage().contains("out of range"),
                        "boundary error should be descriptive");
            }
        });
    }
}
