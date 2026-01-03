package simulator;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

// This class represents the assembler's intermediate representation (IR) 
// of an assembly source.
//
// program — the ordered list of Instruction objects that make up the 
// assembled program. Each entry corresponds to one instruction in the final 
// instruction stream.
//
// lines — a parallel list of source-line numbers that map each
// instruction in program to its originating line in the source.
// This list exists solely for visualization and error reporting,
// and is not required for execution semantics.
//
// labels_to_pc — a mapping from label names to their resolved
// program-counter (PC) addresses. This map is populated during label
// resolution and is used to replace symbolic references with concrete
// PC values.

class AsmIR {
    List<Instruction> program;
    List<Integer> lines; // this is needed solely for visualization purposes.
    Map<String, Integer> labels_to_pc;

    public AsmIR() {
        this.program = new ArrayList<Instruction>();
        this.lines = new ArrayList<Integer>();
        this.labels_to_pc = new HashMap<String, Integer>();
    }
}

// This is the main class that implements the assembler.
// The assembler does two passes to convert the source to an intermediate
// representation (aka AsmIR). Labels are resolved in the first pass, and
// AsmIR generation in the second pass.
public class Assembler {
    // This is a helper method that parses immediate values.
    // Immediate values start with a '#' followed by one of the two prefixes:
    // 0x for hexadecimal and 0b for binary.
    private static int parseImmediate(String token) {
        String t = token.trim().substring(1); // skip the '#'
        if (t == "") {
            throw new IllegalArgumentException("Invalid immediate value.");
        }
        int sign = 1;
        if (t.startsWith("-")) {
            sign = -1;
            t = t.substring(1); // skip the '-'
        }

        if (t.startsWith("0b")) {
            // binary (base 2)
            return sign * Integer.parseInt(t.substring(2), 2);
        } else if (t.startsWith("0x")) {
            // hexadecimal (base 16)
            return sign * Integer.parseInt(t.substring(2), 16);
        } else {
            // decimal (base 10)
            return sign * Integer.parseInt(t);
        }
    }

    // This parses register names by performing a lookup on the Registers enum.
    private static Registers parseRegister(String token) {
        String s = token.trim().toUpperCase();

        try {
            return Registers.valueOf(s);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown register '" + token + "'");
        }
    }

    // This helper parses instructions with two operands. It takes as an 
    // argument a single source line (lexemes) and returns an Instruction object.
    private static Instruction parseTwoOperandInst(String[] lexemes, String op,
            OpCodes regOpCode, OpCodes immOpCode, int lineNum) {
        if (lexemes.length != 3) {
            throw new IllegalArgumentException(op + " requires 2 operands at line " + lineNum);
        }

        Registers dst = parseRegister(lexemes[1]);
        if (lexemes[2].startsWith("#")) { // source is an immediate
            return new Instruction(immOpCode, dst, parseImmediate(lexemes[2]));
        } else { // source is a register
            return new Instruction(regOpCode, dst, parseRegister(lexemes[2]));
        }
    }

    // This helper parses jump instructions.
    private static Instruction parseJumpInst(String[] lexemes, OpCodes opCode,
            Map<String, Integer> labels, int lineNum) {
        if (lexemes.length != 2) {
            throw new IllegalArgumentException(opCode + " requires 1 operand at line " 
                    + lineNum);
        }

        String operand = lexemes[1];
        Integer labelPc = labels.get(operand); // get the PC value if it exists.
        // if there is no PC value for the given operand, it means that this 
        // value is either an immediate or this jump instruction is invalid.
        // This is because jump instructions can only be of two forms:
        // jump LABEL or jump #immediate.  
        int target = (labelPc != null) ? labelPc : parseImmediate(operand);

        return new Instruction(opCode, target);
    }

