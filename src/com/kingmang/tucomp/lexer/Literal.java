package com.kingmang.tucomp.lexer;

public class Literal extends Token{

    final String value;

    public Literal(String value) {
        super(Tag.LITERAL);
        this.value = value;
    }
    @Override
    public String stringify(){
        return '"' + value + '"';
    }
    @Override
    public String toString(){
        return "Literal String: " + '"' + value + '"';
    }

}
