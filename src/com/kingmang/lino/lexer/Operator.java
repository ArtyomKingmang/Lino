package com.kingmang.lino.lexer;

public class Operator extends Token {
    public final String value;

    public Operator(String value) {
        super(TokenType.COMPARATOR);
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
