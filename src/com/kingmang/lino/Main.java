package com.kingmang.lino;


import com.kingmang.lino.lexer.Lexer;
import com.kingmang.lino.lexer.Token;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        File file = new File("input.lino");
        Lexer lexer = new Lexer(file);
        Token token = lexer.tokenize();
        System.out.println(token);
    }
}