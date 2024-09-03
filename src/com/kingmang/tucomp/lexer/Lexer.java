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

    public Lexer(File input){
        try{
            fileInputStream = new FileInputStream(input);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }

        put(new Word(Tag.LITERAL, "true"));
        put(new Word(Tag.LITERAL, "false"));
        put(new Word(Tag.FUNC, "func"));
        put(new Word(Tag.TYPE, "Int"));
        put(new Word(Tag.TYPE, "Char"));
        put(new Word(Tag.TYPE, "String"));
        put(new Word(Tag.TYPE, "Boolean"));
        put(new Word(Tag.RETURN, "return"));
        put(new Word(Tag.WHILE, "while"));
        put(new Word(Tag.IF, "if"));
        put(new Word(Tag.REPEAT, "repeat"));
        put(new Word(Tag.VOID, "void"));
        put(new Word(Tag.PRINT, "print"));

    }

    public Token tokenize() throws IOException{
        for(;; peek = nextChar()){
          if(peek == ' ' || peek == '\t' || peek == '\r');
          else if (peek == '\n') line += 1;
          else break;
        }

        if (peek == '/') {
            peek = nextChar();
            char prev = peek;
            if (peek == '/') {
                for (; ; peek = nextChar()) {
                    if (peek == '\n' || peek == Tag.NULL) {
                        peek = nextChar();
                        line += 1;
                        return new Token(Tag.COMMENT);
                    }
                }


            } else if (peek == '*') {
                peek = nextChar();
                for (; ; peek = nextChar()) {
                    if (peek == '\n') line += 1;
                    else if (peek == '*') {
                        peek = nextChar();
                        if (peek == '/') {
                            peek = nextChar();
                            return new Token(Tag.COMMENT);
                        }
                    }
                    if (peek == Tag.NULL) return new Token(Tag.COMMENT);
                }
            } else {
                peek = prev;
                return new Token('/');
            }
        }
        if(Character.isDigit(peek)){
            int v = 0;
            do{
                v = 10 * v + Character.digit(peek, 10);
                peek = nextChar();
            }while(Character.isDigit(peek));
            return new Num(v);
        }
        if(Character.isLetter(peek)){
            StringBuilder buffer = new StringBuilder();
            do{
                buffer.append(peek);
                peek = nextChar();
            }while(Character.isLetterOrDigit(peek));

            String result = buffer.toString();
            Word word = (Word) words.get(result);
            if(word != null) return word;
            word = new Word(Tag.ID, result);
            words.put(result, word);
            return word;
        }
        if(peek == ':'){
            char prev = peek;
            peek = nextChar();
            if(peek == '='){
                peek = nextChar();
                return new Token(Tag.ASSIGN);
            }else{
                peek = nextChar();
                return new Token(Tag.DOUBLEDOT);
            }
        }
        if(peek == '<'){
            peek = nextChar();
            if(peek == '>'){
                peek = nextChar();
                return new Operator("<>");
            } else if (peek == '=') {
                peek = nextChar();
                return new Operator("<=");
            }else{
                return new Operator("<");
            }
        }

        if(peek == '>'){
            peek = nextChar();
            if (peek == '=') {
                peek = nextChar();
                return new Operator(">=");
            }else{
                return new Operator(">");
            }
        }
        if(peek == '='){
            peek = nextChar();
            return new Operator("==");
        }
        if(peek == '}'){
            peek = nextChar();
            return new Token(Tag.END);
        }
        if(peek == '{'){
            peek = nextChar();
            return new Token(Tag.BEGIN);
        }
        if(peek == '"'){
            StringBuilder buffer = new StringBuilder();
            peek = nextChar();
            do{
                buffer.append(peek);
                peek = nextChar();
            }while(peek != '"' && peek != '\n' && peek != Tag.NULL);

            peek = nextChar();
            return new Literal(buffer.toString());
        }

        Token token = new Token(peek);
        peek = ' ';
        return token;
        //return new Token(1);
    }

    private void put(Word word){
        words.put(word.getLexeme(), word);
    }

    private char nextChar() throws IOException {
        return (char) fileInputStream.read();
    }
}
