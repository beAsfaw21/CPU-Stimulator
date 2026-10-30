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

        runner.runTest("CPU detects stack overflow", () -> {
            runner.resetCPU();
            CPU.registers[Registers.SP.ordinal()] = 0xF000;
            try {
                CPU.push_stack(1);
                runner.fail("push below stack boundary should fail");
            } catch (RuntimeException e) {
                runner.assertEqualsExact(0xEFFE, CPU.registers[Registers.SP.ordinal()],
                        "stack pointer should move before overflow is detected");
                runner.assertTrue(e.getMessage().contains("STACK OVERFLOW"),
                        "error should identify stack overflow");
            }
        });
    }
}
