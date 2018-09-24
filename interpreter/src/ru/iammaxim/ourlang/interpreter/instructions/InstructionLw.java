package ru.iammaxim.ourlang.interpreter.instructions;

import ru.iammaxim.ourlang.interpreter.Interpreter;

public class InstructionLw extends Instruction {
    @Override
    public void execute(int data) {
        int address = Interpreter.popWordFromStack();

        if (Interpreter.debugInstructions)
            System.out.println("> lw " + Interpreter.getWordByAddress(address) + " -> stack");

        Interpreter.putWordIntoStack(Interpreter.getWordByAddress(address));

    }
}
