package ru.iammaxim.ourlang.interpreter.instructions;

import ru.iammaxim.ourlang.interpreter.Interpreter;

public class InstructionJmp extends Instruction {
    @Override
    public void execute(int data) {
        Interpreter.setOperationPointer(data);
    }
}
