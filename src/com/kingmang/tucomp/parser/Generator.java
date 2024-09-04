package com.kingmang.tucomp.parser;

public class Generator {

    public String generate(String lexeme){
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
