package ru.iammaxim.ourlang.interpreter;

import ru.iammaxim.ourlang.Operation;
import ru.iammaxim.ourlang.OperationCode;
import ru.iammaxim.ourlang.interpreter.instructions.*;

import java.util.HashMap;

public class Interpreter {
    private static int[] ops = new int[1024];
    private static byte[] stack = new byte[4096]; // 4KB of stack

    private static int opPointer = 0;
    private static int stackPointer = 0;
    private static int arPointer = 0;

    public static HashMap<Integer, Instruction> instructions = new HashMap<>();

    public static void initInstructions() {
        instructions.put(OperationCode.ADD, new InstructionAdd());
        instructions.put(OperationCode.SUB, new InstructionSub());
        instructions.put(OperationCode.MUL, new InstructionMul());
        instructions.put(OperationCode.DIV, new InstructionDiv());
        instructions.put(OperationCode.AND, new InstructionAnd());
        instructions.put(OperationCode.OR, new InstructionOr());
        instructions.put(OperationCode.XOR, new InstructionXor());
        instructions.put(OperationCode.NOT, new InstructionNot());
        instructions.put(OperationCode.JMP, new InstructionJmp());
        instructions.put(OperationCode.JIF, new InstructionJif());
        instructions.put(OperationCode.INC, new InstructionInc());
        instructions.put(OperationCode.DEC, new InstructionDec());
        instructions.put(OperationCode.EQ, new InstructionEq());
        instructions.put(OperationCode.LE, new InstructionLe());
        instructions.put(OperationCode.LEE, new InstructionLee());
        instructions.put(OperationCode.GR, new InstructionGr());
        instructions.put(OperationCode.GRE, new InstructionGre());
        instructions.put(OperationCode.PUTB, new InstructionPutb());
        instructions.put(OperationCode.PUTW, new InstructionPutw());
        instructions.put(OperationCode.POP, new InstructionPop());
        instructions.put(OperationCode.SB, new InstructionSb());
        instructions.put(OperationCode.SW, new InstructionSw());
        instructions.put(OperationCode.LB, new InstructionLb());
        instructions.put(OperationCode.LW, new InstructionLw());
        instructions.put(OperationCode.PUTARA, new InstructionPutara());

    }

    public static void start(byte[] program) {
        if (program.length > ops.length * 4)
            throw new IllegalArgumentException("Too big program, can't fit it to RAM");

        for (int i = 0; i < program.length; i += 4) {
            int op = program[i] << 24 | program[i + 1] << 16 | program[i + 2] << 8 | program[i + 3];
            ops[i / 4] = op;
        }

        while (true) {
            int op = ops[opPointer];
            Operation operation = Operation.fromBinaryCode(op);

            instructions.get(operation.code).execute(operation.data);

            opPointer++;
        }
    }
}
