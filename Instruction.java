package simulator;

enum OpCodes {
    MOV,
    MOV_IMM,
    LOAD,
    STORE,
    PUSH,
    POP,

    // Arithmetic and logic
    ADD,
    ADD_IMM,
    SUB,
    SUB_IMM,
    OR,
    OR_IMM,
    AND,
    AND_IMM,
    XOR,
    XOR_IMM,
    CMP,

    // control flow
    JMP,
    RJMP,
    BREQ,
    BRNE,
    BRLO,
    BRSH,
    BRMI,
    BRPL,
    BRGE,
    BRLT,
    CALL,
    RET
}

enum Registers {
    R0,
    R1,
    R2,
    R3,
    R4,
    R5,
    R6,
    R7,
    R8,
    R9,
    R10,
    R11,
    R12,
    R13,
    R14,
    R15,
    R16,
    R17,
    R18,
    R19,
    R20,
    R21,
    R23,
    R24,
    R25,
    R26,
    R27,
    R28,
    R29,
    R30,
    R31,
    PC,
    SP,
}

// an instruction should have an OP_CODE and the operands.
public class Instruction {
    OpCodes op;
    Registers dst;
    Registers src;
    int immediate;

    public Instruction() {

    }

    public Instruction(OpCodes op, Registers dst, Registers src, int immediate) {
        this.op = op;
        this.dst = dst;
        this.src = src;
        this.immediate = immediate;
    }

    public Instruction(OpCodes op, Registers dst, int immediate) {
        this.op = op;
        this.dst = dst;
        this.immediate = immediate;
    }

    public Instruction(OpCodes op, Registers dst, Registers src) {
        this.op = op;
        this.dst = dst;
        this.src = src;
        this.immediate = 0;
    }

    public Instruction(OpCodes op, int immediate) {
        this.op = op;
        this.immediate = immediate;
    }

    public Instruction(OpCodes op) {
        this.op = op;
    }
}
