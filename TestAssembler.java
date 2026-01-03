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
    }
}
