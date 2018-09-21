package ru.iammaxim.ourlang.parser.expression;

import ru.iammaxim.ourlang.parser.InvalidTokenException;
import ru.iammaxim.ourlang.parser.value.Value;

/**
 * Created by maxim on 2/17/17 at 7:36 PM.
 */
public class ExpressionValue extends Expression {
    public Value value;
    private int size;

    @Override
    public String toString() {
        return value.toString();
    }

    public ExpressionValue(Value value) throws InvalidTokenException {
        this.value = value;
        size = value.getType().getSize();
    }

    /**
     * @return size of the value in bytes
     */
    public int getSize() {
        return size;
    }
}
