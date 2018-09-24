package ru.iammaxim.ourlang.compiler;

import ru.iammaxim.ourlang.Logger;
import ru.iammaxim.ourlang.Operation;
import ru.iammaxim.ourlang.OperationCode;
import ru.iammaxim.ourlang.parser.InvalidTokenException;
import ru.iammaxim.ourlang.parser.ParsedFunction;
import ru.iammaxim.ourlang.parser.expression.*;
import ru.iammaxim.ourlang.parser.type.TypeIdentifier;

import java.util.ArrayList;

public class Compiler {
    Program program = new Program();
    private Logger logger;

    private static final boolean DEBUG = true;

    public Compiler(Logger logger) {
        this.logger = logger;
    }


    public Program compile(ArrayList<ParsedFunction> functions) throws VariableAlreadyDeclaredException, InvalidTokenException {
        for (int i = 0; i < functions.size(); i++) {
            ParsedFunction pf = functions.get(i);
            Function f = new Function(pf);
            // we need to add function to program before actually compiling it
            // in order to be able to recursively execute it
            program.addFunction(f);

            logger.log("Compiling function '" + f.name + "':");
            logger.increateIndent();
            for (int j = 0; j < pf.expressions.size(); j++) {
                Expression expression = pf.expressions.get(j);
                logger.log("compiling > " + expression);
                compileStatement(f, expression);
            }

            // return control to caller of stop execution of program, if we reached end of __main__
            if (f.name.equals("__main__"))
                f.addOperation(new Operation(OperationCode.STOP, 0));
            else
                compileControlReturn(f);

            logger.decreaseIndent();
        }

        return program;
    }

    private void compileStatement(Function f, Expression expression) throws InvalidTokenException {
        if (expression instanceof ExpressionCondition) {
            ExpressionCondition exp = (ExpressionCondition) expression;

            // compile condition
            program.addComment("Condition");
            compileExpression(f, exp.cond);

            program.addComment("Comparison");

            // invert condition result, since we want to jump if it is not true
            f.addOperation(new Operation(OperationCode.NOT, 0));

            // this operation will jump to the elseBody (or the end of loop if there is no elseBody) if condition is false
            // we will set jump address later, after compilation of body
            Operation jmpOverBody = new Operation(OperationCode.JIF, 0);
            f.addOperation(jmpOverBody);


            // compile body statement-by-statement
            program.addComment("Body");
            for (Expression expression1 : exp.body) {
                compileStatement(f, expression1);
            }

            Operation jmpOverElseBody = new Operation(OperationCode.JMP, 0);
            if (exp.elseBody.size() != 0)
                f.addOperation(jmpOverElseBody);

            // this label indicates start of else body
            // subtract 1 from address because at the end of instruction execution it will be incremented
            int elseBodyStartLabel = program.getFunctionOffset(f) + f.getOperationCount() - 1;
            // jump here if condition is false
            jmpOverBody.data = elseBodyStartLabel;

            if (exp.elseBody.size() != 0) {
                // add jump over else body if it exists
//                f.addOperation(jmpOverElseBody);

                // compile elseBody statement-by-statement
                program.addComment("Else body");
                for (Expression expression1 : exp.elseBody) {
                    compileStatement(f, expression1);
                }

                // set address to jump in case when body completed execution and there is elseBody that we need to skip
                // subtract 1 from address because at the end of instruction execution it will be incremented
                int endOfElseBody = program.getFunctionOffset(f) + f.getOperationCount() - 1;
                jmpOverElseBody.data = endOfElseBody;
            }
            program.addComment("End of if () {} else {}");
        } else if (expression instanceof ExpressionForLoop) {
            ExpressionForLoop exp = (ExpressionForLoop) expression;

            throw new InvalidTokenException("For loop is not implemented yet!");
        } else if (expression instanceof ExpressionFunctionCall) {
            ExpressionFunctionCall exp = (ExpressionFunctionCall) expression;
            // compile call
            compileFunctionCall(f, exp);

            Function callee = program.getFunction(exp.functionName);

            if (callee.activationRecord.returnValueSize > 0) {
                // pop return value of a function, since we won't use it
                f.addOperation(new Operation(OperationCode.POP, callee.activationRecord.returnValueSize));
            }
        } else if (expression instanceof ExpressionReturn) {
            ExpressionReturn exp = (ExpressionReturn) expression;

            // compile address to store return value
            program.addComment("Return value address");
            f.addOperation(new Operation(OperationCode.PUTARA, 0));
//            f.addOperation(new Operation(OperationCode.PUTW, f.activationRecord.getReturnValueOffset()));
//            f.addOperation(new Operation(OperationCode.ADD, 0));

            // compile expression that will generate return value
            program.addComment("Return value");
            compileExpression(f, exp.returnExp);
            // copy that value from stack to activation record
            // TODO: implement non-word return value
            program.addComment("Move return value from stack to activation record");
            f.addOperation(new Operation(OperationCode.SW, 0));
            program.addComment("End of return");

            // return control to caller
            compileControlReturn(f);

        } else if (expression instanceof ExpressionTree) {
            ExpressionTree exp = (ExpressionTree) expression;

            if (!(
                    exp.left instanceof ExpressionValue &&
                            ((ExpressionValue) exp.left).value != null &&
                            (((ExpressionValue) exp.left).value.getType() instanceof TypeIdentifier)
            ))
                throw new InvalidTokenException("Left side of a tree is not an identifier, invalid construction");

            program.addComment("Tree");
            compileTree(f, exp);
            program.addComment("Tree end");

//            if (((ExpressionValue) exp.left).value.getType() instanceof TypeIdentifier) {
//                 pop the last pushed value, since we are out of tree and not about to use it.
//                program.addComment("Pop tree's return value from stack, since it won't be used");
//                f.addOperation(new Operation(OperationCode.POP, f.activationRecord.getVarSize(((ExpressionValue) exp.left).value.value)));
            /*} else*/
            if (((ExpressionValue) exp.left).getSize() > 0 || ((ExpressionValue) exp.left).value.getType() instanceof TypeIdentifier) {
                // pop the last pushed value, since we are out of tree and not about to use it.
                program.addComment("Pop tree's return value from stack, since it won't be used");
                f.addOperation(new Operation(OperationCode.POP, f.activationRecord.getVarSize(((ExpressionValue) exp.left).value.value)));
            }
        } else if (expression instanceof ExpressionValue) {
            throw new InvalidTokenException("Not a statement");
        } else if (expression instanceof ExpressionValueAt) {
            ExpressionValueAt exp = (ExpressionValueAt) expression;

            throw new InvalidTokenException("ValueAt is not implemented yet!");
        } else {
            throw new InvalidTokenException("Unexpected '" + expression.getClass().getSimpleName() + "' met during compilation");
        }
    }

