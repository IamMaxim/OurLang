package ru.iammaxim.ourlang.compiler;

import ru.iammaxim.ourlang.parser.InvalidTokenException;
import ru.iammaxim.ourlang.parser.ParsedFunction;
import ru.iammaxim.ourlang.parser.Token;
import ru.iammaxim.ourlang.parser.expression.*;

import java.util.ArrayList;

public class Compiler {
    Program program = new Program();

    public Program compile(ArrayList<ParsedFunction> functions) throws VariableAlreadyDeclaredException, InvalidTokenException {
        for (int i = 0; i < functions.size(); i++) {
            ParsedFunction pf = functions.get(i);
            Function f = new Function(pf);
            // we need to add function to program before actually compiling it
            // in order to be able to recursively execute it
            program.addFunction(f);

            for (int j = 0; j < pf.expressions.size(); j++) {
                Expression expression = pf.expressions.get(j);
                compileStatement(f, expression);
            }
        }

        return program;
    }

    private void compileStatement(Function f, Expression expression) throws InvalidTokenException {
        if (expression instanceof ExpressionCondition) {
            ExpressionCondition exp = (ExpressionCondition) expression;

            // compile condition
            compileExpression(f, exp.cond);

            // this operation will jump to the elseBody (or the end of loop if there is no elseBody) if condition is false
            // we will set jump address later, after compilation of body
            Operation jmpOverBody = new Operation(OperationCode.JMP, -1);
            f.addOperation(jmpOverBody);


            // compile body statement-by-statement
            for (Expression expression1 : exp.body) {
                compileStatement(f, expression1);
            }

            Operation jmpOverElseBody = new Operation(OperationCode.JMP, -1);

            // if elseBody is not empty, then jump over it after body is completed
            if (exp.elseBody.size() != 0) {
                f.addOperation(jmpOverElseBody);
            }

            // this label indicates start of else body
            int elseBodyStartLabel = f.getOperationCount();
            // jump here if condition is false
            jmpOverBody.data = elseBodyStartLabel;

            if (exp.elseBody.size() != 0) {
                // add jump over else body if it exists
                f.addOperation(jmpOverElseBody);

                // compile elseBody statement-by-statement
                for (Expression expression1 : exp.elseBody) {
                    compileStatement(f, expression1);
                }

                // set address to jump in case when body completed execution and there is elseBody that we need to skip
                int endOfElseBody = f.getOperationCount();
                jmpOverElseBody.data = endOfElseBody;
            }
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


            // compile expression that will generate return value
            compileExpression(f, exp.returnExp);
            // copy that value from stack to activation record
            // TODO: implement non-word return value
            f.addOperation(new Operation(OperationCode.SW, f.activationRecord.getReturnValueOffset()));
        } else if (expression instanceof ExpressionTree) {
            ExpressionTree exp = (ExpressionTree) expression;

            throw new InvalidTokenException("Tree is not implemented yet!");
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

            int valueSize = exp.getSize();

            /*if (valueSize == 1) { // 8-bit value
                f.addOperation(new Operation(OperationCode.SB, exp.value.getValue()));
            } else*/
            if (valueSize == 2) { // 16-bit value
                f.addOperation(new Operation(OperationCode.PUTW, exp.value.getValue()));
            } else { // something bigger. We can't just put it into stack with one instruction, so let's put it into
                // data part of our program
                // TODO: implement this
                throw new IllegalStateException("Values with not 2 bytes size are not implemented yet");
            }

        } else if (expression instanceof ExpressionTree) {
            ExpressionTree exp = (ExpressionTree) expression;

            switch (exp.operator.token) {
                // TODO: implement all operators here
                case "+":
                    compileExpression(f, exp.left);
                    compileExpression(f, exp.right);
                    f.addOperation(new Operation(OperationCode.ADD, 0));
                case "-":
                    f.addOperation(new Operation(OperationCode.ADD, 0));
                case "*":
                    f.addOperation(new Operation(OperationCode.ADD, 0));
                case "/":
                    f.addOperation(new Operation(OperationCode.ADD, 0));
                case "=":
                    f.addOperation(new Operation(OperationCode.ADD, 0));
                case "++":
                    f.addOperation(new Operation(OperationCode.ADD, 0));
                case "--":
                    f.addOperation(new Operation(OperationCode.ADD, 0));
                case "-=":
                    f.addOperation(new Operation(OperationCode.ADD, 0));
                case "+=":
                    f.addOperation(new Operation(OperationCode.ADD, 0));
                case "<":
                    f.addOperation(new Operation(OperationCode.ADD, 0));
                case "<=":
                    f.addOperation(new Operation(OperationCode.ADD, 0));
                case "==":
                    f.addOperation(new Operation(OperationCode.ADD, 0));
                case ">=":
                    f.addOperation(new Operation(OperationCode.ADD, 0));
                case ">":
                    f.addOperation(new Operation(OperationCode.ADD, 0));
                case "!=":
                    f.addOperation(new Operation(OperationCode.ADD, 0));
                default:
                    throw new InvalidTokenException("Unknown operator '" + exp.operator + "' met");
            }
        } else
            throw new InvalidTokenException("Unexpected " + expression.getClass().getSimpleName() + " while compiling expression");
    }

    private void compileFunctionCall() {

    }

    private void compileTree(Function f, Expression expression) throws InvalidTokenException {
        throw new InvalidTokenException("Tree is not implemented yet!");
    }
}
