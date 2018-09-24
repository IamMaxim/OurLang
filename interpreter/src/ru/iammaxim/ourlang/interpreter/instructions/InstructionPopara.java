package ru.iammaxim.ourlang.interpreter.instructions;

import ru.iammaxim.ourlang.interpreter.Interpreter;

public class InstructionPopara extends Instruction {
    @Override
    public void execute(int data) {
        int first = Interpreter.popWordFromStack();

        if (Interpreter.debugInstructions)
            System.out.println("> popara " + first);

        Interpreter.setActivationRecordAddress(first);
    }
}
