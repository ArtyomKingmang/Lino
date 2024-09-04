package com.kingmang.tucomp.lexer;

public class Operator extends Token {
    public final String value;

    public Operator(String value) {
        super(Tag.COMPARATOR);
        this.value = value;
    }

    @Override
    public String toString() {
        return "Operator: " + value;
    }

    @Override
    public String stringify() {
        return value;
    }
}
