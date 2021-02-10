package symbols;

import compiler.Coder;

import java.util.*;

public class Symbol implements Descriptor {
    public final int type;
    public final Object val;
    public int memAddr = -1;
    public int initValue = 0;
    public int length = 1;
    public int[] array;
    public List<Integer> memloc;
    public boolean isInReg = false;

    public Symbol(int tag, int v) {
        this.type = tag;
        val = v;
    }

    public Symbol(int tag, String s) {
        this.type = tag;
        val = s;
    }

    public Symbol(int tag, String s, int size) {
        type = tag;
        val = s;
        length = size;
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

    public int hashCode() {
        return 31*type + val.hashCode();
    }
}
