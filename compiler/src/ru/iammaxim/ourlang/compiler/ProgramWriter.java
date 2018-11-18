package ru.iammaxim.ourlang.compiler;

import ru.iammaxim.ourlang.Operation;
import ru.iammaxim.ourlang.OperationCode;

import java.io.IOException;
import java.io.OutputStream;

public class ProgramWriter {
    public static void write(OutputStream os, Program program) throws IOException {
        // get the auto-generated __main__ function
        // this wrapper is needed to put the real main() activation record into stack
        Function mainFunction = program.getFunction("__main__");

        // write jump to a main function
        // subtract 1 from address since operation pointer will be incremented after instruction end
        Operation jumpToMain = new Operation(OperationCode.JMP, program.getFunctionOffset(mainFunction) - 1);
        writeOperation(os, jumpToMain.toBinaryCode());
        printVerilogInstruction(jumpToMain);

        for (Function function : program.functions) {
            for (Operation operation : function.getOperations()) {
                writeOperation(os, operation.toBinaryCode());
                printVerilogInstruction(operation);
            }
        }
    }

    private static void printVerilogInstruction(Operation jumpToMain) {
        if (Compiler.WRITE_VERILOG_CODE) {
            int first = jumpToMain.toBinaryCode() & 0xffff;
            int second = (jumpToMain.toBinaryCode() & 0xffff0000) >> 16;

            System.out.println("ram.putWordIntoStack(" + first + ");");
            System.out.println("ram.putWordIntoStack(" + second + ");");
        }
    }

    private static void writeOperation(OutputStream os, int op) throws IOException {
//        System.out.println("writing " + Integer.toBinaryString(op) + " " +
//                Integer.toBinaryString((byte) ((op >> 24) & 0xff)) + " " +
//                Integer.toBinaryString((byte) ((op >> 16) & 0xff)) + " " +
//                Integer.toBinaryString((byte) ((op >> 8) & 0xff)) + " " +
//                Integer.toBinaryString(((op) & 0x000000ff)));
        os.write((byte) ((op >> 24) & 0xff));
        os.write((byte) ((op >> 16) & 0xff));
        os.write((byte) ((op >> 8) & 0xff));
        os.write(((op) & 0xff));
    }
}
