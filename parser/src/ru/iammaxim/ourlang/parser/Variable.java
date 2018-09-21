package ru.iammaxim.ourlang.parser;

import ru.iammaxim.ourlang.parser.type.Type;

public class Variable {
    public Type type;
    public String name;

    public Variable(Type type, String name) {
        this.type = type;
        this.name = name;
    }
}
