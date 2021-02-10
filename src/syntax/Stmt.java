package syntax;

import compiler.ItmCodeGen;


public interface Stmt {
    public int gen(ItmCodeGen i);
}
