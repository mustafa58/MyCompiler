package syntax.stmt;

import compiler.ItmCodeGen;
import lexer.Tag;
import symbols.Label;
import syntax.Expr;
import syntax.Node;
import syntax.Stmt;

public class IfElse extends If {
    public Stmt elseb;
    public IfElse(Expr e, Stmt st, Stmt eb) {
        super(e, st);
        elseb = eb;
    }
    /*public String gen(Label l) {
        String after = l.newlabel();
        return String.format("ifFalse %s goto %s\n", expr.rvalue(), after)
                + stmt.gen(l) + "\n"
                + expr.gen(l) + after + elseb.gen(l) + ":\n";
    }*/
    public int gen(ItmCodeGen gen) {
        int e = expr.gen(gen);
        int s = gen.Node(Tag.IfFalse, e, -1);
        int st = stmt.gen(gen);
        int g = gen.Node(Tag.Goto, -1, -1);
        int eb = elseb.gen(gen);
        gen.table[s][2] = eb-1;
        while ( gen.table[eb][0] != 0 ) eb++;
        gen.table[g][1] = eb;
        return s;
    }
}
