package ru.iammaxim.ourlang.Parser.expression;

import java.util.ArrayList;

/**
 * Created by maxim on 14.03.2017.
 */
public class ExpressionScope extends Expression {
    public ArrayList<Expression> exps;

    public ExpressionScope(ArrayList<Expression> exps) {
        this.exps = exps;
    }
}
