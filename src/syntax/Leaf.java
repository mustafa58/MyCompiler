package syntax;

import lexer.Tag;
import symbols.Symbol;

public abstract class Leaf extends Node {
    public int tag;
    public Symbol entry;
    public Leaf(int t, Symbol s) {
        super(t);
        tag = t;
        entry = s;
    }
}
