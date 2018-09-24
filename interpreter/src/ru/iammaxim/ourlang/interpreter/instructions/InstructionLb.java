package ru.iammaxim.ourlang.interpreter.instructions;

import ru.iammaxim.ourlang.interpreter.Interpreter;

public class InstructionLb extends Instruction {
    @Override
    public void execute(int data) {
        int address = Interpreter.popWordFromStack();

        Interpreter.putByteIntoStack(Interpreter.getByteByAddress(address));
    }
}
