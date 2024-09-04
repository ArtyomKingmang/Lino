package com.kingmang.tucomp.parser;

import com.kingmang.tucomp.lexer.Lexer;
import com.kingmang.tucomp.lexer.Tag;
import com.kingmang.tucomp.lexer.Token;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Parser {

    private StringBuilder arduinoBuffer;
    private StringBuilder console;
    private ArrayList<Integer> errors = new ArrayList<>();
    private Lexer lexer;
    private Token token;
    private int line;

    public Parser(){
        arduinoBuffer = new StringBuilder();
        console = new StringBuilder();
        lexer = new Lexer(new File("test.txt"));
    }

    private void skipErrors(Token expected) throws IOException {
        if(!isEndToken(expected) && !isEndToken(token)){
            do{
                nextToken();
            }while(!isEndToken(token));
        }
    }
    private void nextToken() throws IOException {
        do{
            token = lexer.tokenize();
        }while (token.tag == Tag.COMMENT);
        appendLineToConsole(token.toString(), false);
    }
    private boolean isEndToken(Token token){
        return
                token.tag == ';' ||
                token.tag == Tag.NULL ||
                token.tag == Tag.END ||
                token.tag == Tag.RETURN;
    }
    private void appendLineToConsole(String string, boolean isError){
        console.append(string).append("\n");
        if(isError){
            errors.add(this.line);
        }
        this.line++;
    }
}
