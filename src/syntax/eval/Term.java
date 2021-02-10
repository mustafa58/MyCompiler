package syntax.eval;

import compiler.ItmCodeGen;
import lexer.Tag;
import symbols.Symbol;
import syntax.Expr;
import syntax.Leaf;
import syntax.Node;

public class Term extends Leaf implements Expr {
    public Term(Symbol s) {
        super(s.type, s);
    }
    public int gen(ItmCodeGen gen) {
        return gen.Leaf(entry);
    }
    public int rvalue() {
        if ( tag == Tag.NUM )
            return (int)entry.val;
        else if(tag == Tag.ID)
            return (int)entry.initValue;
        else return -1001;
    }

    public String lvalue() {return "";}
}
