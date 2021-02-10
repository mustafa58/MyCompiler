package lexer;

import compiler.Lexer;

import java.io.IOException;

public class SyntaxError extends RuntimeException {
    public SyntaxError(Lexer lex) {
        lex.printError();
        System.exit(-1);
    }
    public SyntaxError(Lexer lex, String error) {
        lex.printError();
        System.err.println(error);
        System.exit(-1);
    }
    public SyntaxError(String error) {
        System.err.println(error);
        System.exit(-1);
    }
}
