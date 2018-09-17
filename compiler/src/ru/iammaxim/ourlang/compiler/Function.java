package ru.iammaxim.ourlang.compiler;

import ru.iammaxim.ourlang.Parser.ParsedFunction;
import ru.iammaxim.ourlang.Parser.Variable;

import java.util.ArrayList;
import java.util.HashMap;

public class Function {
    public String name;
    private ArrayList<Operation> operations = new ArrayList<>();
    public ActivationRecord activationRecord;

    public Function(ParsedFunction pf) throws VariableAlreadyDeclaredException {
        this.name = pf.name;

        // initialize activation record
        this.activationRecord = new ActivationRecord(pf.type.getSize());

        // add all arguments to the activation record (they act like local variables internally)
        for (Variable arg : pf.args) {
            activationRecord.addVariable(arg.name, arg.type.getSize());
        }

        // add all local variables to the activation record
        for (Variable var : pf.localVars) {
            activationRecord.addVariable(var.name, var.type.getSize());
        }
    }

    public ArrayList<Operation> getOperations() {
        return operations;
    }

    public void addOperation(Operation op) {
        operations.add(op);
    }

    public int getOperationCount() {
        return operations.size();
    }
}
