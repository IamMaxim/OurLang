package ru.iammaxim.ourlang.interpreter.instructions;

import ru.iammaxim.ourlang.interpreter.Interpreter;

public class InstructionDiv extends Instruction {
    @Override
    public void execute(int data) {
        int second = Interpreter.popWordFromStack();
        int first = Interpreter.popWordFromStack();

        if (Interpreter.debugInstructions)
            System.out.println("> div " + (first / second));

        Interpreter.putWordIntoStack(first / second);
    }
}
