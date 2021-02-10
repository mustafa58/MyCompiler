package syntax.eval;

import compiler.ItmCodeGen;
import symbols.Label;
import syntax.Expr;
import syntax.Node;
import syntax.Stmt;

public class UnOp extends Node implements Expr {
    public Node operand;
    public UnOp(int op, Node o) {
        super(op);
        operand = o;
    }
    public String gen(Label l) {return "";}
    public int gen(ItmCodeGen gen) {
        return gen.Node(root, operand.gen(gen), -1);
    }
    public int rvalue() {
        switch (root)
        {
            case '*':
            case '&':
            default:
                return -1001;
        }
    }
    public String lvalue() {return "";}
}
