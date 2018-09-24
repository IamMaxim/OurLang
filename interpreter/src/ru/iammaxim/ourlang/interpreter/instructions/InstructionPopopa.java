package ru.iammaxim.ourlang.interpreter.instructions;

import ru.iammaxim.ourlang.interpreter.Interpreter;

public class InstructionPopopa extends Instruction {
    @Override
    public void execute(int data) {
        int first = Interpreter.popWordFromStack();

        if (Interpreter.debugInstructions)
            System.out.println("> popopa " + first);

        Interpreter.setOperationPointer(first);
    }
}
