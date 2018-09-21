package ru.iammaxim.ourlang.parser;


import ru.iammaxim.ourlang.parser.expression.Expression;
import ru.iammaxim.ourlang.parser.type.Type;

import java.util.ArrayList;

/**
 * Created by maxim on 2/17/17 at 7:27 PM.
 */
public class ParsedFunction {
    public Type type;
    public String name;
    public ArrayList<Variable> args;
    public ArrayList<Expression> expressions;
    public ArrayList<Variable> localVars;

    public ParsedFunction(Type type, String name, ArrayList<Variable> args, ArrayList<Expression> expressions, ArrayList<Variable> localVars) {
        this.type = type;
        this.name = name;
        this.args = args;
        this.expressions = expressions;
        this.localVars = localVars;
    }
}
