package ru.iammaxim.ourlang.interpreter.instructions;

import ru.iammaxim.ourlang.interpreter.Interpreter;

public class InstructionDec extends Instruction {
    @Override
    public void execute(int data) {
        int first = Interpreter.popWordFromStack();

        Interpreter.putWordIntoStack(first - 1);
    }
}
