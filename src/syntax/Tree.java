package syntax;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Tree<T> {
    protected Node<T> root;

    public Tree(T rootData) {
        root = new Node<T>(rootData);
    }

    public void put(T child) {
        root.put(child);
    }
    public void put(Node<T> child) {
        root.children.add(child);
    }

    public Node<T> get(int i) {
        return root.children.get(i);
    }

    public static class Node<T> implements Iterable<Node<T>> {
        protected T data;
        protected Node<T> parent;
        protected List<Node<T>> children;

        public Node(T data) {
            this.data = data;
            this.children = new LinkedList<Node<T>>();
        }

        public void put(T child) {
            Node<T> childNode = new Node<T>(child);
            childNode.parent = this;
            this.children.add(childNode);
        }
        public Node<T> get(int i) {
            return children.get(i);
        }

        @Override
        public Iterator<Node<T>> iterator() {
            return children.iterator();
        }
    }
    // other features ...

}
