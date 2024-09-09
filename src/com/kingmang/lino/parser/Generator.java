package com.kingmang.lino.parser;

public class Generator {

    public static String translateLexeme(String lexeme){
        switch(lexeme){
            case "Int":
                return "int ";
            case "Char":
                return "char ";
            case "Boolean":
                return "boolean ";
            case "<>":
                return "!= ";
        }
        return "";
    }
}