    private void compileExpression(Function f, Expression expression) throws InvalidTokenException {
        if (expression instanceof ExpressionValue) {
            ExpressionValue exp = (ExpressionValue) expression;

            if (exp.value.getType() instanceof TypeIdentifier) {
                program.addComment("Identifier '" + exp.value.value + "'");

                // TODO: get value from needed address
                int addr = f.activationRecord.getVarOffset(exp.value.value);
                f.addOperation(new Operation(OperationCode.PUTARA, 0));
                f.addOperation(new Operation(OperationCode.PUTW, addr));
                f.addOperation(new Operation(OperationCode.ADD, 0));
                f.addOperation(new Operation(OperationCode.LW, 0));

            } else {
                int valueSize = exp.getSize();

            /*if (valueSize == 1) { // 8-bit value
                f.addOperation(new Operation(OperationCode.SB, exp.value.getValue()));
            } else*/
                if (valueSize == 2) { // 16-bit value
                    program.addComment("Value: '" + exp.value.getValue() + "'");
                    f.addOperation(new Operation(OperationCode.PUTW, exp.value.getValue()));
                } else {
                    // something bigger. We can't just put it into stack with one instruction, so let's put it into
                    // data part of our program
                    // TODO: implement this
                    throw new IllegalStateException("Values with not 2 bytes size are not implemented yet");
                }
            }

        } else if (expression instanceof ExpressionTree) {
            ExpressionTree exp = (ExpressionTree) expression;

            compileTree(f, exp);
        } else if (expression instanceof ExpressionFunctionCall) {
            ExpressionFunctionCall exp = (ExpressionFunctionCall) expression;
            compileFunctionCall(f, exp);
        } else
            throw new InvalidTokenException("Unexpected " + expression.getClass().getSimpleName() + " while compiling expression");
    }

