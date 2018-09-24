package ru.iammaxim.ourlang.interpreter.instructions;

import ru.iammaxim.ourlang.interpreter.Interpreter;

public class InstructionSb extends Instruction {
    @Override
    public void execute(int data) {
        int value = Interpreter.popByteFromStack();
        int address = Interpreter.popWordFromStack();

        Interpreter.writeByteToAddress(value, address);
    }
}
