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

        for (Function function : program.functions) {
            for (Operation operation : function.getOperations()) {
                writeOperation(os, operation.toBinaryCode());
            }
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
