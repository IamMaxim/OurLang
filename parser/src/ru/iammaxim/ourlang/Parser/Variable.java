package ru.iammaxim.ourlang.Parser;

import ru.iammaxim.ourlang.Parser.type.Type;

public class Variable {
    public Type type;
    public String name;

    public Variable(Type type, String name) {
        this.type = type;
        this.name = name;
    }
}
