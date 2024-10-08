package com.kingmang.lino.parser;

import com.kingmang.lino.lexer.*;
import com.kingmang.lino.symbol.Environment;
import com.kingmang.lino.symbol.Symbol;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Parser {

    private StringBuffer arduinoBuffer;
    private Environment environment;
    private PrintWriter printWriter;
    private StringBuffer buffer;
    public StringBuilder console;
    private ArrayList<Integer> errors = new ArrayList<>();
    private Lexer lexer;
    private Token token;
    private String space = "";
    private boolean error;
    private int line;

    public Parser(Lexer lexer, File output) throws IOException {
        this.lexer = lexer;
        printWriter = new PrintWriter(output);
        buffer = new StringBuffer();
        arduinoBuffer = new StringBuffer();
        console = new StringBuilder();
        nextToken();
    }

    public String program() throws IOException {
        environment = null;
        arduinoBuffer.append(methods());

        printWriter.write(arduinoBuffer.toString());
        printWriter.close();

        appendLineToConsole(String.format("Done with %d %s", errors.size(), errors.size() <= 1 ? "error" : "errors"), errors.size() > 0);

        return arduinoBuffer.toString();

    }

    private String methods() throws IOException{
        StringBuilder methods = new StringBuilder();
        do {
            methods.append(method());
        } while (token.tag != TokenType.NULL);
        return methods.toString();
    }
    private String method() throws IOException {
        Environment saved = environment;
        environment = new Environment(environment);
        String tempString = space;
        space += '\t';
        boolean hasReturn;
        match(TokenType.FUNC);
        String name = match(TokenType.ID);
        match('(');
        String params = optParams();
        match(')');
        match(TokenType.COLON);
        StringBuilder returnType = new StringBuilder("void");
        if (token.tag == TokenType.TYPE) {
            returnType.delete(0,4);
            returnType.append(Generator.translateLexeme(match(TokenType.TYPE)));
            if (token.tag == '[') {
                returnType.append(match('['));
                returnType.append(match(']'));
            }
            hasReturn = true;
        } else if (token.tag == TokenType.VOID) {
            match(TokenType.VOID);
            hasReturn = false;
        } else {
            error(TokenType.RETURN, "return");
            hasReturn = true;
        }
        match(TokenType.LBRACE);
        String decelerations = decelerations();
        String statements = optStatement();

        StringBuilder optReturn = new StringBuilder("\t");
        if (hasReturn) {
            optReturn.append(space);
            optReturn.append("\n\t").append(match(TokenType.RETURN)).append(" ");
            optReturn.append(expr());
            optReturn.append(';');
            optReturn.append("\n\t");
        } else if (token.tag == TokenType.RETURN) {
            optReturn.append(space);
            optReturn.append(match(TokenType.RETURN));
            optReturn.append(';');
            optReturn.append("\n\t");
        }
        match(TokenType.RBRACE);

        String template = "%s%s(%s){\n%s%s%s\n}\n";
        environment = saved;
        space = tempString;
        return String.format(template, returnType, name, params, decelerations, statements, optReturn);
    }

    private String optStatement() throws IOException{
        StringBuilder optStatement = new StringBuilder();
        while(token.tag != TokenType.RETURN && token.tag != TokenType.RBRACE && token.tag != TokenType.NULL){
            optStatement.append(statement());
        }
        return optStatement.toString();

    }
    private String optParams() throws IOException {
        StringBuilder optParams = new StringBuilder();
        optParams.append(param());
        while (true) {
            if (token.tag == ',') {
                optParams.append(match(','));
                optParams.append(param());
            } else return optParams.toString();
        }
    }
    private String decelerations() throws IOException {
        StringBuilder decelerations = new StringBuilder();
        while (true) {
            if (token.tag == TokenType.TYPE) decelerations.append(deceleration());
            else break;
        }
        return decelerations.toString();
    }
    private String deceleration() throws IOException {
        StringBuilder decelration = new StringBuilder();
        Symbol symbol = new Symbol();
        String type = match(TokenType.TYPE);
        symbol.setType(type);
        decelration.append(Generator.translateLexeme(type));
        String id = match(TokenType.ID);
        decelration.append(id);
        if (token.tag == '[') {
            decelration.append(match('['));
            decelration.append(match(']'));
        }
        if (token.tag == TokenType.ASSIGN) {
            decelration.append(match(TokenType.ASSIGN));
            String value = expr();
            decelration.append(value);
            symbol.setValue(value);
        }
        if (environment.get(id) == null)
            environment.put(id, symbol);
        else appendLineToConsole("Error: Variable ".concat(id).concat(" is already defined in the scope"), true);
        decelration.append(';');
        return space + decelration.toString() + "\n";
    }
    private String param() throws IOException{
        StringBuilder param = new StringBuilder();
        if(token.tag == TokenType.TYPE) {
            Symbol symbol = new Symbol();
            String type = match(TokenType.TYPE);
            symbol.setType(type);
            param.append(Generator.translateLexeme(type));
            String id = match(TokenType.ID);
            param.append(id);
            if (token.tag == '[') {
                param.append(match('['));
                param.append(match(']'));
            }
            if (environment.get(id) == null) {
                environment.put(id, symbol);
            }else {
                appendLineToConsole(
                        "Error: Variable"
                                .concat(id)
                                .concat("is already definited in the scope"),
                        true);
            }
        }else if(token.tag == TokenType.LITERAl){
            param.append(match(TokenType.LITERAl));
        }
        return param.toString();
    }

    private String callParams() throws IOException{
        StringBuilder callParams = new StringBuilder();
        if(token.tag == '(' || token.tag == TokenType.NUM || token instanceof Word || token.tag == TokenType.LITERAl){
            callParams.append(expr());
            while (true){
                if(token.tag == ','){
                    callParams.append(match(','));
                    callParams.append(expr());
                }else break;
            }
        }
        return callParams.toString();
    }
    private String statement() throws IOException {
        String tempString = space;
        StringBuilder statement = new StringBuilder();
        if (token.tag == TokenType.IF) {
            statement.append(match(TokenType.IF));
            space += '\t';
            statement.append('(');
            statement.append(booleanExpr());
            match(TokenType.LBRACE);
            statement.append(')').append(" {\n ");
            statement.append(optStatement());
            match(TokenType.RBRACE);
            space = tempString;
            statement.append(space).append('}');
        } else if (token.tag == TokenType.WHILE) {
            statement.append(match(TokenType.WHILE));
            space += '\t';
            statement.append('(');
            statement.append(booleanExpr());
            statement.append(')').append(" {\n ");
            match(TokenType.LBRACE);
            statement.append(optStatement());
            match(TokenType.RBRACE);
            space = tempString;
            statement.append(space).append("}");
        } else if (token.tag == TokenType.REPEAT) {
            match(TokenType.REPEAT);
            space += '\t';
            statement.append("for (int i = 0; i < ");
            statement.append(expr());
            statement.append("; i++) {\n");
            match(TokenType.LBRACE);
            statement.append(optStatement());
            match(TokenType.RBRACE);
            space = tempString;
            statement.append(space).append("}");
        } else if (token.tag == TokenType.ID) {
            String id = match(TokenType.ID);
            String value;
            statement.append(id);
            if (token.tag == '(') {
                statement.append(match('('));
                statement.append(callParams());
                statement.append(match(')'));
                statement.append(match(';'));
            } else {
                if (token.tag == '[') {
                    statement.append(match('['));
                    statement.append(expr());
                    statement.append(match(']'));
                }
                statement.append(match(TokenType.ASSIGN));
                value = expr();
                statement.append(value);
                Symbol symbol = environment.get(id);
                if (symbol != null) {
                    symbol.setValue(value);
                } else {
                    appendLineToConsole(String.format("Error at line: %d Cannot resolve symbol %s", lexer.line, id), true);
                }
                statement.append(';');
            }
        } else if (token.tag == TokenType.PRINT) {
            match(TokenType.PRINT);
            statement.append("Serial.print");
            statement.append('(');
            statement.append(expr());
            statement.append(')');
            statement.append(';');
        } else {
            appendLineToConsole("line: ".concat(String.valueOf(lexer.line)).concat(" Not A Statement"), true);
            nextToken();
        }
        error = false;

        return space + statement.toString();
    }

    private String booleanExpr() throws IOException {
        StringBuilder booleanExpr = new StringBuilder();
        booleanExpr.append(expr());
        if (token.tag == TokenType.COMPARATOR) {
            booleanExpr.append(Generator.translateLexeme(match(TokenType.COMPARATOR)));
            booleanExpr.append(expr());
        }
        return booleanExpr.toString();
    }

    private String expr() throws IOException {
        StringBuilder expr = new StringBuilder();
        expr.append(term());
        while (true) {
            if (token.tag == '+') {
                expr.append(match('+'));
                expr.append(term());
                if (!error) {
                    buffer.append("+ ");
                }
            } else if (token.tag == '-') {
                expr.append(match('-'));
                expr.append(term());
                if (!error) {
                    buffer.append("- ");
                }
            } else return expr.toString();
        }
    }

    private String term() throws IOException {
        StringBuilder term = new StringBuilder();
        term.append(factor());
        while (true) {
            if (token.tag == '*') {
                term.append(match('*'));
                term.append(factor());
                if (!error) {
                    buffer.append("* ");
                }
            } else if (token.tag == '/') {
                term.append(match('/'));
                term.append(factor());
                if (!error) {
                    buffer.append("/ ");
                }
            } else return term.toString();
        }
    }

    private String factor() throws IOException {
        StringBuilder factor = new StringBuilder();
        if (token.tag == '(') {
            factor.append(match('('));
            factor.append(expr());
            factor.append(match(')'));
        } else if (token instanceof Num) {
            buffer.append(((Num) token).value).append(' ');
            factor.append(match(TokenType.NUM));
        } else if (token.tag == TokenType.ID) {
            buffer.append(((Word) token).getLexeme()).append(' ');
            String id = match(TokenType.ID);
            factor.append(id);
            if (token.tag == '.') {
                factor.append(match('.'));
                factor.append(match(TokenType.ID));
            }
            if (token.tag == '[') {
                factor.append(match('['));
                factor.append(expr());
                factor.append(match(']'));
            }
            if (environment.get(id) == null) {
                appendLineToConsole(String.format("Error at line: %d Cannot resolve symbol %s", lexer.line, id), true);
            }
        } else if (token.tag == TokenType.LITERAl) {
            factor.append(match(TokenType.LITERAl));
        } else {
            error(TokenType.NUM, "NUM/Word/LITERAL");
        }
        return factor.toString();
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
                token.tag == TokenType.RBRACE ||
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
