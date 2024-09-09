package com.kingmang.lino.lexer;

public class Word extends Token {
    private final String lexeme;

    public Word(int tag, String lexeme) {
        super(tag);
        this.lexeme = lexeme;
    }

    @Override
    public String toString() {
        return "Word: " + lexeme
                + " TokenType: " + tag;
    }
    public String getLexeme(){
        return lexeme;
    }

    @Override
    public String stringify() {
        return lexeme;
    }
}
