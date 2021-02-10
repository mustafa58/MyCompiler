package compiler;

import lexer.Tag;
import symbols.Env;
import symbols.Symbol;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ItmCodeGen {
    public int[][] table;
    private Hashtable map;
    public List<Symbol> list;
    private int index = -1;
    public ItmCodeGen() {
        table = new int[1000][3];
        map = new Hashtable();
        list = new ArrayList<>();
    }

    public int Leaf(Symbol s) {
        if ( map.containsKey(s.hashCode()) ) return (int)map.get(s.hashCode());
        list.add(s); map.put(s.hashCode(), ++index);
        table[index] = new int[]{s.type, list.indexOf(s)};
        return index;
    }

    public int Node(int tag, int l, int r) {
        int[] tmp = {tag, l, r};
        if ( map.containsKey(tmp) ) return (int)map.get(tmp);
        map.put(tmp, ++index);
        table[index] = tmp;
        return index;
    }
    public int Node(int tag, int arr, int addr, int r) {
        int[] tmp = {tag, arr, addr, r};
        if ( map.containsKey(tmp) ) return (int)map.get(tmp);
        map.put(tmp, ++index);
        table[index] = tmp;
        return index;
    }

    public String to3AdrCde(int i) {
        if ( table[i][0] == Tag.ID )
            return (String) list.get(table[i][1]).val;
        if ( table[i][0] == Tag.NUM )
            return String.valueOf((int) list.get(table[i][1]).val);
        return "t" + i;
    }

    public String toString() {
        String tAddrCode = "";
        for ( int i = 0 ; i <= index ; i++ ) {
            if ( table[i][0] == Tag.ID || table[i][0] == Tag.NUM ) {
                if ( table[i][0] == Tag.ID )
                    tAddrCode += String.format("%d\t:Id, %s\n",
                            i, list.get(table[i][1]).val);
                else
                    tAddrCode += String.format("%d\t:Num, %s\n",
                            i, list.get(table[i][1]).val);
            }
            else if ( table[i][0] == Tag.Array ) {
                tAddrCode += String.format("%d\t:Array, %s\n",
                        i, list.get(table[i][1]).val);
            }
            else if ( table[i][0] == Tag.IfTrue ) {
                tAddrCode += String.format("%d\t:ifTrue %s goto %s\n",
                        i, to3AdrCde( table[i][1] ), table[i][2] );
            }
            else if ( table[i][0] == Tag.IfFalse ) {
                tAddrCode += String.format("%d\t:ifFalse %s goto %s\n",
                        i, to3AdrCde( table[i][1] ), table[i][2] );
            }
            else if ( table[i][0] == Tag.Goto ) {
                tAddrCode += String.format("%d\t:goto %s \n", i, table[i][1]);
            }
            else if ( table[i][0] == Tag.Dereference ) {
                if ( table[i].length > 2 && table[i][2] != -1 )
                    tAddrCode += String.format("%d\t:t%d ← *%s = %s \n",
                            i,i, to3AdrCde(table[i][1]),to3AdrCde(table[i][2]));
                else tAddrCode += String.format("%d\t:t%d ← *%s \n", i,i, to3AdrCde(table[i][1]));

            }
            else if ( table[i][0] == Tag.AddressOf ) {
                tAddrCode += String.format("%d\t:AdrOf t%d \n", i, table[i][1]);
            }
            else if ( table[i][0] == Tag.Access ) {
                if ( table[i].length > 3 && table[i][3] != -1 )
                    tAddrCode += String.format("%d\t:t%d ← %s[%s] = %s \n",
                            i,i, to3AdrCde(table[i][1]),to3AdrCde(table[i][2]),to3AdrCde(table[i][3]));
                else tAddrCode += String.format("%d\t:t%d ← %s[%s] \n",
                        i,i, to3AdrCde(table[i][1]), to3AdrCde(table[i][2]));
            }
            else if ( table[i][0] == Tag.Halt ) {
                tAddrCode += String.format("%d\t:Halt \n", i, table[i][1]);
            }
            else {
                String op = (char)table[i][0]+"";
                if ( table[i][0] == Tag.LE )
                    op = "<=";
                else if (table[i][0] == Tag.GE)
                    op = ">=";
                else if (table[i][0] == Tag.EQ)
                    op = "==";
                else if (table[i][0] == Tag.NE)
                    op = "!=";
                tAddrCode += String.format("%d\t:t%d ← %s %s %s\n", i,
                        i, to3AdrCde( table[i][1] ), op, to3AdrCde( table[i][2] ));
            }
        }
        return tAddrCode;
    }

    public int getSize() {
        return index+1;
    }
}
