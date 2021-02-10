package syntax;

import java.util.ArrayList;
import java.util.List;

public abstract class Node implements Stmt {
    public int root;
    //public List<Node> children;
    public Node(int data) {
        root = data;
        //children = new ArrayList<>();
    }
    /*public void put(Node n) {
        children.add(n);
    }
    public Node get(int i) {
        return children.get(i);
    }*/
}
