package syntax.eval;

import compiler.ItmCodeGen;
import symbols.Label;
import syntax.Node;
import syntax.Stmt;

import java.util.LinkedList;
import java.util.List;

public class Seq extends Node implements Stmt {
    public List<Node> children;
    public Seq() {
        super(-1);
        children = new LinkedList<Node>();
    }
    public String gen(Label l) {return "";}
    public void put(Node s) {
        children.add(s);
    }
    public int gen(ItmCodeGen gen) {
        Stmt s; int i = 1;
        if (children.size() == 0) return -1;
        int p = children.get(0).gen(gen);
        if (children.size() == 1) return p;

        for ( ; i<children.size(); i++)
        {
            s = children.get(i);
            s.gen(gen);
        }
        return p;
    }
}
