package ru.iammaxim.ourlang.compiler;

import ru.iammaxim.ourlang.Operation;
import ru.iammaxim.ourlang.OperationCode;

import java.io.IOException;
import java.io.OutputStream;

public class ProgramWriter {
    public static void write(OutputStream os, Program program) throws IOException {
        // get the main function
        Function mainFunction = program.getFunction("main");
        if (mainFunction == null) {
            throw new IllegalStateException("You have not declared function main(), can't compile program");
        }
        // write jump to a main function
        Operation jumpToMain = new Operation(OperationCode.JMP, program.getFunctionOffset(mainFunction));
        writeOperation(os, jumpToMain.toBinaryCode());

        for (Function function : program.functions) {
            for (Operation operation : function.getOperations()) {
                writeOperation(os, operation.toBinaryCode());
            }
        }
    }

    private static void writeOperation(OutputStream os, int op) throws IOException {
        os.write((op >> 24) & 0x000000ff);
        os.write((op >> 16) & 0x000000ff);
        os.write((op >> 8) & 0x000000ff);
        os.write((op) & 0x000000ff);
    }
}
