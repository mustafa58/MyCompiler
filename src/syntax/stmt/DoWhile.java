package syntax.stmt;

import compiler.ItmCodeGen;
import lexer.Tag;
import symbols.Label;
import syntax.Expr;
import syntax.Node;
import syntax.Stmt;

public class DoWhile extends Node implements Stmt {
    public Stmt stmt;
    public Expr expr;
    public DoWhile(Expr e, Stmt st) {
        super(Tag.DO);
        expr = e; stmt = st;
    }
    public String gen(Label l) {return "";}
    public int gen(ItmCodeGen gen) {
        int st = stmt.gen(gen);
        int e = expr.gen(gen);
        return gen.Node(Tag.IfTrue, e, st);
    }
}
