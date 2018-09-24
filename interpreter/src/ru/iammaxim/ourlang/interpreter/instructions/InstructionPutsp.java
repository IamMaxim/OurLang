package ru.iammaxim.ourlang.interpreter.instructions;

import ru.iammaxim.ourlang.interpreter.Interpreter;

public class InstructionPutsp extends Instruction {
    @Override
    public void execute(int data) {
        if (Interpreter.debugInstructions)
            System.out.println("> putsp " + Interpreter.getStackPointer());

        Interpreter.putWordIntoStack(Interpreter.getStackPointer());
    }
}
