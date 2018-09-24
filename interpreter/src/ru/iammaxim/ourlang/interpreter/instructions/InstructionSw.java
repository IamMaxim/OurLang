package ru.iammaxim.ourlang.interpreter.instructions;

import ru.iammaxim.ourlang.interpreter.Interpreter;

public class InstructionSw extends Instruction {
    @Override
    public void execute(int data) {
        int value = Interpreter.popWordFromStack();
        int address = Interpreter.popWordFromStack();

        if (Interpreter.debugInstructions)
            System.out.println("> sw " + value + " -> " + address);

        Interpreter.writeWordToAddress(value, address);
    }
}
