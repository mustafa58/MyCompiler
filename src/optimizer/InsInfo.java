package optimizer;

public class InsInfo {
    public boolean isAlive = false;
    public int nextUse = -1;

    public InsInfo() {
        isAlive = false;
        nextUse = -1;
    }
}
