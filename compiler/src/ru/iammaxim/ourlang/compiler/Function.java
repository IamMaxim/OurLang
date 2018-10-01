package ru.iammaxim.ourlang.compiler;

import ru.iammaxim.ourlang.Operation;
import ru.iammaxim.ourlang.parser.ParsedFunction;
import ru.iammaxim.ourlang.parser.Variable;

import java.util.ArrayList;

public class Function {
    public String name;
    public ActivationRecord activationRecord;
    private ArrayList<Operation> operations = new ArrayList<>();
    private ArrayList<String> arguments = new ArrayList<>();
    private ArrayList<Integer> argumentSizes = new ArrayList<>();

    public Function(ParsedFunction pf) throws VariableAlreadyDeclaredException {
        this.name = pf.name;

        // initialize activation record
        this.activationRecord = new ActivationRecord(pf.type.getSize());

        // add all arguments to the activation record (they act like local variables internally)
        for (Variable arg : pf.args) {
            activationRecord.addVariable(arg.name, arg.type.getSize());
            arguments.add(arg.name);
            argumentSizes.add(arg.type.getSize());
        }

        // add all local variables to the activation record
        for (Variable var : pf.localVars) {
            activationRecord.addVariable(var.name, var.type.getSize());
        }
    }

    public ArrayList<Integer> getArgumentSizes() {
        return argumentSizes;
    }

    public ArrayList<String> getArguments() {
        return arguments;
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
