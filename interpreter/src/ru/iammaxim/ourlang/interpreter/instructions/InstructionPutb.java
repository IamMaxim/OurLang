package ru.iammaxim.ourlang.interpreter.instructions;

import ru.iammaxim.ourlang.interpreter.Interpreter;

public class InstructionPutb extends Instruction {
    @Override
    public void execute(int data) {
        if (Interpreter.debugInstructions)
            System.out.println("> putb " + data);

        Interpreter.putByteIntoStack(data);
    }
}
