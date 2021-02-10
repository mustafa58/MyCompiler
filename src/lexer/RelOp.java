package lexer;

public class RelOp extends Token {
    public RelOp(int t) {
        super(t);
        if (t == Tag.LT
                || t == Tag.LE
                || t == Tag.GT
                || t == Tag.GE
                || t == Tag.EQ
                || t == Tag.NE);
        else throw new Error("Mistake!");
    }
    public String toString() {
        switch (tag) {
            case Tag.LT:
                return "{<}";
            case Tag.LE:
                return "{<=}";
            case Tag.GT:
                return "{>}";
            case Tag.GE:
                return "{>=}";
            case Tag.EQ:
                return "{==}";
            case Tag.NE:
                return "{!=}";
            default:
                return null;
        }
    }
}
