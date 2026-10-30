package simulator;

public class TestAssembler {
    public static void registerTests(TestRunner runner) {
        runner.runTest("Assembler.immediates (bin/hex/dec/neg)", () -> {
            AsmIR a1 = Assembler.assemble("mov r1, #0b1010");
            runner.assertEquals(10, a1.program.get(0).immediate, "binary immediate");

            AsmIR a2 = Assembler.assemble("mov r1, #0xFF");
            runner.assertEquals(255, a2.program.get(0).immediate, "hex immediate");

            AsmIR a3 = Assembler.assemble("mov r1, #-10");
            // immediate may be stored as signed negative
            runner.assertEqualsExact(-10, a3.program.get(0).immediate, "negative immediate");
        });

        runner.runTest("Assembler.labels (define and resolve)", () -> {
            AsmIR ir = Assembler.assemble("mov r0, #1\njmp skip\nskip:");
            Integer pc = ir.labels_to_pc.get("skip");
            if (pc == null) runner.fail("label 'skip' not found");
            runner.assertEqualsExact(2, pc.intValue(), "label 'skip' should point to PC 2");
        });

        runner.runTest("Assembler.comments and labels on instruction lines", () -> {
            AsmIR ir = Assembler.assemble(
                    "; setup\nstart: mov r0, #1 ; initialize register\n"
                            + "jmp start");
            runner.assertEqualsExact(2, ir.program.size(), "instruction count");
            runner.assertEqualsExact(0, ir.labels_to_pc.get("start"), "start label");
        });

        runner.runTest("Assembler rejects invalid instructions and operands", () -> {
            try {
                Assembler.assemble("move r0, #1");
                runner.fail("invalid instruction should be rejected");
            } catch (IllegalArgumentException e) {
                runner.assertTrue(e.getMessage().contains("line 1"),
                        "error should include source line");
            }

            try {
                Assembler.assemble("mov r0");
                runner.fail("missing operand should be rejected");
            } catch (IllegalArgumentException e) {
                runner.assertTrue(e.getMessage().contains("requires 2 operands"),
                        "error should describe missing operands");
            }
        });

        runner.runTest("Assembler rejects duplicate labels", () -> {
            try {
                Assembler.assemble("loop: mov r0, #1\nloop: mov r1, #2");
                runner.fail("duplicate label should be rejected");
            } catch (IllegalArgumentException e) {
                runner.assertTrue(e.getMessage().contains("Duplicate label"),
                        "error should describe duplicate label");
            }
        });
    }
}
