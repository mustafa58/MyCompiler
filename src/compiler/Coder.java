package compiler;

import lexer.Tag;
import optimizer.BasicBlock;
import optimizer.InsInfo;
import optimizer.Prep;
import symbols.Descriptor;
import symbols.Symbol;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Coder {

    public int lid = 1;

    public static class MemMan {
        public boolean[] mem = new boolean[100];
        public int tmp = 99;
        public int push() {
            tmp = tmp - 1;
            mem[tmp+1] = true;
            return tmp + 1;
        }
        public int pop() {
            tmp = tmp + 1;
            mem[tmp+1] = false;
            return tmp - 1;
        }
        public int getFreeMem() {
            for (int i = 99; i < tmp; i--) {
                if ( !mem[i] ) {
                    mem[i] = true;
                    return i;
                }
            }
            tmp = tmp - 1;
            mem[tmp+1] = true;
            return tmp + 1;
        }
        public int array(int length) {
            tmp = tmp - length;
            for (int i = 1; i <= length; i++) {
                mem[tmp-i] = true;
            }
            return tmp+length;
        }
        public void setMemFree(int index) {
            mem[index] = false;
            if (index == tmp+1)
                tmp = tmp +1;
        }
    }
    public static class RegDesc {
        public List<Integer> memloc = new LinkedList<Integer>();

        public void add(int addr) {
            if (!(memloc.contains(addr)))
                memloc.add(addr);
        }

        public void remove(int addr) {
            if (memloc.contains(addr))
                memloc.remove(addr);
        }

        public void flush() {
            memloc = new LinkedList<Integer>();
        }
        public boolean isLoaded(int x) {
            return memloc.contains(x);
        }
    }

    public int[][] icode;
    public ItmCodeGen codeGen;
    public MemMan mem = new MemMan();
    public RegDesc acc;
    public InsInfo[] insInfo;
    public Descriptor[] addrDesc;
    public List<BasicBlock> blocks;
    public List<Symbol> symbols;

    public Coder(ItmCodeGen cgen)
    {
        int size = cgen.getSize();
        codeGen = cgen;
        icode = cgen.table;
        symbols = cgen.list;
        acc = new RegDesc();
        insInfo = Prep.analyze(icode, size);
        addrDesc = Prep.init(icode, size, symbols);
        blocks = Prep.partition(icode, size);
    }


    public String generate(int size) {
        Symbol one=null;
        Symbol thr=null;
        Symbol five=null;
        String result = "";
        String data = ""; int count=0;
        String[] kod = new String[size];
        //Prepare for memory allocation
        for (int i = 0; i < size; i++) {
            kod[i] = "";
            if (icode[i][0] == '*' || icode[i][0] == '/') {
                int o=codeGen.Leaf(new Symbol(Tag.NUM, 1));
                one = symbols.get(icode[o][1]);
                int adr = mem.getFreeMem();
                one.memAddr = adr;
                one.add(adr);
                data = String.format("%+04d\n", one.val).
                        replace("+","")+data;
                count++;
            }
            else if ( icode[i][0] == Tag.Dereference ) {
                if ( icode[i].length > 2 && icode[i][2] != -1 ) {
                    int o=codeGen.Leaf(new Symbol(Tag.NUM, 300));
                    thr = symbols.get(icode[o][1]);
                    int adr = mem.getFreeMem();
                    thr.memAddr = adr;
                    thr.add(adr);
                    data = String.format("%+04d\n", thr.val).
                            replace("+","")+data;
                }
                else {
                    int o=codeGen.Leaf(new Symbol(Tag.NUM, 500));
                    five = symbols.get(icode[o][1]);
                    int adr = mem.getFreeMem();
                    five.memAddr = adr;
                    five.add(adr);
                    data = String.format("%+04d\n", five.val).
                            replace("+","")+data;
                }
                count++;
            }
            else if ( icode[i][0] == Tag.AddressOf ) {
                int o=codeGen.Leaf(new Symbol(Tag.NUM, 500));
                five = symbols.get(icode[o][1]);
                int adr = mem.getFreeMem();
                five.memAddr = adr;
                five.add(adr); count++;
                data = String.format("%+04d\n", five.val).
                        replace("+","")+data;
            }
            else if ( icode[i][0] == Tag.ID ) {
                int adr = mem.getFreeMem();
                symbols.get(icode[i][1]).memAddr = adr;
                addrDesc[i].add(adr);
                data = "000\n" + data;
                count++;
            }
            else if (icode[i][0] == Tag.NUM) {
                int adr = mem.getFreeMem();
                symbols.get(icode[i][1]).memAddr = adr;
                addrDesc[i].add(adr);
                data = String.format("%+04d\n",
                        (int)symbols.get(icode[i][1]).val).
                        replace("+","")+data;
                count++;
            }
            else if (icode[i][0] == Tag.Array) {
                int[] arr=symbols.get(icode[i][1]).array;
                int adr = mem.array(symbols.get(icode[i][1]).length);
                symbols.get(icode[i][1]).memAddr = adr;
                addrDesc[i].add(adr);
                for (int j = 0; j < arr.length; j++) {
                    data = String.format("%+04d\n", arr[j]).
                            replace("+","")+data;
                    count++;
                }
            }
        }
        data = "*"+(100-count)+"\n"+data;
        //Convert cross jump references
        for (int i = 0; i < size; i++) {
            kod[i] = kod[i] + code(i);
            if ( icode[i][0] == Tag.Goto ) {
                String label = newlabl();
                kod[icode[i][1]] =
                        label + ": \n" + kod[icode[i][1]];
                kod[i]=kod[i].replace(("$"+icode[i][1]), label);
            }
            else if ( icode[i][0] == Tag.IfFalse ) {
                String label = newlabl();
                kod[ icode[i][2] ] =
                        label+": \n"+ kod[ icode[i][2] ];
                kod[i]=kod[i].replace(("$"+icode[i][2]), label);
            }
            else if ( icode[i][0] == Tag.IfTrue ) {
                String label = newlabl();
                kod[ icode[i][2] ] =
                        label+": \n"+ kod[ icode[i][2] ];
                kod[i]=kod[i].replace(("$"+icode[i][2]), label);
            }
        }
        for (int i = 0; i < size; i++) {
            result += kod[i];
        }
        if ( one != null ) {
            result = result.replaceAll("#1",
                    String.valueOf(one.memAddr));
        }
        if ( five != null ) {
            result = result.replaceAll("#500",
                    String.valueOf(five.memAddr));
        }
        if ( thr != null ) {
            result = result.replaceAll("#300",
                    String.valueOf(thr.memAddr));
        }
        String[] lines = result.split("\n");
        //Replace labels with actual addresses
        for (int i = 0; i < lines.length; i++) {
            Pattern p = Pattern.compile("L\\d+");
            Matcher m = p.matcher(lines[i]);
            if ( m.lookingAt() ) {
                String lbl = m.group();
                String yeni = result.replaceAll(lbl+":\\s*\n", "");
                yeni = yeni.replaceAll(lbl+"\\s*:\\s*", "");
                yeni = yeni.replaceAll(lbl, String.format("%02d", i));
                result = yeni;
                lines = result.split("\n");
                i = 0;
            }
            count = lines.length;
        }
        lines = result.split("\n"); result="";
        //Modify $next instruction symbols
        for (int i = 0; i < lines.length; i++) {
            if ( lines[i].contains("$n2") ) {
                lines[i] = lines[i].replace("$n2",
                        String.format("%02d", i+2));
            }
            if ( lines[i].contains("$n") ) {
                lines[i] = lines[i].replace("$n",
                        String.format("%02d", i+1));
            }
            result += lines[i]+"\n";
        }
        if ( result.contains("$hlt") ) {
            result = result.replaceAll("\\$hlt", ""+(count-1));
        }
        return result+data;
    }


    public String newlabl() {
        lid = lid + 1;
        return "L" + (lid - 1);
    }

    public String spill(int x) {
        int loc;
        if ( icode[x][0] == Tag.ID )
            loc = symbols.get(icode[x][1]).memAddr;
        /*else if (icode[x][0] == Tag.Dereference) {
            loc = symbols.get(icode[x][1]).memAddr;
            addrDesc[x].add(loc);
            return  "STO "+mem.tmp+" \n"
                    + "LDA "+loc+" \n"
                    + "ADD #300 \n"
                    + "STO $n2 \n"
                    + "LDA "+mem.tmp+" \n"
                    + "STO 00 \n";
        }*/
        else loc = mem.getFreeMem();
        addrDesc[x].add(loc);
        return "STO "+loc+" \n";
    }

    /*private boolean bAliveExit(int x) {
        BasicBlock c=blocks.get(0);
        for (BasicBlock b: blocks) {
            if (b.begin <= x && b.end>= x) {
                c = b;
                break;
            }
        }
        return insInfo[c.end].isAliveAt(insInfo[x]);
    }
    private boolean usedAfter(int x, int v) {
        for (int i = x+1; i < insInfo.length; i++) {
            if ( insInfo[i].isAliveAt(insInfo[v]) )
                return true;
        }
        return false;
    }
    private boolean isCompByX(int x, int v) {
        if ( icode[x][0] == Tag.Assign ) {
            return (icode[x][1] == v);
        }
        else if (icode[v][0] == Tag.Dereference) {
            if ( icode[x].length > 2 && icode[x][2] != -1 ) {
                if ( icode[v][0] == Tag.Dereference )
                    return (icode[x][1]==icode[v][1]);
            }
            return false;
        }
        else if (icode[v][0] == Tag.Access) {
            if ( icode[x].length > 3 && icode[x][3] != -1 ) {
                if ( icode[v][0] == Tag.Access )
                    return (icode[x][1]==icode[v][1] && icode[x][2]==icode[v][2]);
            }
            return false;
        }
        else return false;
    }*/

    public String save(int x) {
        String com = "";
        int l = icode[x][1];
        int r = -1;
        if (icode[x].length > 2)
            r = icode[x][2];
        int m = -1;
        if (icode[x][0] == Tag.Access && icode[x].length > 3) {
            m = icode[x][3];
        }
        for (int v: acc.memloc) {
            /*if (icode[v][0] != Tag.ID && icode[v][0] != Tag.NUM
                    && icode[v][0] != Tag.Access)
                continue;*/
            addrDesc[v].setInReg(false);
            if (addrDesc[v].size() > 0) continue;       //Already copy in memory
            if (v == x && (v != l && v != r && v != m))
                continue;                               //Variable being computed
            if (insInfo[v].nextUse == -1 ||
                    (insInfo[v].nextUse == v+1 && icode[v+1][0] != Tag.Access) )//&& insInfo[v].isAlive)
                continue;                               //Will be recalculated
            else {
                com += spill(v);
                addrDesc[v].setInReg(false);
            }
        }
        acc.flush();
        return com;
    }

    public String getReg(int x, int y) {
        if ( acc.isLoaded(icode[x][y]) )
            return "";
        int expr = icode[x][y];
        String com = save(x);
        acc.flush(); acc.add(expr);
        addrDesc[expr].setInReg(true);
        if ( icode[expr][0] == Tag.Dereference ) {
            int adr = symbols.get(icode[expr][1]).memAddr;
            com   +=  "LDA "+adr+" \n"
                    + "ADD #500 \n"
                    + "STO $n \n"
                    + "LDA 00 \n";
            return com;
        }
        else if ( icode[expr][0] == Tag.Access ) {
            if ( acc.isLoaded(icode[expr][2]) ) {
                com = save(x);
            }
            com  +=  "LDA $n2 \n"
                    + "BR $n2 \n"
                    + "LDA "+addrDesc[icode[expr][1]].get(0)+" \n"
                    + "ADD "+addrDesc[icode[expr][2]].get(0)+" \n"
                    + "STO $n \n"
                    + "LDA 00 \n";
            acc.flush(); acc.add(x);
            addrDesc[x].setInReg(true);
            return com;
        }
        else if(icode[expr][0] == Tag.AddressOf) {
            int adr = symbols.get(icode[expr][1]).memAddr;
            com   +=  "LDA $n2 \n"
                    + "BR $n2 \n"
                    + "LDA "+adr+" \n"
                    + "SUB #500 \n";
            return com;
        }
        int adr = addrDesc[expr].get(0);
        return com+ "LDA "+adr+" \n";
    }

    public String addition(int x) {
        String result = "";                     //ADD left,right
        int l = icode[x][1];
        int r = icode[x][2];
        Descriptor right = addrDesc[r];
        if ( acc.isLoaded(l) )                  //left in register
            result = save(x);
        else                                    //left in memory
            result += getReg(x, 1);
        if ( acc.isLoaded(r) ) {                //right in register
            result += "STO "+mem.tmp+" \n";
            acc.flush(); acc.add(x);
            addrDesc[x].setInReg(true);
            return result + "ADD "+mem.tmp+" \n";
        }
        else if (right.size() > 0) {            //right in memory
            acc.flush(); acc.add(x);
            addrDesc[x].setInReg(true);
            return result + "ADD "+right.get(0)+" \n";
        }
        else throw new Error("something gone wrong.");
    }
    public String subtraction(int x) {
        String result = "";                     //SUB left,right
        int l = icode[x][1];
        int r = icode[x][2];
        Descriptor right = addrDesc[r];
        if ( acc.isLoaded(l) )                  //left in register
            result = save(x);
        else                                    //left in memory
            result += getReg(x, 1);
        if ( acc.isLoaded(r) ) {
            result += "STO "+mem.tmp+" \n";
            acc.flush(); acc.add(x);
            addrDesc[x].setInReg(true);
            return result + "SUB "+mem.tmp+" \n";
        }
        else if ( right.size() > 0 ) {
            acc.flush(); acc.add(x);
            addrDesc[x].setInReg(true);
            return result + "SUB "+right.get(0)+" \n";
        }
        else throw new Error("something gone wrong.");
    }
    public String multiplication(int x) {
        String result = "";                     //MULT left,right
        int l = icode[x][1];
        int r = icode[x][2];
        Descriptor left = addrDesc[l];
        Descriptor right = addrDesc[r];
        if ( !(acc.isLoaded(l)) && left.size() == 0)
            throw new Error("something gone wrong.");
        if ( !(acc.isLoaded(r)) && right.size() == 0)
            throw new Error("something gone wrong.");
        //todo check this!!!
        //if ( acc.isLoaded(l) || acc.isLoaded(r) )
            result += save(x);
        int a = left.get(0);
        int b = right.get(0);
        int a_stck = mem.getFreeMem();
        int b_stck = mem.getFreeMem();
        int top = mem.getFreeMem();
        String L1 = newlabl();
        String L2 = newlabl();
        String loop = newlabl();
        String end = newlabl();
        result += "LDA "+b+" \n"
                + "BRP "+L1+" \n"
                + "SUB "+b+" \n"
                + "SUB "+b+" \n"
                + "STO "+b_stck+" \n"
                + "LDA "+a+" \n"
                + "SUB "+a+" \n"
                + "SUB "+a+" \n"
                + "STO "+a_stck+" \n"
                + "BR "+L2+" \n"
                + L1 + ": \n"
                + "STO "+b_stck+" \n"
                + "LDA "+a+" \n"
                + "STO "+a_stck+" \n"
                + L2 + ": \n"
                + "STO "+top+" \n"
                + "SUB "+top+" \n"
                + "STO "+top+" \n"
                + "LDA "+b_stck+" \n"
                + loop + ": \n"
                + "BRZ "+end+" \n"
                + "SUB #1 \n"
                + "STO "+b_stck+" \n"
                + "LDA "+top+" \n"
                + "ADD "+a_stck+" \n"
                + "STO "+top+" \n"
                + "LDA "+b_stck+" \n"
                + "BR "+loop+" \n"
                + end + ": \n"
                + "LDA "+top+" \n"
                + "out \n";
        mem.setMemFree(a_stck); mem.setMemFree(b_stck); mem.setMemFree(top);
        acc.flush(); acc.add(x);
        return result;
    }
    public String division(int x) {
        String result = "";                     //DIV left,right
        int l = icode[x][1];
        int r = icode[x][2];
        Descriptor left = addrDesc[l];
        Descriptor right = addrDesc[r];
        if ( !(acc.isLoaded(l)) && left.size() == 0)
            throw new Error("something gone wrong.");
        if ( !(acc.isLoaded(r)) && right.size() == 0)
            throw new Error("something gone wrong.");
        //todo check this!!!
        //if ( acc.isLoaded(l) || acc.isLoaded(r) )
        result += save(x);
        int a = left.get(0);
        int b = right.get(0);
        int a_stck = mem.push();
        String entry = newlabl();
        String loop = newlabl();
        int bol = mem.push();
        result += "LDA "+a+" \n"
                + "STO "+a_stck+" \n"
                + "SUB "+a+" \n"
                + "STO "+bol+" \n"
                + "LDA "+b+" \n"
                + "BRZ $hlt \n"
                + "BR "+entry+" \n"
                + loop + ": \n"
                + "STO "+a_stck+" \n"
                + "LDA "+bol+" \n"
                + "ADD #1 \n"
                + "STO "+bol+" \n"
                + entry + ": \n"
                + "LDA "+a_stck+" \n"
                + "SUB "+b+" \n"
                + "BRP "+loop+" \n"
                + "LDA "+bol+" \n"
                + "out \n";
        mem.pop(); mem.pop();
        acc.flush(); acc.add(x);
        return result;
    }
    public String modulus(int x) {
        String result = "";                     //MOD left,right
        int l = icode[x][1];
        int r = icode[x][2];
        Descriptor left = addrDesc[l];
        Descriptor right = addrDesc[r];
        if ( !(acc.isLoaded(l)) && left.size() == 0)
            throw new Error("something gone wrong.");
        if ( !(acc.isLoaded(r)) && right.size() == 0)
            throw new Error("something gone wrong.");
        //todo check this!!!
        //if ( acc.isLoaded(l) || acc.isLoaded(r) )
        result += save(x);
        int a = left.get(0);
        int b = right.get(0);
        int a_stck = mem.push();
        String entry = newlabl();
        String loop = newlabl();
        result += "LDA "+a+" \n"
                + "STO "+a_stck+" \n"
                + "LDA "+b+" \n"
                + "BRZ $hlt \n"
                + "LDA "+a_stck+" \n"
                + "BR "+entry+" \n"
                + loop + ": \n"
                + "STO "+a_stck+" \n"
                + entry + ": \n"
                + "SUB "+b+" \n"
                + "BRP "+loop+" \n"
                + "ADD "+b+" \n";
        mem.pop();
        acc.flush(); acc.add(x);
        return result;
    }

    public String code(int x) {
        String komut = "";
        if ( icode[x][0] == Tag.NUM
                || icode[x][0] == Tag.ID
                || icode[x][0] == Tag.Array)
            return komut;

        int l = icode[x][1];
        int r = icode[x][2];

        switch (icode[x][0]) {
            case Tag.IfTrue:
                String label;
                Descriptor cond;
                if (icode[l][0] == Tag.ID)
                    cond = symbols.get(icode[l][1]);
                else cond = addrDesc[l];
                if ( !(acc.isLoaded(l)) ) {
                    if (cond.size() > 0)
                        komut += getReg(x, 1);
                    else
                        throw new Error("something gone wrong.");
                }
                switch (icode[l][0]) {
                    case Tag.LE:
                        label = newlabl();
                        komut += String.format("BRZ $%d \nBRP %s\nBR $%d \n%s: \n",
                                r, label, r, label);
                        break;
                    case Tag.GE:
                        komut += "BRP $"+r+" \n"; break;
                    case Tag.LT:
                        label = newlabl();
                        komut += String.format("BRP %s\nBR $%d \n%s: \n", label,r,label);
                        break;
                    case Tag.GT:
                        label = newlabl();
                        komut += String.format("BRZ %s\nBRP $%d \n%s: \n", label,r,label);
                        break;
                    case Tag.EQ:
                        komut += "BRZ $"+r+" \n"; break;
                    case Tag.NE:
                        label = newlabl();
                        komut += String.format("BRZ %s\nBR $%d \n%s: \n", label,r,label);
                        break;
                    default:
                        label = newlabl();
                        komut += String.format("BRZ %s\nBR $%d \n%s: \n", label,r,label);
                        break;
                }
                break;
            case Tag.IfFalse:
                if (icode[l][0] == Tag.ID)
                    cond = symbols.get(icode[l][1]);
                else cond = addrDesc[l];
                if ( !(acc.isLoaded(l)) ) {
                    if (cond.size() > 0)
                        komut = getReg(x, 1);
                    else
                        throw new Error("something gone wrong.");
                }
                switch (icode[l][0]) {
                    case Tag.LE:
                        label = newlabl();
                        komut += String.format("BRZ %s\nBRP $%d \n%s: \n", label,r,label);
                        break;
                    case Tag.GE:
                        label = newlabl();
                        komut += String.format("BRP %s\nBR $%d \n%s: \n", label,r,label);
                        break;
                    case Tag.LT:
                        komut += "BRP $"+r+" \n"; break;
                    case Tag.GT:
                        /*label = newlabl(); l2 = newlabl();
                        komut += String.format("BRP %s\n%s: BR $%d \n%s: BRZ %s\n",
                                label, l2, r, label, l2);*/
                        label = newlabl();
                        komut += String.format("BRZ $%d \nBRP %s\nBR $%d \n%s: \n",
                                r, label, r, label);
                        break;
                    case Tag.EQ:
                        label = newlabl();
                        komut += String.format("BRZ %s\nBR $%d \n%s: \n", label,r,label);
                        break;
                    case Tag.NE:
                        komut += "BRZ $"+r+" \n"; break;
                    default:
                        komut += "BRZ $"+r+" \n"; break;
                }
                break;
            case Tag.Goto:
                /*for (int v: acc.memloc) {
                    if (icode[v][0] != Tag.ID)
                        continue;
                    else komut += spill(v);
                }*/
                komut=save(x);
                komut+="BR $"+l+" \n";
                break;
            case Tag.Dereference:
                if ( icode[x].length > 2 && icode[x][2] != -1 ) {
                    komut = save(x);
                    if ( !acc.isLoaded(r) ) {
                        komut += "LDA "+addrDesc[r].get(0)+" \n";
                    }
                    komut +=  "STO "+mem.tmp+" \n"
                            + "LDA "+addrDesc[l].get(0)+" \n"
                            + "ADD #300 \n"
                            + "STO $n2 \n"
                            + "LDA "+mem.tmp+" \n"
                            + "STO 00 \n";
                    addrDesc[x].setInReg(true);
                    acc.flush(); acc.add(x);
                }
                else {
                    if ( acc.isLoaded(l) ) {
                        komut = save(x);
                        addrDesc[x].setInReg(true);
                        acc.flush(); acc.add(x);
                        komut +=  "ADD #500 \n"
                                + "STO $n \n"
                                + "LDA 00 \n";
                        return komut;
                    }
                    if ( addrDesc[l].size() == 0 )
                        throw new Error("something gone wrong.");
                    komut = save(x);
                    addrDesc[x].setInReg(true);
                    acc.flush(); acc.add(x);
                    komut +=  "LDA "+addrDesc[l].get(0)+" \n"
                            + "ADD #500 \n"
                            + "STO $n \n"
                            + "LDA 00 \n";
                }
                break;
            case Tag.AddressOf:
                komut = save(x);
                if ( addrDesc[l].size() == 0 ) {
                    addrDesc[l].add(symbols.get(icode[l][1]).memAddr);
                    //throw new Error("something gone wrong.");
                }
                addrDesc[x].setInReg(true);
                acc.flush(); acc.add(x);
                komut +=  "LDA $n2 \n"
                        + "BR $n2 \n"
                        + "LDA "+addrDesc[l].get(0)+" \n"
                        + "SUB #500 \n";
                break;
            case Tag.Access:
                if ( icode[x].length > 3 && icode[x][3] != -1 ) {
                    if (acc.isLoaded( icode[x][3] ))
                        komut = spill( icode[x][3] );
                    komut += save(x);
                    komut +=  "LDA $n2 \n"
                            + "BR $n2 \n"
                            + "STO "+addrDesc[icode[x][1]].get(0)+" \n"
                            + "SUB "+addrDesc[icode[x][2]].get(0)+" \n"
                            + "STO $n2 \n"
                            + "LDA "+addrDesc[icode[x][3]].get(0)+" \n"
                            + "STO 00 \n";
                    addrDesc[x].setInReg(true);
                    acc.flush(); acc.add(x);
                }
                else {
                    komut = save(x);
                    komut +=  "LDA $n2 \n"
                            + "BR $n2 \n"
                            + "LDA "+addrDesc[l].get(0)+" \n"
                            + "SUB "+addrDesc[r].get(0)+" \n"
                            + "STO $n \n"
                            + "LDA 00 \n";
                    acc.flush(); acc.add(x);
                    addrDesc[x].setInReg(true);
                }
                break;
            case Tag.Halt:
                for (int v: acc.memloc) {
                    if (icode[v][0] != Tag.ID)
                        continue;
                    else komut += spill(v);
                }
                return komut+"HLT \n";
            default:
                Descriptor left, right;
                if (icode[l][0] == Tag.ID)
                    left = symbols.get(icode[l][1]);
                else left = addrDesc[l];
                if (icode[r][0] == Tag.ID)
                    right = symbols.get(icode[r][1]);
                else right = addrDesc[r];

                switch (icode[x][0]) {
                    case '=':
                        if (acc.isLoaded(r)) {
                            addrDesc[l].flush(mem);
                            addrDesc[l].setInReg(true);
                            acc.add(l); acc.add(x);
                            addrDesc[x].setInReg(true);
                        }
                        else {
                            if (right.size() == 0 ) {
                                if ( icode[r][0] == Tag.Dereference ) {
                                    komut = getReg(x, 2);
                                    addrDesc[l].flush(mem);
                                    addrDesc[l].setInReg(true);
                                    acc.add(l); acc.add(x);
                                    addrDesc[x].setInReg(true);
                                }else throw new Error("something gone wrong.");
                            }
                            else {
                                komut = getReg(x, 2);
                                addrDesc[l].flush(mem);
                                addrDesc[l].setInReg(true);
                                acc.add(l); acc.add(x);
                                addrDesc[x].setInReg(true);
                            }
                        }
                        break;
                    case '+':
                        komut = addition(x); break;
                    case '*':
                        komut = multiplication(x); break;
                    case '/':
                        komut = division(x); break;
                    case '%':
                        komut = modulus(x); break;
                    case '<':
                    case '>':
                    case Tag.LE:
                    case Tag.GE:
                    case Tag.EQ:
                    case Tag.NE:
                    case '-':
                        /*if (acc.isLoaded(l)) {
                            if (acc.isLoaded(r)) {
                                acc.flush(); acc.add(x);
                                addrDesc[x].setInReg(true);
                                if (right.size() > 0)
                                    return "SUB "+right.get(0)+" \n";
                                else {
                                    return  "LDA "+mem.tmp+" \n" +
                                            "SUB "+mem.tmp+" \n";
                                }
                            }
                            else {
                                if (right.size() > 0) {
                                    acc.flush(); acc.add(x);
                                    addrDesc[x].setInReg(true);
                                    return "SUB "+right.get(0)+" \n";
                                }
                                else
                                    throw new Error("something gone wrong.");
                            }
                        }
                        else if (acc.isLoaded(r)) {
                            if (left.size() > 0) {
                                acc.flush(); acc.add(x);
                                addrDesc[x].setInReg(true);
                                if (right.size() > 0) {
                                    return  "LDA "+left.get(0)+" \n" +
                                            "SUB "+right.get(0)+" \n";
                                }
                                else {
                                    String com = spill(r);
                                    addrDesc[r].setInReg(false);
                                    return com+"SUB "+right.get(0)+" \n";
                                }
                            }
                            else
                                throw new Error("something gone wrong.");
                        }
                        else {
                            if (left.size() == 0 || right.size() == 0)
                                throw new Error("something gone wrong.");
                            else {
                                String com = "";
                                com += getReg(x, 1);
                                acc.flush(); acc.add(x);
                                addrDesc[x].setInReg(true);
                                com += "SUB "+right.get(0)+" \n";
                                return com;
                            }
                        }*/
                        komut = subtraction(x); break;
                    default:
                        break;
                }
                break;
        }
        for (BasicBlock b: blocks) {
            if (x == b.end) {
                return komut+save(x);
            }
        }
        return komut;
    }
}
