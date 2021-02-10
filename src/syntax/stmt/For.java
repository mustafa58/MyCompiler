package syntax.stmt;

import compiler.ItmCodeGen;
import lexer.Tag;
import symbols.Label;
import syntax.Expr;
import syntax.Node;
import syntax.Stmt;

public class For extends Node implements Stmt {
    public Expr expr1;
    public Expr expr2;
    public Expr expr3;
    public Stmt stmt;
    public For(Expr e1, Expr e2, Expr e3, Stmt st) {
        super(Tag.FOR);
        expr1 = e1; expr2 = e2; expr3 = e3;
        stmt = st;
    }
    public String gen(Label l) {return "";}
    public int gen(ItmCodeGen gen) {
        expr1.gen(gen);
        int e2 = expr2.gen(gen);
        int s = gen.Node(Tag.IfFalse, e2, -1);
        stmt.gen(gen); expr3.gen(gen);
        int g = gen.Node(Tag.Goto, e2, -1);
        gen.table[s][2] = g + 1;
        return s;
    }
}
