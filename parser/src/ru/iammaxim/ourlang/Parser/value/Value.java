package ru.iammaxim.ourlang.Parser.value;

import ru.iammaxim.ourlang.Parser.InvalidTokenException;
import ru.iammaxim.ourlang.Parser.type.Type;
import ru.iammaxim.ourlang.Parser.type.TypeInt;
import ru.iammaxim.ourlang.Parser.type.TypeVoid;

public class Value {
    public String value;
    private Type type;

    public Value(String value) throws InvalidTokenException {
        this.value = value;
        this.type = getType();
    }

    @Override
    public String toString() {
        return "val:'" + value + "'";
    }

    public Type getType() throws InvalidTokenException {
        if (TypeInt.isValid(value))
            return new TypeInt();
        else if (TypeVoid.isValid(value))
            return new TypeVoid();
        else
            throw new InvalidTokenException("Unknown type for value '" + value + "'");
    }

    public int getValue() {
        return type.getValue(value);
    }
}
