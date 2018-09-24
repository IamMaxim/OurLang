package ru.iammaxim.ourlang.interpreter.instructions;

import ru.iammaxim.ourlang.interpreter.Interpreter;

public class InstructionOr extends Instruction {
    @Override
    public void execute(int data) {
        int second = Interpreter.popWordFromStack();
        int first = Interpreter.popWordFromStack();

        Interpreter.putWordIntoStack(first | second);

    }
}