    private void compileFunctionCall(Function f, ExpressionFunctionCall exp) throws InvalidTokenException {
        Function toCall = program.getFunction(exp.functionName);

        if (toCall == null) {
            throw new InvalidTokenException("Function '" + exp.functionName + "' is not declared");
        }

        ActivationRecord ar = toCall.activationRecord;

        program.addComment("Function '" + toCall.name + "' call");

        if (ar.returnValueSize == 0)
            program.addComment("Void return type, no space for return value needed");
        else
            program.addComment("Allocate space for return value");

        if (ar.returnValueSize == 0) {
            // do nothing
        } else if (ar.returnValueSize == 1) {
            f.addOperation(new Operation(OperationCode.PUTB, 0));
        } else if (ar.returnValueSize == 2) {
            f.addOperation(new Operation(OperationCode.PUTW, 0));
        }

        // put return address
        program.addComment("Save return address");
        // calculate the end of call later
        Operation returnAddressOperation = new Operation(OperationCode.PUTW, 0);
        f.addOperation(returnAddressOperation);

        /*
        // put current activation record address
        program.addComment("Save current activation record address");
        f.addOperation(new Operation(OperationCode.PUTSP, 0));
        // return value size and return address
        f.addOperation(new Operation(OperationCode.PUTW, ar.returnValueSize + 2));
        f.addOperation(new Operation(OperationCode.SUB, 0));
*/

        program.addComment("Save current activation record address");
        f.addOperation(new Operation(OperationCode.PUTARA, 0));

        program.addComment("Allocate space for local variables");
        // allocate activation record
        // first, allocate maximum allowed memory with 16-bit calls
        for (int i = 4 + ar.returnValueSize; i < ar.totalARsize; i += 2) {
            f.addOperation(new Operation(OperationCode.PUTW, 0));
        }
        // if we have non-16-bit-multiple size of AR, allocate 8 bit which are left
        if (ar.totalARsize % 2 == 1)
            f.addOperation(new Operation(OperationCode.PUTB, 0));

        // TODO: copy actual arguments to activation record


        // Update activation record pointer
        f.addOperation(new Operation(OperationCode.PUTSP, 0));
        f.addOperation(new Operation(OperationCode.PUTW, ar.totalARsize));
        f.addOperation(new Operation(OperationCode.SUB, 0));
        f.addOperation(new Operation(OperationCode.POPARA, 0));

        // subtract 1 from address since operation pointer will be incremented after instruction end
        program.addComment("Call of the '" + toCall.name + "'");
        f.addOperation(new Operation(OperationCode.JMP, program.getFunctionOffset(toCall) - 1));

        // now, we can update return address
        // -1 because after instruction end operation pointer will increment
        returnAddressOperation.data = program.getFunctionOffset(f) + f.getOperationCount() - 1;

        // we still have our return value left in stack after callee's activation record self-cleanup.
    }

    private void compileControlReturn(Function f) {
        ActivationRecord ar = f.activationRecord;

        program.addComment("Return control to caller");

        // remove all local variables from stack
        program.addComment("Cleanup local variables from stack");
        f.addOperation(new Operation(OperationCode.POP, ar.totalARsize - 4 - ar.returnValueSize));
        // restore activation record pointer
        program.addComment("Restore activation record address");
        f.addOperation(new Operation(OperationCode.POPARA, 0));
        // return control to caller
        program.addComment("Restore operation pointer");
        f.addOperation(new Operation(OperationCode.POPOPA, 0));
    }

