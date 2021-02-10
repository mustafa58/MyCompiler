package syntax.eval;

import compiler.ItmCodeGen;
import lexer.Tag;
import symbols.Label;
import syntax.Expr;
import syntax.Node;
import syntax.Stmt;

public class BinOp extends Node implements Expr {
    public Node left;
    public Node right;
    public BinOp(int op, Node l, Node r) {
        super(op);
        left = l;
        right = r;
    }
    public String gen(Label l) {return "";}
    public int gen(ItmCodeGen gen) {
        if ( root == '=' ) {
            if ( left.root == Tag.Dereference )
                return gen.Node(Tag.Dereference,
                        ((UnOp)left).operand.gen(gen), right.gen(gen));
            if ( left.root == Tag.Access )
                return gen.Node(Tag.Access,
                        ((Access)left).operand.gen(gen),
                        ((Access)left).address.gen(gen), right.gen(gen));
        }
        return gen.Node(root,
                left.gen(gen), right.gen(gen));
    }
    public int rvalue() {
        Expr left = (Expr) this.left;
        Expr right = (Expr) this.right;
        switch (root)
        {
            case '+':
                return left.rvalue() + right.rvalue();
            case '-':
                return left.rvalue() - right.rvalue();
            case '*':
                return left.rvalue() * right.rvalue();
            case '/':
                return left.rvalue() / right.rvalue();
            case '=':
                return right.rvalue();
            default:
                throw new Error("Not known value");
        }
    }

    public String lvalue() {return "";}
}
