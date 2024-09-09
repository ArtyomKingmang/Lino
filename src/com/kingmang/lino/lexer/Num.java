package com.kingmang.lino.lexer;

public class Num extends Token {
    public final int value;

    public Num(int value) {
        super(TokenType.NUM);
        this.value = value;
    }

    @Override
    public String toString() {
        return "Number: " + value;
    }

    @Override
    public String stringify() {
        return String.valueOf(value);
    }
}
