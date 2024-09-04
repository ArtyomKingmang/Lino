package com.kingmang.tucomp.lexer;

public class Literal extends Token{
    public final String value;

    public Literal(String value) {
        super(Tag.LITERAl);
        this.value = value;
    }

    @Override
    public String toString() {
        return "Literal String: " + '"'+ value+ '"';
    }

    @Override
    public String stringify() {
        return '"'+ value+ '"';
    }
}
