package com.kingmang.lino.parser;

public class Generator {

    public static String translateLexeme(String lexeme){
        return switch (lexeme) {
            case "Int" -> "int ";
            case "Char" -> "char ";
            case "Boolean" -> "boolean ";
            case "<>" -> "!= ";
            default -> lexeme.concat(" ");
        };
    }
}
