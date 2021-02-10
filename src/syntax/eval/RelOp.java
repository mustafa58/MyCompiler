package syntax.eval;

import compiler.ItmCodeGen;
import lexer.Tag;
import symbols.Label;
import syntax.Expr;
import syntax.Node;
import syntax.Stmt;

public class RelOp extends Node implements Expr {
    public Node left;
    public Node right;
    public RelOp(int op, Node l, Node r) {
        super(op);
        left = l;
        right = r;
    }
    public String gen(Label l) {return "";}
    public int gen(ItmCodeGen gen) {
        return gen.Node(root,
                left.gen(gen), right.gen(gen));
    }

    public int rvalue() {
        Expr left = (Expr) this.left;
        Expr right = (Expr) this.right;
        return left.rvalue() - right.rvalue();
    }
    public String lvalue() {
        return null;
    }
}
