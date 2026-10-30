package simulator;

public class TestALU {
    public static void registerTests(TestRunner runner) {
        runner.runTest("ALU.add overflow (0x8000 + 0x8000)", () -> {
            ALU alu = new ALU();
            int res = alu.add(0x8000, 0x8000);
            runner.assertEquals(0x0000, res, "add result");
            runner.assertTrue(alu.Z, "Z should be true");
            runner.assertTrue(alu.C, "C should be true");
            runner.assertTrue(alu.V, "V should be true");
            runner.assertFalse(alu.N, "N should be false");
        });

        runner.runTest("ALU.sub borrow (0x0000 - 0x0001)", () -> {
            ALU alu = new ALU();
            int res = alu.sub(0x0000, 0x0001);
            runner.assertEquals(0xffff, res & 0xffff, "sub result");
            runner.assertFalse(alu.Z, "Z should be false");
            runner.assertTrue(alu.N, "N should be true");
            runner.assertTrue(alu.C, "C should be true (borrow)");
        });

        runner.runTest("ALU.add wraps 16-bit operands", () -> {
            ALU alu = new ALU();
            int res = alu.add(0xffff, 1);
            runner.assertEquals(0x0000, res, "wrapped add result");
            runner.assertTrue(alu.Z, "Z should be true");
            runner.assertTrue(alu.C, "C should be true");
            runner.assertFalse(alu.N, "N should be false");
            runner.assertFalse(alu.V, "V should be false");
        });

        runner.runTest("ALU.sub detects signed overflow", () -> {
            ALU alu = new ALU();
            int res = alu.sub(0x8000, 1);
            runner.assertEquals(0x7fff, res, "overflowing subtraction result");
            runner.assertFalse(alu.Z, "Z should be false");
            runner.assertFalse(alu.N, "N should be false");
            runner.assertFalse(alu.C, "C should be false");
            runner.assertTrue(alu.V, "V should be true");
        });

        runner.runTest("ALU.logical operations clear arithmetic flags", () -> {
            ALU alu = new ALU();
            alu.add(0x7fff, 1);
            int res = alu.and(0x8000, 0x8000);
            runner.assertEquals(0x8000, res, "logical result");
            runner.assertFalse(alu.Z, "Z should be false");
            runner.assertTrue(alu.N, "N should be true");
            runner.assertFalse(alu.C, "C should be false");
            runner.assertFalse(alu.V, "V should be false");
        });
    }
}
