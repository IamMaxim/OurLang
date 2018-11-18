// single-line comments

/*
* Multi-line comments
*/ 

function print(int value) {
    instr(putara, 0);
    instr(putw, 4);
    instr(add, 0);
    instr(lw, 0);
    instr(printword, 0);
}

function func1(int arg1, int arg2): int {
    print(arg1);
    print(arg2);

    if (arg1 == 0) {
        return arg2;
    } else {
        return func1(arg1 - 1, arg2);
    };
}

function main() {
    var i: int;

    i = func1(1, 10);

    if (i == 0) {
        i = 1;
    } else {
        i = 2;
    };
    print(i);
}
