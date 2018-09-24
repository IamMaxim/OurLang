package ru.iammaxim.ourlang.interpreter.instructions;

import ru.iammaxim.ourlang.interpreter.Interpreter;

public class InstructionMul extends Instruction {
    @Override
    public void execute(int data) {
        int second = Interpreter.popWordFromStack();
        int first = Interpreter.popWordFromStack();

        if (Interpreter.debugInstructions)
            System.out.println("> mul " + (first * second));

        Interpreter.putWordIntoStack(first * second);

    }
}
