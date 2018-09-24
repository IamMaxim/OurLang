package ru.iammaxim.ourlang.compiler;

import java.util.ArrayList;
import java.util.HashMap;

public class Program {
    // compiled functions
    public ArrayList<Function> functions = new ArrayList<>();
    // map for fast getting of functions
    public HashMap<String, Function> functionMap = new HashMap<>();
    // offsets of functions in the compiled program
    public ArrayList<Integer> functionOffsets = new ArrayList<>();
    // comments that can be used while debugging
    public HashMap<Integer, ArrayList<String>> comments = new HashMap<>();

    /**
     * @param f function to get offset for
     * @return offset of function's first operation from program's first operation in the compiled program
     */
    public int getFunctionOffset(Function f) {
        return functionOffsets.get(functions.indexOf(f));
    }

    public void addFunction(Function f) {
        if (functions.size() > 0)
            functionOffsets.add(functionOffsets.get(functionOffsets.size() - 1) + functions.get(functions.size() - 1).getOperationCount());
        else
            functionOffsets.add(1);

        functions.add(f);
        functionMap.put(f.name, f);
    }


    public Function getFunction(String name) {
        return functionMap.get(name);
    }

    public void addComment(String comment) {
        int pos = 0;

        if (functions.size() > 0) {
            int index = functions.size() - 1;
            pos = functionOffsets.get(index);
            pos += functions.get(index).getOperationCount();
        }

        if (comments.get(pos) == null)
            comments.put(pos, new ArrayList<>());
        comments.get(pos).add(comment);
    }
}
