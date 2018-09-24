package ru.iammaxim.ourlang.interpreter.instructions;

import ru.iammaxim.ourlang.interpreter.Interpreter;

public class InstructionPutopa extends Instruction {
    @Override
    public void execute(int data) {
        if (Interpreter.debugInstructions)
            System.out.println("> putopa " + Interpreter.getCurrentOperationAddress());

        Interpreter.putWordIntoStack(Interpreter.getCurrentOperationAddress());
    }
}
