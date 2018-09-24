package ru.iammaxim.ourlang.interpreter.instructions.debug;

import ru.iammaxim.ourlang.interpreter.Interpreter;
import ru.iammaxim.ourlang.interpreter.instructions.Instruction;

public class InstructionPrintbyte extends Instruction {
    @Override
    public void execute(int data) {
        int val = Interpreter.popByteFromStack();
        System.out.println(val);
    }
}
