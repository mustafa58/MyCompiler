package symbols;

public class Label {
    private int i;
    public Label() {
        i = 0;
    }
    public String newlabel() {
        return String.format("Label%d:", i++);
    }
    public void reset() {
        i = 0;
    }
}
