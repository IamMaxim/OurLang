package ru.iammaxim.ourlang.interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        File f = new File("src.olbc");
        FileInputStream fis = new FileInputStream(f);
        byte[] program = new byte[(int) f.length()];
        for (int i = 0; i < f.length(); i++) {
            program[i] = (byte) fis.read();
        }
        Interpreter.start(program);
    }
}
