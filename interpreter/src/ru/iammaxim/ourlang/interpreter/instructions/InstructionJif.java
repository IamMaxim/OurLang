package ru.iammaxim.ourlang.interpreter.instructions;

import ru.iammaxim.ourlang.interpreter.Interpreter;

public class InstructionJif extends Instruction {
    @Override
    public void execute(int data) {
        int first = Interpreter.popWordFromStack();

        if (Interpreter.debugInstructionsState)
            System.out.println("jif > " + first);

        if (first != 0) {
            Interpreter.setOperationPointer(data);
            System.out.println("Jumping to " + data);
        }
    }
}
