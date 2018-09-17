package ru.iammaxim.ourlang.Parser.type;

/**
 * int is 16-bit numeric value
 */
public class TypeInt extends Type {
    @Override
    public String toString() {
        return "int";
    }

    public static boolean isValid(String s) {
        return s.matches("[0-9]*");
    }

    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public int getValue(String value) {
        return Integer.parseInt(value);
    }
}
