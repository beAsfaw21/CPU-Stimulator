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
            case MOV:
                registers[inst.dst.ordinal()] = registers[inst.src.ordinal()];
                break;
            case MOV_IMM:
                registers[inst.dst.ordinal()] = inst.immediate & 0xffff; 
                break;
            case LOAD:
                // each entry in memory is a byte, and a register is two bytes,
                // so we load two adjacent bytes.
                int low_byte = memory.read(registers[inst.src.ordinal()]) & 0xFF;
                int high_byte = memory.read(registers[inst.src.ordinal()] + 1) & 0xFF;
                registers[inst.dst.ordinal()] = low_byte | (high_byte << 8);
                break;
            case STORE:
                byte val_low = (byte) (registers[inst.src.ordinal()] & 0xff);
                memory.write(registers[inst.dst.ordinal()], val_low);
                byte val_high = (byte) ((registers[inst.src.ordinal()] >> 8) & 0xff);
                memory.write(registers[inst.dst.ordinal()] + 1, val_high);
                break;
            case ADD:
                registers[inst.dst.ordinal()] = alu.add(registers[inst.dst.ordinal()], registers[inst.src.ordinal()]);
                break;
            case ADD_IMM:
                registers[inst.dst.ordinal()] = alu.add(registers[inst.dst.ordinal()], inst.immediate);
                break;
            case SUB:
                registers[inst.dst.ordinal()] = alu.sub(registers[inst.dst.ordinal()], registers[inst.src.ordinal()]);
                break;
            case SUB_IMM:
                registers[inst.dst.ordinal()] = alu.sub(registers[inst.dst.ordinal()], inst.immediate);
                break;
            case OR:
                registers[inst.dst.ordinal()] = alu.or(registers[inst.dst.ordinal()], registers[inst.src.ordinal()]);
                break;
            case OR_IMM:
                registers[inst.dst.ordinal()] = alu.or(registers[inst.dst.ordinal()], registers[inst.immediate]);
                break;
            case XOR:
                registers[inst.dst.ordinal()] = alu.xor(registers[inst.dst.ordinal()], registers[inst.src.ordinal()]);
                break;
            case XOR_IMM:
                registers[inst.dst.ordinal()] = alu.xor(registers[inst.dst.ordinal()], registers[inst.immediate]);
                break;
            case AND:
                registers[inst.dst.ordinal()] = alu.and(registers[inst.dst.ordinal()], registers[inst.src.ordinal()]);
                break;
            case AND_IMM:
                registers[inst.dst.ordinal()] = alu.and(registers[inst.dst.ordinal()], registers[inst.immediate]);
                break;
            case CMP:
                alu.cmp(registers[inst.dst.ordinal()], registers[inst.src.ordinal()]);
                break;
            case JMP:
                registers[Registers.PC.ordinal()] = inst.immediate;
                break;
            case RJMP:
                registers[Registers.PC.ordinal()] += inst.immediate;
                break;
            case BREQ:
                if (alu.Z) {
                    registers[Registers.PC.ordinal()] = inst.immediate;
                }
                break;
            case BRNE:
                if (!alu.Z) {
                    registers[Registers.PC.ordinal()] = inst.immediate;
                }
                break;
            case BRLO:
                if (alu.C) {
                    registers[Registers.PC.ordinal()] = inst.immediate;
                }
                break;
            case BRSH:
                if (!alu.C) {
                    registers[Registers.PC.ordinal()] = inst.immediate;
                }
                break;
            case BRMI:
                if (alu.N) {
                    registers[Registers.PC.ordinal()] = inst.immediate;
                }
                break;
            case BRPL:
                if (!alu.N) {
                    registers[Registers.PC.ordinal()] = inst.immediate;
                }
                break;
            case BRGE:
                if (!(alu.N ^ alu.V)) {
                    registers[Registers.PC.ordinal()] = inst.immediate;
                }
                break;
            case BRLT:
                if (alu.N ^ alu.V) {
                    registers[Registers.PC.ordinal()] = inst.immediate;
                }
                break;
            case PUSH:
                push_stack(inst.immediate);
                break;
            case POP:
                registers[Registers.SP.ordinal()] += 2;
                break;
            case CALL:
                push_stack(registers[Registers.PC.ordinal()]);
                registers[Registers.PC.ordinal()] = inst.immediate;
                break;
            case RET:
                int returnAddress = (memory.read(registers[Registers.SP.ordinal()]) & 0xFF)
                        | ((memory.read(registers[Registers.SP.ordinal()] + 1) & 0xFF) << 8);
                registers[Registers.PC.ordinal()] = returnAddress;
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
