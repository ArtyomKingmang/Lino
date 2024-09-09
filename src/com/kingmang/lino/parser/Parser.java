package com.kingmang.lino.parser;

import com.kingmang.lino.lexer.*;
import com.kingmang.lino.symbol.Environment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Parser {

    private StringBuilder arduinoBuffer;
    private Environment environment;
    private StringBuilder buffer;
    private StringBuilder console;
    private ArrayList<Integer> errors = new ArrayList<>();
    private Lexer lexer;
    private Token token;
    private boolean error;
    private int line;

    public Parser(){
        arduinoBuffer = new StringBuilder();
        console = new StringBuilder();
        lexer = new Lexer(new File("test.txt"));
    }

    private String booleanExpr() throws IOException{
        StringBuilder booleanExpr = new StringBuilder();
        booleanExpr.append(expr());
        if(token.tag == TokenType.COMPARATOR){
            booleanExpr.append(Generator.translateLexeme(match(TokenType.COMPARATOR)));
            booleanExpr.append(expr());
        }
        return booleanExpr.toString();
    }
    private String expr() throws IOException{
        StringBuilder expr = new StringBuilder();
        expr.append(term());
        while(true){
            if(token.tag == '+'){
                expr.append(match('+'));
                expr.append(term());
                if(!error){
                    buffer.append("+ ");
                }
            }else if (token.tag == '-'){
                expr.append(match('-'));
                expr.append(term());
                if(!error){
                    buffer.append("- ");
                }
            }else return expr.toString();
        }

    }
    private String factor() throws IOException{
        StringBuilder factor = new StringBuilder();
        if(token.tag == '('){
            factor.append(match('('));
            factor.append(expr());
            factor.append(match(')'));
        }else if(token instanceof Num){
            buffer.append(((Num) token).value).append(' ');
            factor.append(match(TokenType.NUM));
        }else if(token.tag == TokenType.ID){
            buffer.append(((Word) token).getLexeme()).append(' ');
            String id = match(TokenType.ID);
            factor.append(id);
            if(token.tag == '.'){
                factor.append(match('.'));
                factor.append(match(TokenType.ID));
            }
            if(token.tag == '['){
                factor.append(match('['));
                factor.append(expr());
                factor.append(match(']'));
            }
            if(environment.get(id) == null){
                appendLineToConsole(String.format("Error ar line: %d Cannot resolve symbol %s", lexer.line, id),  true);
            }else if(token.tag == TokenType.LITERAl){
                factor.append(match(TokenType.LITERAl));
            }else{
                error(TokenType.NUM, "NUM/WORD/LITERAL");
            }

        }
        return factor.toString();
    }

    private String term() throws IOException{
        StringBuilder term = new StringBuilder();
        term.append(factor());
        while(true){
            if(token.tag == '*'){
                term.append(match('*'));
                term.append(factor());
                if(!error){
                    buffer.append("* ");
                }
            }else if (token.tag == '/'){
                term.append(match('/'));
                term.append(factor());
                if(!error){
                    buffer.append("/ ");
                }
            }else return term.toString();
        }

    }

    private String match(int matchTag) throws IOException{
        if(token.tag == matchTag){
            Token temp = token;
            nextToken();
            return temp.stringify();
        }else{
            error(matchTag, String.valueOf(matchTag));
            return "";
        }
    }
    private void error(int tag, String expected) throws IOException{
        if(!error){
            error = true;
            int last = buffer.lastIndexOf("\n");
            if(last >= 0){
                buffer.delete(last, buffer.length());
                appendLineToConsole("Expected".concat("Error at line: ".concat(String.valueOf(lexer.line)).concat("").concat(expected)), true);
                skipErrors(new Token(tag));
            }
        }
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
        }while (token.tag == TokenType.COMMENT);
        appendLineToConsole(token.toString(), false);
    }
    private boolean isEndToken(Token token){
        return
                token.tag == ';' ||
                token.tag == TokenType.NULL ||
                token.tag == TokenType.END ||
                token.tag == TokenType.RETURN;
    }
    private void appendLineToConsole(String string, boolean isError){
        console.append(string).append("\n");
        if(isError){
            errors.add(this.line);
        }
        this.line++;
    }
}
