package optimizer;

import compiler.Coder;
import symbols.Descriptor;

import java.util.*;

public class AddressDesc implements Descriptor {
    public int statement;
    public List<Integer> memloc;
    public boolean isInReg = false;

    public AddressDesc(int temp)
    {
        statement = temp;
        isInReg = false;
    }

    public boolean isInReg() {
        return isInReg;
    }
    public void setInReg(boolean v) {
        isInReg = v;
    }
    public int get(int index) {
        return memloc.get(index);
    }
    public int size() {
        if ( memloc == null)
            return 0;
        return memloc.size();
    }
    public void add(int addr) {
        if ( memloc == null )
            memloc = new LinkedList<Integer>();
        if ( !(memloc.contains(addr)) )
            memloc.add(addr);
    }
    public void remove(int addr) {
        if ( memloc != null && memloc.contains(addr) )
            memloc.remove(addr);
    }
    public void flush(Coder.MemMan mem) {
        if ( memloc != null ) {
            for (int i: memloc) {
                mem.setMemFree(i);
            }
            memloc = new LinkedList<Integer>();
        }
    }
}
