package ru.iammaxim.ourlang.interpreter;

import ru.iammaxim.ourlang.Operation;
import ru.iammaxim.ourlang.OperationCode;
import ru.iammaxim.ourlang.interpreter.instructions.*;
import ru.iammaxim.ourlang.interpreter.instructions.debug.InstructionPrintbyte;
import ru.iammaxim.ourlang.interpreter.instructions.debug.InstructionPrintword;

import java.util.HashMap;

public class Interpreter {
    private static int[] ops = new int[1024];
    private static byte[] memory = new byte[32768];

    private static int opPointer = 0;
    private static int stackPointer = 0;
    private static int arPointer = 0;

    public static HashMap<Integer, Instruction> instructions = new HashMap<>();

    private static boolean needToRun = true;

    public static final boolean debugInstructions = true;
    public static final boolean debugInstructionNumbers = true;
    public static final boolean debugInstructionsState = false;

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
        instructions.put(OperationCode.PUTOPA, new InstructionPutopa());
        instructions.put(OperationCode.POPARA, new InstructionPopara());
        instructions.put(OperationCode.POPOPA, new InstructionPopopa());
        instructions.put(OperationCode.STOP, new InstructionStop());
        instructions.put(OperationCode.PUTSP, new InstructionPutsp());


        instructions.put(OperationCode.PRINTBYTE, new InstructionPrintbyte());
        instructions.put(OperationCode.PRINTWORD, new InstructionPrintword());

    }

    private static HashMap<Integer, String> instructionNames = new HashMap<>();

    static {
        instructionNames.put(OperationCode.ADD, "ADD");
        instructionNames.put(OperationCode.SUB, "SUB");
        instructionNames.put(OperationCode.MUL, "MUL");
        instructionNames.put(OperationCode.DIV, "DIV");
        instructionNames.put(OperationCode.AND, "AND");
        instructionNames.put(OperationCode.OR, "OR");
        instructionNames.put(OperationCode.XOR, "XOR");
        instructionNames.put(OperationCode.NOT, "NOT");
        instructionNames.put(OperationCode.JMP, "JMP");
        instructionNames.put(OperationCode.JIF, "JIF");
        instructionNames.put(OperationCode.INC, "INC");
        instructionNames.put(OperationCode.DEC, "DEC");
        instructionNames.put(OperationCode.EQ, "EQ");
        instructionNames.put(OperationCode.LE, "LE");
        instructionNames.put(OperationCode.LEE, "LEE");
        instructionNames.put(OperationCode.GR, "GR");
        instructionNames.put(OperationCode.GRE, "GRE");
        instructionNames.put(OperationCode.PUTB, "PUTB");
        instructionNames.put(OperationCode.PUTW, "PUTW");
        instructionNames.put(OperationCode.POP, "POP");
        instructionNames.put(OperationCode.SB, "SB");
        instructionNames.put(OperationCode.SW, "SW");
        instructionNames.put(OperationCode.LB, "LB");
        instructionNames.put(OperationCode.LW, "LW");
        instructionNames.put(OperationCode.PUTARA, "PUTARA");
        instructionNames.put(OperationCode.PUTOPA, "PUTOPA");
        instructionNames.put(OperationCode.POPARA, "POPARA");
        instructionNames.put(OperationCode.POPOPA, "POPOPA");
        instructionNames.put(OperationCode.STOP, "STOP");
        instructionNames.put(OperationCode.PUTSP, "PUTSP");

        instructionNames.put(OperationCode.PRINTBYTE, "PRINTBYTE");
        instructionNames.put(OperationCode.PRINTWORD, "PRINTWORD");
    }

    public static void start(byte[] program) {
        if (program.length > ops.length * 4)
            throw new IllegalArgumentException("Too big program, can't fit it to RAM");

        initInstructions();

        int offset = 0;

        for (int i = 0; i < program.length; i += 4) {
            int op = ((program[i] & 0xff) << 24) | ((program[i + 1] & 0xff) << 16) | ((program[i + 2] & 0xff) << 8) | (program[i + 3] & 0xff);
            ops[i / 4] = op;

            Operation oper = Operation.fromBinaryCode(op);
            System.out.println(offset + " " + instructionNames.get(oper.code) + " " + oper.data + " | ");
//                    Integer.toBinaryString((program[i]) & 0xff) + " " +
//                    Integer.toBinaryString((program[i + 1]) & 0xff) + " " +
//                    Integer.toBinaryString((program[i + 2]) & 0xff) + " " +
//                    Integer.toBinaryString((program[i + 3]) & 0xff));
            offset += 1;
        }

        stackPointer = offset * 4;
        System.out.println("Initial stack pointer in bytes: " + stackPointer);

        while (needToRun) {
            int op = ops[opPointer];
            Operation operation = Operation.fromBinaryCode(op);

            if (debugInstructionNumbers)
                System.out.println("executing " + opPointer + ": " + instructionNames.get(operation.code) + " " + operation.data);

            instructions.get(operation.code).execute(operation.data);

            opPointer++;
        }
    }

    public static int popWordFromStack() {
        int result = ((memory[stackPointer - 2] & 0xff) << 8) | (memory[stackPointer - 1] & 0xff);
        stackPointer -= 2;


//        if (debugInstructionsState)
            System.out.println("Popped " + result);

        return result;
    }

    public static void putWordIntoStack(int word) {
//        if (debugInstructionsState)
            System.out.println("Putting " + word);

        memory[stackPointer] = (byte) ((word >> 8) & 0xff);
        memory[stackPointer + 1] = (byte) ((word) & 0xff);
        stackPointer += 2;
    }

    public static void setOperationPointer(int address) {
        opPointer = address;
    }

    public static int getByteByAddress(int address) {
        return memory[address];
    }

    public static int getWordByAddress(int address) {
        return ((memory[address] & 0xff) << 8) | (memory[address + 1] & 0xff);
    }

    public static void putByteIntoStack(int b) {
        memory[stackPointer] = (byte) b;
        stackPointer++;
    }

    public static void moveStackPointerBackBy(int offset) {
        stackPointer -= offset;
    }

    public static void setActivationRecordAddress(int address) {
        arPointer = address;
    }

    public static int getActivationRecordAddress() {
        return arPointer;
    }

    public static int getCurrentOperationAddress() {
        return opPointer;
    }

    public static int popByteFromStack() {
        int result = memory[stackPointer - 1];
        stackPointer -= 1;
        return result;
    }

    public static void writeByteToAddress(int value, int address) {
        memory[address] = (byte) value;
    }

    public static void writeWordToAddress(int value, int address) {
        memory[address] = (byte) (value >> 8);
        memory[address + 1] = (byte) (value & 0xff);
    }

    public static void stop() {
        needToRun = false;
    }

    public static int getStackPointer() {
        return stackPointer;
    }

    public static void printStack() {
//        ArrayList<Integer> s = new ArrayList<>();
//        for (int i = 0; i < stackPointer; i += 2) {
//            s.add(getWordByAddress(i));
//        }
//        System.out.println(s);
    }
}
