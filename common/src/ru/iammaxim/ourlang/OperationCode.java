package ru.iammaxim.ourlang;

public class OperationCode {
    public static final int ADD = 0;
    public static final int SUB = 1;
    public static final int MUL = 2;
    public static final int DIV = 3;
    public static final int AND = 4;
    public static final int OR = 5;
    public static final int XOR = 6;
    public static final int NOT = 7;
    public static final int JMP = 8;
    public static final int JIF = 9;
    public static final int INC = 10;
    public static final int DEC = 11;
    public static final int EQ = 12;
    public static final int LE = 13;
    public static final int LEE = 14;
    public static final int GR = 15;
    public static final int GRE = 16;
    public static final int PUTB = 17;
    public static final int PUTW = 18;
    public static final int POP = 19;
    public static final int SB = 20;
    public static final int SW = 21;
    public static final int LB = 22;
    public static final int LW = 23;
    public static final int PUTARA = 24;
    public static final int PUTOPA = 25;
    public static final int POPARA = 26;
    public static final int POPOPA = 27;
    public static final int STOP = 28;
    public static final int PUTSP = 29;

    // debug interpreter-only instructions
    public static final int PRINTBYTE = 62;
    public static final int PRINTWORD = 63;

    public static int getByName(String instr) {
        switch (instr.toUpperCase()) {
            case "ADD":
                return ADD;
            case "SUB":
                return SUB;
            case "MUL":
                return MUL;
            case "DIV":
                return DIV;
            case "AND":
                return AND;
            case "OR":
                return OR;
            case "XOR":
                return XOR;
            case "NOT":
                return NOT;
            case "JMP":
                return JMP;
            case "JIF":
                return JIF;
            case "INC":
                return INC;
            case "DEC":
                return DEC;
            case "EQ":
                return EQ;
            case "LE":
                return LE;
            case "LEE":
                return LEE;
            case "GR":
                return GR;
            case "GRE":
                return GRE;
            case "PUTB":
                return PUTB;
            case "PUTW":
                return PUTW;
            case "POP":
                return POP;
            case "SB":
                return SB;
            case "SW":
                return SW;
            case "LB":
                return LB;
            case "LW":
                return LW;
            case "PUTARA":
                return PUTARA;
            case "PUTOPA":
                return PUTOPA;
            case "POPARA":
                return POPARA;
            case "POPOPA":
                return POPOPA;
            case "STOP":
                return STOP;
            case "PUTSP":
                return PUTSP;

            case "PRINTBYTE":
                return PRINTBYTE;
            case "PRINTWORD":
                return PRINTWORD;

            default:
                throw new IllegalArgumentException("Instruction \"" + instr + "\" not found");
        }
    }
}
