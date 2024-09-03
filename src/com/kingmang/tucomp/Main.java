package com.kingmang.tucomp;


import com.kingmang.tucomp.lexer.Lexer;
import com.kingmang.tucomp.lexer.Token;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        File file = new File("test.txt");
        Lexer lexer = new Lexer(file);
        Token token = lexer.tokenize();
        System.out.println(token);
    }
}