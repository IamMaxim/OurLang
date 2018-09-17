package ru.iammaxim.ourlang.compiler;

import ru.iammaxim.ourlang.Logger;
import ru.iammaxim.ourlang.Parser.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws FileNotFoundException, InvalidTokenException, VariableAlreadyDeclaredException {
        Logger logger = new Logger("log.txt");

        Scanner scanner = new Scanner(new FileInputStream("src.ol")).useDelimiter("\\A");
        String src = scanner.next();

        ArrayList<Token> tokens = Lexer.lex(src);

        System.out.println(tokens);

        Parser parser = new Parser(logger, tokens);

        ArrayList<ParsedFunction> funcs = parser.parse();

        System.out.println("Parsing completed.");

        Compiler compiler = new Compiler();
        Program program = compiler.compile(funcs);

        ProgramPrinter.print(program);
    }
}
