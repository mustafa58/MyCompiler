package symbols;

import compiler.Coder;

public interface Descriptor {
    public boolean isInReg();
    public void setInReg(boolean v);
    public int get(int index);
    public int size();
    public void add(int addr);
    public void remove(int addr);
    public void flush(Coder.MemMan mem);
}