    public static AsmIR assemble(String src) {
        String[] lines = src.split("\n");
        AsmIR result = new AsmIR();

        // First pass: collect labels and determine PC for each instruction
        int pc = 0; 
        for (int line_idx = 0; line_idx < lines.length; line_idx++) {
            String original = lines[line_idx];
            int commentIdx = original.indexOf(';'); // checks if there is a comment.
            // if there was a comment, ignore the comment.
            String line = (commentIdx >= 0 ? original.substring(0, commentIdx) : original).trim();
            if (line.isEmpty()) // if it was a comment-only line, skip.
                continue;

            int colonIdx = line.indexOf(':'); 
            if (colonIdx >= 0) { // any labels?
                String label = line.substring(0, colonIdx).trim(); 
                if (label.isEmpty()) {
                    throw new IllegalArgumentException("Empty label at line " + (line_idx + 1));
                }
                if (result.labels_to_pc.containsKey(label)) {
                    throw new IllegalArgumentException("Duplicate label '" + label + "' at line " 
                            + (line_idx + 1));
                }
                // we've found a valid label, store the PC value for that label.
                result.labels_to_pc.put(label, pc);
                line = line.substring(colonIdx + 1).trim();
                if (line.isEmpty())
                    continue; // label-only line, skip.
            }
            pc++;
        }

        // Second pass: parse instructions using the collected labels
        for (int line_idx = 0; line_idx < lines.length; line_idx++) {
            String original = lines[line_idx];
            int commentIdx = original.indexOf(';');
            String line = (commentIdx >= 0 ? original.substring(0, commentIdx) : original).trim();
            if (line.isEmpty())
                continue;

            int colonIdx = line.indexOf(':');
            if (colonIdx >= 0) {
                line = line.substring(colonIdx + 1).trim();
                if (line.isEmpty())
                    continue;
            }

            // now that we've resolved the labels, parse the instruction.
            // this simple regular expression is enough to split out a line
            // into the relevant components (lexemes).
            String[] lexemes = line.split("[\s,]+");
            if (lexemes.length == 0)
                continue;

            String op = lexemes[0].toLowerCase(); // the mnemonic
            Instruction inst;

            try {
                switch (op) {
                    case "mov":
                        inst = parseTwoOperandInst(lexemes, op,
                                OpCodes.MOV, OpCodes.MOV_IMM, line_idx + 1);
                        break;
                    case "ld":
                        inst = parseTwoOperandInst(lexemes, op,
                                OpCodes.LOAD, OpCodes.LOAD, line_idx + 1);
                        break;
                    case "st":
                        inst = parseTwoOperandInst(lexemes, op,
                                OpCodes.STORE, OpCodes.STORE, line_idx + 1);
                        break;
                    case "add":
                        inst = parseTwoOperandInst(lexemes, op,
                                OpCodes.ADD, OpCodes.ADD_IMM, line_idx + 1);
                        break;
                    case "sub":
                        inst = parseTwoOperandInst(lexemes, op,
                                OpCodes.SUB, OpCodes.SUB_IMM, line_idx + 1);
                        break;
                    case "or":
                        inst = parseTwoOperandInst(lexemes, op,
                                OpCodes.OR, OpCodes.OR_IMM, line_idx + 1);
                        break;
                    case "and":
                        inst = parseTwoOperandInst(lexemes, op,
                                OpCodes.AND, OpCodes.AND_IMM, line_idx + 1);
                        break;
                    case "xor":
                        inst = parseTwoOperandInst(lexemes, op,
                                OpCodes.XOR, OpCodes.XOR_IMM, line_idx + 1);
                        break;
                    case "cmp":
                        inst = parseTwoOperandInst(lexemes, op,
                                OpCodes.CMP, OpCodes.CMP, line_idx + 1);
                        break;
                    case "jmp":
                        inst = parseJumpInst(lexemes, OpCodes.JMP, 
                                result.labels_to_pc, line_idx + 1);
                        break;
                    case "rjmp":
                        inst = parseJumpInst(lexemes, OpCodes.RJMP, 
                                result.labels_to_pc, line_idx + 1);
                        break;
                    case "breq":
                        inst = parseJumpInst(lexemes, OpCodes.BREQ, 
                                result.labels_to_pc, line_idx + 1);
                        break;
                    case "brne":
                        inst = parseJumpInst(lexemes, OpCodes.BRNE, 
                                result.labels_to_pc, line_idx + 1);
                        break;
                    case "brlo":
                        inst = parseJumpInst(lexemes, OpCodes.BRLO, 
                                result.labels_to_pc, line_idx + 1);
                        break;
                    case "brsh":
                        inst = parseJumpInst(lexemes, OpCodes.BRSH, 
                                result.labels_to_pc, line_idx + 1);
                        break;
                    case "brmi":
                        inst = parseJumpInst(lexemes, OpCodes.BRMI, 
                                result.labels_to_pc, line_idx + 1);
                        break;
                    case "brpl":
                        inst = parseJumpInst(lexemes, OpCodes.BRPL,
                                result.labels_to_pc, line_idx + 1);
                        break;
                    case "brge":
                        inst = parseJumpInst(lexemes, OpCodes.BRGE,
                                result.labels_to_pc, line_idx + 1);
                        break;
                    case "brlt":
                        inst = parseJumpInst(lexemes, OpCodes.BRLT,
                                result.labels_to_pc, line_idx + 1);
                        break;
                    case "push":
                        int val = parseImmediate(lexemes[1]);
                        inst = new Instruction(OpCodes.PUSH, val);
                        break;
                    case "pop":
                        inst = new Instruction(OpCodes.POP);
                        break;
                    case "call":
                        inst = parseJumpInst(lexemes, OpCodes.CALL, 
                                result.labels_to_pc, line_idx + 1);
                        break;
                    case "ret":
                        if (lexemes.length != 1) {
                            throw new IllegalArgumentException("ret requires no operands at line " 
                                    + (line_idx + 1));
                        }
                        inst = new Instruction(OpCodes.RET);
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "Invalid instruction '" + op + "' at line " + 
                                (line_idx + 1));
                }
            } catch (IllegalArgumentException e) {
                // rethrow with line info
                throw new IllegalArgumentException("Error parsing line " +
                        (line_idx + 1) + ": '"  + original + "': " + e.getMessage(), e);
            }

            result.program.add(inst);
            result.lines.add(line_idx + 1);
        }

        return result;
    }
};
