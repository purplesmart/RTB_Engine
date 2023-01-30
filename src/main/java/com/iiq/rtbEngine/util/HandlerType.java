package com.iiq.rtbEngine.util;

public enum HandlerType {

    ProfileAttribute(0), BID(1);

    private int numVal;

    HandlerType(int numVal) {
        this.numVal = numVal;
    }

    public int getNumVal() {
        return numVal;
    }
}
