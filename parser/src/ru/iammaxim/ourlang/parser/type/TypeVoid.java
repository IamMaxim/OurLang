package ru.iammaxim.ourlang.parser.type;

public class TypeVoid extends Type {
    @Override
    public String toString() {
        return "void";
    }

    public static boolean isValid(String s) {
        return s.equals("null");
    }
    
    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public int getValue(String value) {
        return 0;
    }
}
