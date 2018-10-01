package ru.iammaxim.ourlang.interpreter.instructions;

import ru.iammaxim.ourlang.interpreter.Interpreter;

public class InstructionSub extends Instruction {
    @Override
    public void execute(int data) {
        if (Interpreter.debugInstructionsState) {
            System.out.println("Stack before sub:");
            Interpreter.printStack();
        }

        int second = Interpreter.popWordFromStack();
        int first = Interpreter.popWordFromStack();

        if (Interpreter.debugInstructions)
            System.out.println("> sub " + (first - second));

        Interpreter.putWordIntoStack(first - second);
    }
}
