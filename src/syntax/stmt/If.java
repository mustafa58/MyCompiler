package syntax.stmt;

import compiler.ItmCodeGen;
import lexer.Tag;
import symbols.Label;
import syntax.Expr;
import syntax.Node;
import syntax.Stmt;

public class If extends Node implements Stmt {
    public Expr expr;
    public Stmt stmt;
    public If(Expr e, Stmt st) {
        super(Tag.IF);
        expr = e;
        stmt = st;
    }
    /*public String gen(Label l) {
        String after = l.newlabel();
        return String.format("ifFalse %s goto %s\n", expr.rvalue(), after)
                + expr.gen(l) + after + ":\n";
    }*/
    public int gen(ItmCodeGen gen) {
        int e = expr.gen(gen);
        int s = gen.Node(Tag.IfFalse, e, -1);
        int st = stmt.gen(gen);
        gen.table[s][2] = st + 1;
        return s;
    }
}
