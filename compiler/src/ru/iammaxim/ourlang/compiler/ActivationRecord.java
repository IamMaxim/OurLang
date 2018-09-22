package ru.iammaxim.ourlang.compiler;

import java.util.HashMap;


public class ActivationRecord {
    public int returnValueSize;
    private HashMap<String, Integer> localVarOffsets = new HashMap<>();
    private HashMap<String, Integer> localVarSizes = new HashMap<>();
    // 32 bits for return address
    // and 32 bits for previous activation record are already reserved
    public int totalARsize = 8;

    public ActivationRecord(int returnValueSize) {
        this.returnValueSize = returnValueSize;
        totalARsize += returnValueSize;
    }

    /**
     * @return true if this function has no return value
     */
    public boolean isVoid() {
        return returnValueSize == 0;
    }

    /**
     * @param name name of variable
     * @param size size of variable in the memory
     * @throws VariableAlreadyDeclaredException if variable with the same name is already declared in the function.
     */
    public void addVariable(String name, int size) throws VariableAlreadyDeclaredException {
        if (localVarOffsets.containsKey(name))
            throw new VariableAlreadyDeclaredException("Variable '" + name + "' is already defined in this scope");

        localVarSizes.put(name, size);
        localVarOffsets.put(name, totalARsize);
        totalARsize += size;
    }

    /**
     * @return offset of return value relative to start of activation record
     */
    public int getReturnValueOffset() {
        return 8;
    }

    /**
     * @param name name of the variable
     * @return offset of the variable relative to AR start
     */
    public int getVarOffset(String name) {
        return localVarOffsets.get(name);
    }

    /**
     * @param name name of the variable
     * @return size of requested variable
     */
    public int getVarSize(String name) {
        return localVarSizes.get(name);
    }
}
