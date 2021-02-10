package optimizer;

import java.util.LinkedList;
import java.util.List;

public class BasicBlock {
    public int begin;
    public int end;
    public List<BasicBlock> predecessors;
    public List<BasicBlock> successors;

    public BasicBlock(int start, int finish) {
        begin = start;
        end = finish;
    }

    public void addPre(BasicBlock b) {
        if (predecessors == null)
            predecessors = new LinkedList<>();
        if ( !(predecessors.contains(b)) )
            predecessors.add(b);
        if ( b.successors == null )
            b.successors = new LinkedList<>();
        if ( !(b.successors.contains(this)) )
            b.addAft(this);
    }
    public void addAft(BasicBlock b) {
        if (successors == null)
            successors = new LinkedList<>();
        if ( !(successors.contains(b)) )
            successors.add(b);
        if ( b.predecessors == null )
            b.predecessors = new LinkedList<>();
        if ( !(b.predecessors.contains(this)) )
            b.addPre(this);
    }
}
