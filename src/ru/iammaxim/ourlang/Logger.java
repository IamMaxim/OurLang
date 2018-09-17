package ru.iammaxim.ourlang;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by maxim on 13.03.2017.
 */
public class Logger {
    private static final int indentFactor = 2;
    private int indent = 0;
    private static FileOutputStream log;

    public Logger(String path) throws FileNotFoundException {
        log = new FileOutputStream(path);
    }

    public void increateIndent() {
        indent++;
    }

    public void decreaseIndent() {
        indent--;
    }

    public void log(String s) {
        try {
            log.write((indent(indent * indentFactor) + s + "\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String indent(int count) {
        StringBuilder outputBuffer = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            outputBuffer.append(" ");
        }
        return outputBuffer.toString();
    }
}
