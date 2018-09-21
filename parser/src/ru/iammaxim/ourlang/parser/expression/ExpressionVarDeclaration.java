package ru.iammaxim.ourlang.parser.expression;

import ru.iammaxim.ourlang.parser.type.Type;

public class ExpressionVarDeclaration extends Expression {
    public Type type;
    public String name;

    public ExpressionVarDeclaration(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String toString() {
        return "var decl: " + name + ": " + type;
    }
}
