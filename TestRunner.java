package simulator;

public class TestRunner {
    private int failures = 0;
    private int tests = 0;

    @FunctionalInterface
    public interface Test {
        void run() throws Exception;
    }

    public void runTest(String name, Test t) {
        System.out.print("RUN " + name + " ... ");
        tests++;
        try {
            t.run();
            System.out.println("PASS");
        } catch (AssertionError e) {
            failures++;
            System.out.println("FAIL");
            System.out.println("  " + e.getMessage());
        } catch (Throwable e) {
            failures++;
            System.out.println("ERROR");
            e.printStackTrace(System.out);
        }
    }

    public void assertEquals(int expected, int actual, String msg) {
        if (expected != actual) {
            throw new AssertionError(msg + ": expected=" + (expected & 0xffff) + " actual=" + (actual & 0xffff));
        }
    }

    public void assertEqualsExact(int expected, int actual, String msg) {
        if (expected != actual) {
            throw new AssertionError(msg + ": expected=" + expected + " actual=" + actual);
        }
    }

    public void assertTrue(boolean cond, String msg) {
        if (!cond)
            throw new AssertionError(msg);
    }

    public void assertFalse(boolean cond, String msg) {
        if (cond)
            throw new AssertionError(msg);
    }

    public void fail(String msg) {
        throw new AssertionError(msg);
    }

    public void resetCPU() {
        CPU.memory = new Memory();
        CPU.registers = new int[Registers.values().length];
        CPU.registers[Registers.SP.ordinal()] = 0xffff;
        CPU.halt = false;
        CPU.alu = new ALU();
    }

    public void runAsmProgramSafely(AsmIR code, int maxSteps) {
        int iter = 0;
        while (!CPU.halt && iter++ < maxSteps) {
            CPU.step(code);
        }
        if (iter >= maxSteps) {
            throw new AssertionError("Program exceeded max steps: " + maxSteps);
        }
    }

    public static void main(String[] args) {
        TestRunner runner = new TestRunner();

        TestAssembler.registerTests(runner);
        TestALU.registerTests(runner);
        TestCPU.registerTests(runner);

        System.out.println("--------------------------------------------------");
        System.out.println("Ran " + runner.tests + " tests, failures: " + runner.failures);
        if (runner.failures != 0) {
            System.exit(1);
        }
    }
}
