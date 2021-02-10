package syntax.eval;

import compiler.ItmCodeGen;
import symbols.Label;
import symbols.Symbol;
import syntax.Expr;
import syntax.Node;
import syntax.Stmt;

public class Access extends UnOp implements Expr {
    public Node address;
    public Access(int op, Node addr, Node o) {
        super(op, o);
        address = addr;
    }
    public String gen(Label l) {return "";}
    public int gen(ItmCodeGen gen) {
        return gen.Node(root,
               operand.gen(gen), address.gen(gen));
    }

    public int rvalue() {
        int addr = ((Expr)address).rvalue();
        return ((Term)operand).entry.array[addr];
    }

    public String lvalue() {return "";}
}
