package ru.iammaxim.ourlang.interpreter.instructions;

import ru.iammaxim.ourlang.interpreter.Interpreter;

public class InstructionJif extends Instruction {
    @Override
    public void execute(int data) {
        int first = Interpreter.popWordFromStack();

        if (first != 0)
            Interpreter.setOperationPointer(data);
    }
}