    /**
     * Please, don't blame me for this shitcode!
     */
    private void compileTree(Function f, ExpressionTree expression) throws InvalidTokenException {
        switch (expression.operator.token) {
            case "==": {
                compileExpression(f, expression.left);
                compileExpression(f, expression.right);
                f.addOperation(new Operation(OperationCode.EQ, 0));

                break;
            }
            case "+": {
                compileExpression(f, expression.left);
                compileExpression(f, expression.right);
                f.addOperation(new Operation(OperationCode.ADD, 0));

                break;
            }
            case "-": {
                compileExpression(f, expression.left);
                compileExpression(f, expression.right);
                f.addOperation(new Operation(OperationCode.SUB, 0));

                break;
            }
            case "*": {
                compileExpression(f, expression.left);
                compileExpression(f, expression.right);
                f.addOperation(new Operation(OperationCode.MUL, 0));

                break;
            }
            case "/": {
                compileExpression(f, expression.left);
                compileExpression(f, expression.right);
                f.addOperation(new Operation(OperationCode.DIV, 0));

                break;
            }
            case "=": {
                if (!(expression.left instanceof ExpressionValue && ((ExpressionValue) expression.left).value.getType() instanceof TypeIdentifier))
                    throw new InvalidTokenException("Expected identifier on left side of '='");

                String varName = ((ExpressionValue) expression.left).value.value;
                int varOffset = f.activationRecord.getVarOffset(varName);

                program.addComment("Assignment");

                // compile address
                program.addComment("Left side (address to write to)");
                f.addOperation(new Operation(OperationCode.PUTARA, 0));
                f.addOperation(new Operation(OperationCode.PUTW, varOffset));
                f.addOperation(new Operation(OperationCode.ADD, 0));

                // compile value
                program.addComment("Right side");
                compileExpression(f, expression.right);

                // store value at the compiled address
                program.addComment("Write right side value from stack to left side");
                f.addOperation(new Operation(OperationCode.SW, 0));

                program.addComment("Load result back to stack so the tree parent can use it");
                f.addOperation(new Operation(OperationCode.PUTARA, 0));
                f.addOperation(new Operation(OperationCode.PUTW, varOffset));
                f.addOperation(new Operation(OperationCode.ADD, 0));
                f.addOperation(new Operation(OperationCode.LW, 0));

                f.addOperation(new Operation(OperationCode.PUTARA, 0));
                f.addOperation(new Operation(OperationCode.PUTW, varOffset));
                f.addOperation(new Operation(OperationCode.ADD, 0));
                f.addOperation(new Operation(OperationCode.LW, 0));
                f.addOperation(new Operation(OperationCode.PRINTWORD, 0));

                break;
            }
            case "++": {
                if (expression.right != null)
                    throw new InvalidTokenException("Right side of '++' is not empty. Are you sure you are doing right things?!");

                if (expression.left instanceof ExpressionValue && f.activationRecord.getVarSize(((ExpressionValue) expression.left).value.value) != 2)
                    throw new InvalidTokenException("For now only increment of 16-bit int is allowed");

                compileExpression(f, expression.left);
                f.addOperation(new Operation(OperationCode.PUTW, 1));
                f.addOperation(new Operation(OperationCode.ADD, 0));

                break;
            }
            case "--": {
                if (expression.right != null)
                    throw new InvalidTokenException("Right side of '--' is not empty. Are you sure you are doing right things?!");

                if (expression.left instanceof ExpressionValue && f.activationRecord.getVarSize(((ExpressionValue) expression.left).value.value) != 2)
                    throw new InvalidTokenException("For now only decrement of 16-bit int is allowed");

                compileExpression(f, expression.left);
                f.addOperation(new Operation(OperationCode.PUTW, 1));
                f.addOperation(new Operation(OperationCode.SUB, 0));

                break;
            }
            case "-=": {
//                compileExpression(f, expression.left);
//                compileExpression(f, expression.right);
//                f.addOperation(new Operation(OperationCode.EQ, 0));

                throw new InvalidTokenException("'-=' is not implemented yet");

//                break;
            }
            case "+=": {
//                compileExpression(f, expression.left);
//                compileExpression(f, expression.right);
//                f.addOperation(new Operation(OperationCode.EQ, 0));

                throw new InvalidTokenException("'+=' is not implemented yet");

//                break;
            }
            case "<": {
                compileExpression(f, expression.left);
                compileExpression(f, expression.right);
                f.addOperation(new Operation(OperationCode.LE, 0));

                break;
            }
            case "<=": {
                compileExpression(f, expression.left);
                compileExpression(f, expression.right);
                f.addOperation(new Operation(OperationCode.LEE, 0));

                break;
            }
            case ">=": {
                compileExpression(f, expression.left);
                compileExpression(f, expression.right);
                f.addOperation(new Operation(OperationCode.GRE, 0));

                break;
            }
            case ">": {
                compileExpression(f, expression.left);
                compileExpression(f, expression.right);
                f.addOperation(new Operation(OperationCode.GR, 0));

                break;
            }
            case "!=": {
                compileExpression(f, expression.left);
                compileExpression(f, expression.right);
                // compare two values
                f.addOperation(new Operation(OperationCode.EQ, 0));
                // negate result
                f.addOperation(new Operation(OperationCode.NOT, 0));

                break;
            }
        }
    }
}
