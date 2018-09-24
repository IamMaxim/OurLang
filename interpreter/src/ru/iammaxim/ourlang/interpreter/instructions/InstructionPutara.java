package ru.iammaxim.ourlang.interpreter.instructions;

import ru.iammaxim.ourlang.interpreter.Interpreter;

public class InstructionPutara extends Instruction {
    @Override
    public void execute(int data) {
        if (Interpreter.debugInstructions)
            System.out.println("> putara " + Interpreter.getActivationRecordAddress());

        Interpreter.putWordIntoStack(Interpreter.getActivationRecordAddress());
    }
}
