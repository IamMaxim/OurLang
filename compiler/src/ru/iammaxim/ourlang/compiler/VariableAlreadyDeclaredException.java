package ru.iammaxim.ourlang.compiler;

public class VariableAlreadyDeclaredException extends Exception {
    public VariableAlreadyDeclaredException(String s) {
        super(s);
    }
}
