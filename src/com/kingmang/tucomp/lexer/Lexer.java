package com.kingmang.tucomp.lexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;

public class Lexer {

    public int line = 1;
    private char peek = ' ';
    private Hashtable words = new Hashtable();
    private FileInputStream fileInputStream;

    public Lexer(File input) {
        try {
            fileInputStream = new FileInputStream(input);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        putKeyword(new Word(Tag.LITERAl, "true"));
        putKeyword(new Word(Tag.LITERAl, "false"));
        putKeyword(new Word(Tag.FUNC, "func"));
        putKeyword(new Word(Tag.TYPE, "Int"));
        putKeyword(new Word(Tag.TYPE, "Char"));
        putKeyword(new Word(Tag.TYPE, "String"));
        putKeyword(new Word(Tag.TYPE, "Boolean"));
        putKeyword(new Word(Tag.RETURN, "return"));
        putKeyword(new Word(Tag.WHILE, "while"));
        putKeyword(new Word(Tag.IF, "if"));
        putKeyword(new Word(Tag.REPEAT, "repeat"));
        putKeyword(new Word(Tag.VOID, "void"));
        putKeyword(new Word(Tag.PRINT, "print"));

    }

    private void putKeyword(Word t) {
        words.put(t.getLexeme(), t);
    }

    public Token tokenize() throws IOException {
        for (;; peek = nextChar()) {
            if (peek == ' ' || peek == '\t' || peek == '\r') ;
            else if (peek == '\n') line = line + 1;
            else break;
        }

        if (peek == '/') {
            peek = nextChar();
            char prev = peek;
            if (peek == '/') {
                for (; ; peek = nextChar()) {
                    if (peek == '\n' || peek == Tag.NULL) {
                        peek = nextChar();
                        line = line + 1;
                        return new Token(Tag.COMMENT);
                    }
                }
            } else if (peek == '*') {
                peek = nextChar();
                for (; ; peek = nextChar()) {
                    if (peek == '\n') line = line + 1;
                    else if (peek == '*') {
                        peek = nextChar();
                        if (peek == '/') {
                            peek = nextChar();
                            return new Token(Tag.COMMENT);
                        }
                    }
                    if (peek == Tag.NULL) {
                        return new Token(Tag.COMMENT);
                    }
                }
            } else {
                peek = prev;
                return new Token('/');
            }
        }

        if (Character.isDigit(peek)) {
            int result = 0;
            do {
                result = 10 * result + Character.digit(peek, 10);
                peek = nextChar();
            } while (Character.isDigit(peek));
            return new Num(result);
        }

        if (Character.isLetter(peek)) {
            StringBuilder builder = new StringBuilder();
            do {
                builder.append(peek);
                peek = nextChar();
            } while (Character.isLetterOrDigit(peek));

            String buffer = builder.toString();
            Word word = (Word) words.get(buffer);
            if (word != null) return word;
            word = new Word(Tag.ID, buffer);
            words.put(buffer, word);
            return word;
        }

        if (peek == ':') {
            char prev = peek;
            peek = nextChar();
            if (peek == '=') {
                peek = nextChar();
                return new Token(Tag.ASSIGN);
            } else {
                peek = nextChar();
                return new Token(Tag.DOUBLEDOT);
            }
        }

        if (peek == '<') {
            peek = nextChar();
            if (peek == '>') {
                peek = nextChar();
                return new Operator("<>");
            } else if (peek == '=') {
                peek = nextChar();
                return new Operator("<=");
            } else {
                return new Operator("<");
            }
        }

        if (peek == '>') {
            peek = nextChar();
            if (peek == '=') {
                peek = nextChar();
                return new Operator(">=");
            } else {
                return new Operator(">");
            }
        }

        if (peek == '=') {
            peek = nextChar();
            return new Operator("==");
        }
        if (peek == '}') {
            peek = nextChar();
            return new Token(Tag.END);
        }
        if(peek == '{'){
            peek = nextChar();
            return new Token(Tag.BEGIN);
        }


        if (peek == '"') {
            StringBuilder builder = new StringBuilder();
            peek = nextChar();
            do {
                builder.append(peek);
                peek = nextChar();
            } while (peek != '"' && peek != '\n' && peek != Tag.NULL);
            peek = nextChar();
            return new Literal(builder.toString());
        }

        Token result = new Token(peek);
        peek = ' ';
        return result;
    }

    private char nextChar() throws IOException {
        return (char) fileInputStream.read();
    }
}
