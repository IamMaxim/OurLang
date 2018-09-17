package ru.iammaxim.ourlang.compiler;

public class Operation {
    public int code; // 6-bit code of operation
    public int data; // 26-bit data of the operation

    public Operation(int code, int data) {
        this.code = code;
        this.data = data;
    }

    public int toBinaryCode() {
        return ((code & 0b111111) << 26) | (data & 0x03FFFFFF);
    }

    public static Operation fromBinaryCode(int n) {
        int code = (n >> 26) & 0b111111;
        int data = n & 0x03FFFFFF;
        return new Operation(code, data);
    }
}
