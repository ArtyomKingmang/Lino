package com.kingmang.lino.lexer;

public class Token {
    public final int tag;

    public Token(int tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "Token: " + (char) tag;
    }

    public String stringify() {
        if (tag == TokenType.ASSIGN)
            return "=";
        return String.valueOf((char) tag);
    }
}
