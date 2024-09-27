package com.kingmang.lino.lexer;

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
        putKeyword(new Word(TokenType.LITERAl, "true"));
        putKeyword(new Word(TokenType.LITERAl, "false"));
        putKeyword(new Word(TokenType.FUNC, "func"));
        putKeyword(new Word(TokenType.TYPE, "Int"));
        putKeyword(new Word(TokenType.TYPE, "Char"));
        putKeyword(new Word(TokenType.TYPE, "String"));
        putKeyword(new Word(TokenType.TYPE, "Boolean"));
        putKeyword(new Word(TokenType.RETURN, "return"));
        putKeyword(new Word(TokenType.WHILE, "while"));
        putKeyword(new Word(TokenType.IF, "if"));
        putKeyword(new Word(TokenType.REPEAT, "repeat"));
        putKeyword(new Word(TokenType.VOID, "void"));
        putKeyword(new Word(TokenType.PRINT, "print"));
        //putKeyword(new Word(TokenType.SBEGIN, "sbegin"));
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
                    if (peek == '\n' || peek == TokenType.NULL) {
                        peek = nextChar();
                        line = line + 1;
                        return new Token(TokenType.COMMENT);
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
                            return new Token(TokenType.COMMENT);
                        }
                    }
                    if (peek == TokenType.NULL) {
                        return new Token(TokenType.COMMENT);
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
            word = new Word(TokenType.ID, buffer);
            words.put(buffer, word);
            return word;
        }

        if (peek == ':') {
            peek = nextChar();
            return new Token(TokenType.COLON);
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
            if (peek == '=') {
                peek = nextChar();
                return new Operator("==");
            }else {
                peek = nextChar();
                return new Token(TokenType.ASSIGN);
            }
        }
        if (peek == '}') {
            peek = nextChar();
            return new Token(TokenType.RBRACE);
        }
        if(peek == '{'){
            peek = nextChar();
            return new Token(TokenType.LBRACE);
        }


        if (peek == '"') {
            StringBuilder builder = new StringBuilder();
            peek = nextChar();
            do {
                builder.append(peek);
                peek = nextChar();
            } while (peek != '"' && peek != '\n' && peek != TokenType.NULL);
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
