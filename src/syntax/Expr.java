package syntax;

public interface Expr extends Stmt {
    public int rvalue();
    public String lvalue();
}
