package ru.iammaxim.ourlang.Parser.expression;

public class ExpressionValueAt extends Expression {
    public String value;
    public ExpressionValue key;

    public ExpressionValueAt(String value, ExpressionValue key) {
        this.value = value;
        this.key = key;
    }

    @Override
    public String toString() {
        return value + "[" + key + "]";
    }
}
