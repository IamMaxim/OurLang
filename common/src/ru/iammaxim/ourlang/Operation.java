package ru.iammaxim.ourlang;

public class Operation {
    public int code; // 6-bit code of operation
    public int data; // 26-bit data of the operation

    public Operation(int code, int data) {
        this.code = code;
        this.data = data;
    }

    public int toBinaryCode() {
        int n = ((code & 0b111111) << 26) | (data & 0x03FFFFFF);
//        System.out.println(
//                ((code & 0b111111)) + " " + (data & 0x03FFFFFF) + " | " +
//                        fromBinaryCode(n).code + " " + fromBinaryCode(n).data + " | " +
//                        Integer.toBinaryString(n));
        return n;
    }

    public static Operation fromBinaryCode(int n) {
        int code = (n >> 26) & 0b111111;
        int data = n & 0x03FFFFFF;
//        System.out.println("    " + Integer.toBinaryString(n) + " " + code + " " + data);
        return new Operation(code, data);
    }
}
