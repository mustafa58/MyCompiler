package syntax.stmt;

import compiler.ItmCodeGen;
import lexer.Tag;
import symbols.Label;
import syntax.Expr;
import syntax.Node;
import syntax.Stmt;

public class While extends Node implements Stmt {
    public Expr expr;
    public Stmt stmt;
    public While(Expr e, Stmt st) {
        super(Tag.WHILE);
        expr = e; stmt = st;
    }
    public int gen(ItmCodeGen gen) {
        int e = expr.gen(gen);
        int s = gen.Node(Tag.IfFalse, e, -1);
        stmt.gen(gen);
        int end = gen.Node(Tag.Goto, e, -1);
        gen.table[s][2] = end + 1;
        return s;
    }
}
