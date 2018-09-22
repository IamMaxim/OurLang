package ru.iammaxim.ourlang.compiler;

import ru.iammaxim.ourlang.Logger;
import ru.iammaxim.ourlang.parser.InvalidTokenException;
import ru.iammaxim.ourlang.parser.ParsedFunction;
import ru.iammaxim.ourlang.parser.expression.*;
import ru.iammaxim.ourlang.parser.type.TypeIdentifier;

import java.util.ArrayList;

public class Compiler {
    Program program = new Program();
    private Logger logger;

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

            Function toCall = program.getFunction(exp.functionName);

            if (toCall == null) {
                throw new InvalidTokenException("Function '" + exp.functionName + "' is not declared");
            }

            ActivationRecord ar = toCall.activationRecord;

            // TODO: put activation record into the stack here

            // TODO: pass control to the callee

            // TODO: if return type in non-void, read the return value and put it into stack
            // this can be done by simply omitting the rest of the activation record, since return value is in
            // the very beginning. Just move stack pointer to the end of return value.

            throw new InvalidTokenException("Function Call is not implemented yet!");
        } else if (expression instanceof ExpressionReturn) {
            ExpressionReturn exp = (ExpressionReturn) expression;

            // compile address to store return value
            program.addComment("Return value address");
            f.addOperation(new Operation(OperationCode.PUTARA, 0));
            f.addOperation(new Operation(OperationCode.PUTW, f.activationRecord.getReturnValueOffset()));
            f.addOperation(new Operation(OperationCode.ADD, 0));

            // compile expression that will generate return value
            program.addComment("Return value");
            compileExpression(f, exp.returnExp);
            // copy that value from stack to activation record
            // TODO: implement non-word return value
            program.addComment("Move return value from stack to activation record");
            f.addOperation(new Operation(OperationCode.SW, 0));
            program.addComment("End of return");
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
            // pop the last pushed value, since we are out of tree and not about to use it.
            program.addComment("Pop tree's return value from stack, since it won't be used");
            f.addOperation(new Operation(OperationCode.POP, f.activationRecord.getVarSize(((ExpressionValue) exp.left).value.value)));
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
            // TODO: implement this
        } else
            throw new InvalidTokenException("Unexpected " + expression.getClass().getSimpleName() + " while compiling expression");
    }

    private void compileFunctionCall() throws InvalidTokenException {
        throw new InvalidTokenException("Function call is not implemented yet");
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
                int offset = f.activationRecord.getVarOffset(varName);

                // compile address
                f.addOperation(new Operation(OperationCode.PUTARA, 0));
                f.addOperation(new Operation(OperationCode.PUTW, offset));
                f.addOperation(new Operation(OperationCode.ADD, 0));

                // compile value
                compileExpression(f, expression.right);

                // store value at the compiled address
                f.addOperation(new Operation(OperationCode.SW, 0));

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
