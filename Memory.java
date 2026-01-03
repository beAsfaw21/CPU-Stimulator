package simulator;

// This defines the memory of the CPU.
// The CPU has 64kb of memory of which 4kb is the stack.
public class Memory {
    private final byte[] ram   = new byte[61440];  // 60 KB
    private final byte[] stack = new byte[4096];   // 4 KB

    public byte read(int addr) {
        if (addr >= 0xF000) {
            return stack[addr - 0xF000];
        }
        return ram[addr];
    }

    public void write(int addr, byte value) {
        if (addr >= 0xF000) {
            stack[addr - 0xF000] = value;
        } else {
            ram[addr] = value;
        }
    }
}
