package optimizer;

import lexer.Tag;
import symbols.Descriptor;
import symbols.Symbol;

import java.util.*;

public class Prep {
    public final static BasicBlock entry  = new BasicBlock(-1, -1);
    public final static BasicBlock exit = new BasicBlock(-1, -1);

    private static int[] leaders(int[][] icode, int size)
    {
        Integer[] leader;
        Set<Integer> lead = new HashSet<>();
        lead.add(0); int j = 1;
        for (int i = 0; i < size; i++) {
            if ( icode[i][0] == Tag.Goto) {
                lead.add(icode[i][1]);
                lead.add(i+1); j += 2;
            }
            if ( icode[i][0] == Tag.IfFalse) {
                lead.add(icode[i][2]);
                lead.add(i+1); j += 2;
            }
            if ( icode[i][0] == Tag.IfTrue) {
                lead.add(icode[i][2]);
                lead.add(i+1); j += 2;
            }
        }
        leader = new Integer[lead.size()];
        lead.toArray(leader);
        int[] r = new int[lead.size()];
        for (int i = 0; i < leader.length; i++) {
            r[i] = leader[i];
        }
        return r;
    }

    public static List<BasicBlock> partition(int[][] icode, int size)
    {
        BasicBlock b,t; int i;
        int[] lead = leaders(icode, size);
        List<BasicBlock> blocks = new LinkedList<>();
        if ( lead.length == 1 ){
            t=new BasicBlock(lead[0], icode.length-1);
            t.addPre(entry);
            t.addAft(exit);
            blocks.add(t);
            return blocks;
        }
        t=new BasicBlock(lead[0], lead[1]-1);
        t.addPre(entry);
        blocks.add(t);
        for (i = 1; i+1 < lead.length; i++) {
            b=new BasicBlock(lead[i], lead[i+1]-1);
            if ( icode[t.end][0] != Tag.Goto ) b.addPre(t);
            blocks.add(b);
            t = b;
        }
        b=new BasicBlock(lead[i], size-1);
        b.addAft(exit); b.addPre(t);
        blocks.add(b);
        for (BasicBlock x: blocks) {
            if ( icode[x.end][0] == Tag.Goto ) {
                BasicBlock y = blocks.get(0); i=0;
                while ( icode[x.end][1] != y.begin )
                {
                    i++;
                    y = blocks.get(i);
                }
                if (x.end < y.begin) x.addAft(y);
                else y.addAft(x);
            }
            else if (icode[x.end][0] == Tag.IfTrue) {
                BasicBlock y = blocks.get(0); i=0;
                while ( icode[x.end][2] != y.begin )
                {
                    i++;
                    y = blocks.get(i);
                }
                if (x.end < y.begin) x.addAft(y);
                else y.addAft(x);
            }
            else if (icode[x.end][0] == Tag.IfFalse) {
                BasicBlock y = blocks.get(0); i=0;
                while ( icode[x.end][2] != y.begin )
                {
                    i++;
                    y = blocks.get(i);
                }
                if (x.end < y.begin) x.addAft(y);
                else y.addAft(x);
            }
        }
        return blocks;
    }

    public static InsInfo[] analyze(int[][] icode, int size)
    {
        InsInfo[] inf = new InsInfo[size];
        for (int i = 0; i < size; i++) {
            inf[i] = new InsInfo();
        }
        int[] l = leaders(icode,size);
        if ( l.length == 1 ) {
            l = new int[]{l[0], size - 1};
        }
        for (int i = l.length-1; i > 0; i--) {
            boolean[] used = new boolean[size];
            for (int j = l[i]-1; j >= l[i-1]; j--) {
                if ( icode[j][0] == Tag.Goto ) continue;
                else if ( icode[j][0] == Tag.ID)
                    continue;
                else if ( icode[j][0] == Tag.NUM)
                    inf[j].isAlive = false;
                else if ( icode[j][0] == Tag.Array )
                    continue;
                else if ( icode[j][0] == Tag.AddressOf)
                    continue;
                else if ( icode[j][0] == Tag.Assign) {
                    int left = icode[ icode[j][1] ][0];
                    if (left == Tag.ID && !used[icode[j][1]]) {
                        used[icode[j][1]] = true;
                        inf[j].isAlive = false;
                        inf[ icode[j][1] ].isAlive = true;
                        inf[ icode[j][2] ].isAlive = true;
                        inf[ icode[j][2] ].nextUse = j;
                    }
                }
                else if ( icode[j][0] == Tag.Dereference) {
                    inf[j].isAlive = false;
                    inf[j].nextUse = -1;
                    if ( icode[j].length > 2 && icode[j][2] != -1) {
                        inf[ icode[j][2] ].isAlive = true;
                        inf[ icode[j][2] ].nextUse = j;
                    }
                    inf[ icode[j][1] ].isAlive = true;
                    inf[ icode[j][1] ].nextUse = j;
                }
                else if ( icode[j][0] == Tag.Access ) {
                    inf[j].isAlive = false;
                    if ( icode[j].length > 3 && icode[j][3] != -1) {
                        inf[ icode[j][3] ].isAlive = true;
                        inf[ icode[j][3] ].nextUse = j;
                    }
                    inf[ icode[j][2] ].isAlive = true;
                    inf[ icode[j][2] ].nextUse = j;
                    inf[ icode[j][1] ].isAlive = true;
                    inf[ icode[j][1] ].nextUse = j;
                }
                else if ( icode[j][0] == Tag.IfFalse) {
                    inf[ icode[j][1] ].isAlive = true;
                    inf[ icode[j][1] ].nextUse = j;
                }
                else if ( icode[j][0] == Tag.IfTrue) {
                    inf[ icode[j][1] ].isAlive = true;
                    inf[ icode[j][1] ].nextUse = j;
                }
                else {
                    inf[j].isAlive = false;
                    inf[ icode[j][1] ].isAlive = true;
                    inf[ icode[j][1] ].nextUse = j;
                    inf[ icode[j][2] ].isAlive = true;
                    inf[ icode[j][2] ].nextUse = j;
                }

            }
        }
        return inf;
    }

    public static Descriptor[] init(int[][] icode, int size, List<Symbol> env)
    {
        Descriptor[] desc = new Descriptor[size];
        for (int i = 0; i < size; i++) {
            if ( icode[i][0] == Tag.ID)
                desc[i] = env.get(icode[i][1]);
            else if ( icode[i][0] == Tag.NUM)
                desc[i] = env.get(icode[i][1]);
            else if ( icode[i][0] == Tag.IfFalse)
                continue;
            else if ( icode[i][0] == Tag.IfTrue)
                continue;
            else if ( icode[i][0] == Tag.Goto )
                continue;
            else {
                desc[i] = new AddressDesc(i);
            }
        }
        return desc;
    }

    private static void traverse(List<BasicBlock> bl, BasicBlock b, List<BasicBlock> path) {
        if (b == exit) return;
        if (path.contains(b)) {
            int i=bl.indexOf(b);
            for (BasicBlock x: path.subList(i,path.size())) {
                System.out.print(x.begin+", "+x.end);
                System.out.print('-');
            }
        }
        else {
            path.add(b);
            if ( b.successors == null) return;
            for (BasicBlock x: b.successors) {
                traverse(bl, x, path);
            }
        }
    }

    public static void loops(List<BasicBlock> blocks) {
        List<BasicBlock> path = new LinkedList<>();
        traverse(blocks, blocks.get(0), path);
    }
}
