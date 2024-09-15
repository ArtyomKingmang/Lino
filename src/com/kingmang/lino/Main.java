package com.kingmang.lino;


import com.kingmang.lino.lexer.Lexer;
import com.kingmang.lino.lexer.Token;
import com.kingmang.lino.parser.Parser;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        File input = new File("input.lino");
        File output = new File("output.ino");

        try {
            Lexer lexer = new Lexer(input);
            Parser parser = new Parser(lexer, output);
            parser.program();
            System.out.println(parser.console.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}