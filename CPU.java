package simulator;

public class CPU {
    public static Memory memory = new Memory();
    public static int[] registers = new int[Registers.values().length];
    public static boolean halt = false;
    public static ALU alu = new ALU();

    public CPU() {
        registers[Registers.SP.ordinal()] = 0xFFFF;
    }

    // This method runs one instruction at a time.
    public static void step(AsmIR code) {
        if (registers[Registers.PC.ordinal()] == code.program.size()) {
            halt = true;
            return;
        }

        Instruction inst = code.program.get(registers[Registers.PC.ordinal()]++);

        switch (inst.op) {
            case OpCodes.MOV:
                registers[inst.dst.ordinal()] = registers[inst.src.ordinal()];
                break;
            case OpCodes.MOV_IMM:
                registers[inst.dst.ordinal()] = inst.immediate & 0xffff; 
                break;
            case OpCodes.LOAD:
                // each entry in memory is a byte, and a register is two bytes,
                // so we load two adjacent bytes.
                int low_byte = memory.read(registers[inst.src.ordinal()]) & 0xFF;
                int high_byte = memory.read(registers[inst.src.ordinal()] + 1) & 0xFF;
                registers[inst.dst.ordinal()] = low_byte | (high_byte << 8);
                break;
            case OpCodes.STORE:
                byte val_low = (byte) (registers[inst.src.ordinal()] & 0xff);
                memory.write(registers[inst.dst.ordinal()], val_low);
                byte val_high = (byte) ((registers[inst.src.ordinal()] >> 8) & 0xff);
                memory.write(registers[inst.dst.ordinal()] + 1, val_high);
                break;
            case OpCodes.ADD:
                registers[inst.dst.ordinal()] = alu.add(registers[inst.dst.ordinal()], registers[inst.src.ordinal()]);
                break;
            case OpCodes.ADD_IMM:
                registers[inst.dst.ordinal()] = alu.add(registers[inst.dst.ordinal()], inst.immediate);
                break;
            case OpCodes.SUB:
                registers[inst.dst.ordinal()] = alu.sub(registers[inst.dst.ordinal()], registers[inst.src.ordinal()]);
                break;
            case OpCodes.SUB_IMM:
                registers[inst.dst.ordinal()] = alu.sub(registers[inst.dst.ordinal()], inst.immediate);
                break;
            case OpCodes.OR:
                registers[inst.dst.ordinal()] = alu.or(registers[inst.dst.ordinal()], registers[inst.src.ordinal()]);
                break;
            case OpCodes.OR_IMM:
                registers[inst.dst.ordinal()] = alu.or(registers[inst.dst.ordinal()], registers[inst.immediate]);
                break;
            case OpCodes.XOR:
                registers[inst.dst.ordinal()] = alu.xor(registers[inst.dst.ordinal()], registers[inst.src.ordinal()]);
                break;
            case OpCodes.XOR_IMM:
                registers[inst.dst.ordinal()] = alu.xor(registers[inst.dst.ordinal()], registers[inst.immediate]);
                break;
            case OpCodes.AND:
                registers[inst.dst.ordinal()] = alu.and(registers[inst.dst.ordinal()], registers[inst.src.ordinal()]);
                break;
            case OpCodes.AND_IMM:
                registers[inst.dst.ordinal()] = alu.and(registers[inst.dst.ordinal()], registers[inst.immediate]);
                break;
            case OpCodes.CMP:
                alu.cmp(registers[inst.dst.ordinal()], registers[inst.src.ordinal()]);
                break;
            case OpCodes.JMP:
                registers[Registers.PC.ordinal()] = inst.immediate;
                break;
            case OpCodes.RJMP:
                registers[Registers.PC.ordinal()] += inst.immediate;
                break;
            case OpCodes.BREQ:
                if (alu.Z) {
                    registers[Registers.PC.ordinal()] = inst.immediate;
                }
                break;
            case OpCodes.BRNE:
                if (!alu.Z) {
                    registers[Registers.PC.ordinal()] = inst.immediate;
                }
                break;
            case OpCodes.BRLO:
                if (alu.C) {
                    registers[Registers.PC.ordinal()] = inst.immediate;
                }
                break;
            case OpCodes.BRSH:
                if (!alu.C) {
                    registers[Registers.PC.ordinal()] = inst.immediate;
                }
                break;
            case OpCodes.BRMI:
                if (alu.N) {
                    registers[Registers.PC.ordinal()] = inst.immediate;
                }
                break;
            case OpCodes.BRPL:
                if (!alu.N) {
                    registers[Registers.PC.ordinal()] = inst.immediate;
                }
                break;
            case OpCodes.BRGE:
                if (!(alu.N ^ alu.V)) {
                    registers[Registers.PC.ordinal()] = inst.immediate;
                }
                break;
            case OpCodes.BRLT:
                if (alu.N ^ alu.V) {
                    registers[Registers.PC.ordinal()] = inst.immediate;
                }
                break;
            case OpCodes.PUSH:
                push_stack(inst.immediate);
                break;
            case OpCodes.POP:
                registers[Registers.SP.ordinal()] += 2;
                break;
            case OpCodes.CALL:
                push_stack(registers[Registers.PC.ordinal()]);
                break;
            case OpCodes.RET:
                registers[Registers.PC.ordinal()] = registers[Registers.SP.ordinal()];
                registers[Registers.SP.ordinal()] += 2;
                break;
            default:
                halt = true;
                throw new RuntimeException("Unknown instruction");
        }
    }

    // helper method to push a value onto the stack
    public static void push_stack(int val) {
        registers[Registers.SP.ordinal()] -= 2;
        int sp = registers[Registers.SP.ordinal()];
        if (sp < 0xF000) { // 64k - 4k = 0xF000
            throw new RuntimeException("STACK OVERFLOW");
        }
        memory.write(sp, (byte) (val & 0xFF));
        memory.write(sp + 1, (byte) ((val >> 8) & 0xFF));
    }
};
