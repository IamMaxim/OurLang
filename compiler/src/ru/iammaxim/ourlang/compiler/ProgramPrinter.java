package ru.iammaxim.ourlang.compiler;

import ru.iammaxim.ourlang.Operation;
import ru.iammaxim.ourlang.OperationCode;

import java.util.ArrayList;

public class ProgramPrinter {
    public static void print(Program program) {
        for (int i = 0; i < program.functions.size(); i++) {
            Function function = program.functions.get(i);
            System.out.println("Function " + function.name + ":");

            int offset = program.getFunctionOffset(function);

            for (int j = 0; j < function.getOperations().size(); j++) {
                Operation operation = function.getOperations().get(j);
                if (i > 0 && j > 0 || i == 0) {
                    ArrayList<String> comments = program.comments.get(offset);
                    if (comments != null) {
                        comments.forEach(c -> System.out.println("// " + c));
                    }
                }

                printOp(offset, operation);
                offset += 1;
            }

            ArrayList<String> comments = program.comments.get(offset);
            if (comments != null) {
                comments.forEach(c -> System.out.println("// " + c));
            }
        }
    }

    private static void println(String s) {
        System.out.println(s);
    }

    private static void printOp(int addr, Operation operation) {
        switch (operation.code) {
            case OperationCode.ADD:
                println(formatOperationAddress(addr) + "add    ");
                break;
            case OperationCode.SUB:
                println(formatOperationAddress(addr) + "sub    ");
                break;
            case OperationCode.MUL:
                println(formatOperationAddress(addr) + "mul    ");
                break;
            case OperationCode.DIV:
                println(formatOperationAddress(addr) + "div    ");
                break;
            case OperationCode.AND:
                println(formatOperationAddress(addr) + "and    ");
                break;
            case OperationCode.OR:
                println(formatOperationAddress(addr) + "or     ");
                break;
            case OperationCode.XOR:
                println(formatOperationAddress(addr) + "xor    ");
                break;
            case OperationCode.NOT:
                println(formatOperationAddress(addr) + "not    ");
                break;
            case OperationCode.JMP:
                println(formatOperationAddress(addr) + "jmp    " + formatAddr(operation.data));
                break;
            case OperationCode.JIF:
                println(formatOperationAddress(addr) + "jif    " + formatAddr(operation.data));
                break;
            case OperationCode.INC:
                println(formatOperationAddress(addr) + "inc    ");
                break;
            case OperationCode.DEC:
                println(formatOperationAddress(addr) + "dec    ");
                break;
            case OperationCode.EQ:
                println(formatOperationAddress(addr) + "eq     ");
                break;
            case OperationCode.LE:
                println(formatOperationAddress(addr) + "le     ");
                break;
            case OperationCode.LEE:
                println(formatOperationAddress(addr) + "lee    ");
                break;
            case OperationCode.GR:
                println(formatOperationAddress(addr) + "gr     ");
                break;
            case OperationCode.GRE:
                println(formatOperationAddress(addr) + "gre    ");
                break;
            case OperationCode.PUTB:
                println(formatOperationAddress(addr) + "putb   " + formatValue(operation.data));
                break;
            case OperationCode.PUTW:
                println(formatOperationAddress(addr) + "putw   " + formatValue(operation.data));
                break;
            case OperationCode.POP:
                println(formatOperationAddress(addr) + "pop    ");
                break;
            case OperationCode.SB:
                println(formatOperationAddress(addr) + "sb     ");
                break;
            case OperationCode.SW:
                println(formatOperationAddress(addr) + "sw     ");
                break;
            case OperationCode.LB:
                println(formatOperationAddress(addr) + "lb     ");
                break;
            case OperationCode.LW:
                println(formatOperationAddress(addr) + "lw     ");
                break;
            case OperationCode.PUTARA:
                println(formatOperationAddress(addr) + "putara ");
                break;
        }
    }

    private static String formatOperationAddress(int addr) {
        StringBuilder s = new StringBuilder(Integer.toString(addr));
        while (s.length() < 8)
            s.append(" ");
        return s.toString();
    }

    private static String formatValue(int value) {
        return Integer.toString(value);
    }

    private static String formatAddr(int addr) {
        return Integer.toString(addr);
    }
}
