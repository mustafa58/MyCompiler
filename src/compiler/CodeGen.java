package compiler;

import lexer.Tag;
import symbols.Symbol;

import java.util.List;

public class CodeGen {
    public int tmp = 99;
    public int lid = 1;
    public int regStat = -1;
    public int[][] arr;
    public int[] t;
    public List<Symbol> symbols;
    public CodeGen(int[][] itmcode, List<Symbol> env)
    {
        arr = itmcode;
        t = new int[itmcode.length];
        symbols = env;
    }

    public int push() {
        tmp = tmp - 1;
        return tmp + 1;
    }

    public int pop() {
        tmp = tmp + 1;
        return tmp - 1;
    }

    public String newlabl() {
        lid = lid + 1;
        return "L" + (lid-1);
    }

    public boolean isSymbol(int index) {
        if (index < 0) return false;
        return ( arr[index][0] == Tag.NUM || arr[index][0] == Tag.ID );
    }

    public int referencesOf(int index, int after) {
        int count = 0;
        for ( int i = after+1; i < arr.length; i++)
        {
            if ( i == index ) continue;
            if ( arr[i][1] == index )
                count++;
            if ( arr[i].length > 2 && arr[i][2] == index )
                count++;
        }
        return count;
    }

    public int getFreeMem() {
        for (int i = 0; i < t.length; i++) {
            if ( t[i] < 0 ) {
                t[i] = -1 * t[i];
                return t[i];
            }
        }
        tmp = tmp - 1;
        return tmp + 1;
    }

    public void setMemFree(int index) {
        t[index] = -1 * t[index];
    }

    public void preprocess() {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i][0] == Tag.NUM) {
                t[i] = push();
                System.out.print(String.format("%d <- %s \n", t[i], symbols.get(arr[i][1]).val));
            }
            if (arr[i][0] == Tag.ID) {
                t[i] = push();
                System.out.print(String.format("%d <- %s \n", t[i], symbols.get(arr[i][1]).val));
            }
        }
    }



    public String code(int i) {
        if ( t[i] != 0 ) return "";
        int l = arr[i][1]; int r = arr[i][2];
        String left,right,kod;
        String on = "";
        if (regStat == l) {
            if ( r != -1) {
                if ( isSymbol(r) )
                    right = ""+symbols.get(arr[r][1]).val;
                else right = String.valueOf(t[r]);
                if (referencesOf(r, i) > 0) setMemFree(r);
            } else right = "";
            if ( !isSymbol(l) && referencesOf(l, i) > 0) {
                t[l] = getFreeMem();
                on = "STR "+t[l]+" \n";
            }
            switch (arr[i][0]) {
                case '+':
                    kod = "ADD "+right+" \n";
                    break;
                case '-':
                    kod = "SUB "+right+" \n";
                    break;
                case '*': {
                    left = ""+push();
                    int top = push();
                    int b = push();
                    pop(); pop(); pop();
                    String loop = newlabl();
                    String end = newlabl();
                    kod =     "STO " + left + " \n"
                            + "SUB " + left + " \n"
                            + "STR " + top + " \n"
                            + "LDA " + right + " \n"
                            + "STR " + b + " \n"
                            + loop + ": \n"
                            + "LDA " + b + " \n"
                            + "BRZ " + end + " \n"
                            + "SUB #1 \n"
                            + "STR " + b + "\n"
                            + "LDA " + top + " \n"
                            + "ADD " + left + " \n"
                            + "STR " + top + " \n"
                            + "BR " + loop + " \n"
                            + end + ": \n"
                            + "LDA " + top + " \n";
                } break;
                case '/': {
                    String cont = newlabl();
                    String loop = newlabl();
                    int a = push();
                    int bol = push();
                    pop(); pop();
                    kod =     "STR " + a + " \n"
                            + "SUB " + a + " \n"
                            + "STR " + bol + " \n"
                            + "LDA " + a + " \n"
                            + "BR " + cont + " \n"
                            + loop + ": \n"
                            + "STR " + a + " \n"
                            + "LDA " + bol + " \n"
                            + "ADD #1 \n"
                            + "STR " + bol + " \n"
                            + "LDA " + a + " \n"
                            + cont + ": \n"
                            + "SUB " + right + " \n"
                            + "BRP " + loop + " \n"
                            + "LDA " + bol + " \n";
                } break;
                case Tag.Assign: {
                    regStat = l;
                    if ( isSymbol(l) ) left = ""+symbols.get(arr[l][1]).val;
                    else left = String.valueOf(t[l]);
                    kod = String.format("LDA %s \nSTO %s \n", right, left);
                    return kod;
                }
                case '<':
                case '>':
                case Tag.LE:
                case Tag.GE:
                case Tag.EQ:
                case Tag.NE:
                    kod = "SUB "+right+" \n";
                    break;
                case Tag.IfTrue: {
                    switch (arr[l][0]) {
                        case Tag.LE:
                            kod = String.format("BRN $%d \nBRZ $%d \n", r,r);
                            break;
                        case Tag.GE:
                            kod = String.format("BRP $%d \nBRZ $%d \n", r,r);
                            break;
                        case Tag.LT:
                            kod = "BRN $"+r+" \n"; break;
                        case Tag.GT:
                            kod = "BRP $"+r+" \n"; break;
                        case Tag.EQ:
                            kod = "BRZ $"+r+" \n"; break;
                        case Tag.NE:
                            kod = String.format("BRP $%d \nBRN $%d \n", r,r);
                            break;
                        default:
                            kod = String.format("BRP $%d \nBRN $%d \n", r,r);
                            break;
                    }
                    break;
                }
                case Tag.IfFalse: {
                    switch (arr[l][0]) {
                        case Tag.LE:
                            kod = "BRP $"+r+" \n"; break;
                        case Tag.GE:
                            kod = "BRN $"+r+" \n"; break;
                        case Tag.LT:
                            kod = String.format("BRP $%d \nBRZ $%d \n", r,r);
                            break;
                        case Tag.GT:
                            kod = String.format("BRN $%d \nBRZ $%d \n", r,r);
                            break;
                        case Tag.EQ:
                            kod = "BRZ $"+r+" \n"; break;
                        case Tag.NE:
                            kod = String.format("BRP $%d \nBRN $%d \n", r,r);
                            break;
                        default:
                            kod = "BRZ $"+r+" \n"; break;
                    }
                    break;
                }
                case Tag.Goto:
                    kod = "BR $"+l+" \n"; break;
                case Tag.Dereference:
                    int m = t[l]; if(m<0) m=-1*m;
                    kod = "LDA ["+ m +"] \n";
                    break;
                case Tag.AddressOf:
                    kod = "LDA "+ t[l]+ " \n"; break;
                default:
                    kod = ""; break;
            }
        }
        else if (regStat == r) {
            if ( isSymbol(l) )
                left = ""+symbols.get(arr[l][1]).val;
            else left = String.valueOf(t[l]);
            if (referencesOf(l, i) > 0) setMemFree(l);
            if ( !isSymbol(r) && referencesOf(r, i) > 0) {
                t[r] = getFreeMem();
                on = "STR "+t[r]+" \n";
            }
            switch (arr[i][0]) {
                case '+':
                    kod = "ADD "+left+" \n";
                    break;
                case '-':
                    right = ""+push(); pop();
                    kod =     "STO "+right+" \n"
                            + "LDA "+left+" \n"
                            + "SUB "+right+" \n";
                    break;
                case '*': {
                    right = ""+push();
                    int b = push();
                    int top = push();
                    pop(); pop(); pop();
                    String loop = newlabl();
                    String end = newlabl();
                    kod =     "STR " + right + " \n"
                            + "STR " + b + " \n"
                            + "SUB " + right + " \n"
                            + "STR " + top + " \n"
                            + loop + ": \n"
                            + "LDA " + b + " \n"
                            + "BRZ " + end + " \n"
                            + "SUB #1 \n"
                            + "STR " + b + "\n"
                            + "LDA " + top + " \n"
                            + "ADD " + left + " \n"
                            + "STR " + top + " \n"
                            + "BR " + loop + " \n"
                            + end + ": \n"
                            + "LDA " + top + " \n";
                } break;
                case '/': {
                    String cont = newlabl();
                    String loop = newlabl();
                    right = ""+push();
                    int bol = push();
                    int a = push();
                    pop(); pop(); pop();
                    kod =     "STR " + right + " \n"
                            + "SUB " + right + " \n"
                            + "STR " + bol + " \n"
                            + "LDA " + left + " \n"
                            + "STR " + a + " \n"
                            + "BR " + cont + " \n"
                            + loop + ": \n"
                            + "STR " + a + " \n"
                            + "LDA " + bol + " \n"
                            + "ADD #1 \n"
                            + "STR " + bol + " \n"
                            + "LDA " + a + " \n"
                            + cont + ": \n"
                            + "SUB " + right + " \n"
                            + "BRP " + loop + " \n"
                            + "LDA " + bol + " \n";
                } break;
                case Tag.Assign: {
                    regStat = l;
                    kod = String.format("STO %s \n", left);
                    return kod;
                }
                case '<':
                case '>':
                case Tag.LE:
                case Tag.GE:
                case Tag.EQ:
                case Tag.NE:
                    right = ""+push(); pop();
                    kod =     "STO "+right+" \n"
                            + "LDA "+left+" \n"
                            + "SUB "+right+" \n";
                    break;
                case Tag.IfTrue: {
                    switch (arr[l][0]) {
                        case Tag.LE:
                            kod = String.format("BRN $%d \nBRZ $%d \n", r,r);
                            break;
                        case Tag.GE:
                            kod = String.format("BRP $%d \nBRZ $%d \n", r,r);
                            break;
                        case Tag.LT:
                            kod = "BRN $"+r+" \n"; break;
                        case Tag.GT:
                            kod = "BRP $"+r+" \n"; break;
                        case Tag.EQ:
                            kod = "BRZ $"+r+" \n"; break;
                        case Tag.NE:
                            kod = String.format("BRP $%d \nBRN $%d \n", r,r);
                            break;
                        default:
                            kod = String.format("BRP $%d \nBRN $%d \n", r,r);
                            break;
                    }
                    break;
                }
                case Tag.IfFalse: {
                    switch (arr[l][0]) {
                        case Tag.LE:
                            kod = "BRP $"+r+" \n"; break;
                        case Tag.GE:
                            kod = "BRN $"+r+" \n"; break;
                        case Tag.LT:
                            kod = String.format("BRP $%d \nBRZ $%d \n", r,r);
                            break;
                        case Tag.GT:
                            kod = String.format("BRN $%d \nBRZ $%d \n", r,r);
                            break;
                        case Tag.EQ:
                            kod = "BRZ $"+r+" \n"; break;
                        case Tag.NE:
                            kod = String.format("BRP $%d \nBRN $%d \n", r,r);
                            break;
                        default:
                            kod = "BRZ $"+r+" \n"; break;
                    }
                    break;
                }
                case Tag.Goto:
                    kod = "BR $"+l+" \n"; break;
                case Tag.Dereference:
                    int m = t[l]; if(m<0) m=-1*m;
                    kod = "LDA ["+ m +"] \n";
                    break;
                case Tag.AddressOf:
                    kod = "LDA "+ t[l]+ " \n"; break;
                default:
                    kod = ""; break;
            }
        }
        else {
            if ( isSymbol(l) )
                left = ""+symbols.get(arr[l][1]).val;
            else left = String.valueOf(t[l]);
            if (referencesOf(l, i) > 0) setMemFree(l);
            if ( r != -1) {
                if ( isSymbol(r) )
                    right = ""+symbols.get(arr[r][1]).val;
                else right = String.valueOf(t[r]);
                if (referencesOf(r, i) > 0) setMemFree(r);
            } else right = "";
            switch (arr[i][0]) {
                case '+':
                    kod = "LDA "+left+" \nADD "+right+" \n";
                    break;
                case '-':
                    kod = "LDA "+left+" \nSUB "+right+" \n";
                    break;
                case '*': {
                    int top = push();
                    int b = push();
                    pop(); pop();
                    String loop = newlabl();
                    String end = newlabl();
                    kod =     "LDA " + left + " \n"
                            + "SUB " + left + " \n"
                            + "STR " + top + " \n"
                            + "LDA " + right + " \n"
                            + "STR " + b + " \n"
                            + loop + ": \n"
                            + "LDA " + b + " \n"
                            + "BRZ " + end + " \n"
                            + "SUB #1 \n"
                            + "STR " + b + "\n"
                            + "LDA " + top + " \n"
                            + "ADD " + left + " \n"
                            + "STR " + top + " \n"
                            + "BR " + loop + " \n"
                            + end + ": \n"
                            + "LDA " + top + " \n";
                } break;
                case '/': {
                    String cont = newlabl();
                    String loop = newlabl();
                    int a = push();
                    int bol = push();
                    pop(); pop();
                    kod =     "LDA " + left + " \n"
                            + "STR " + a + " \n"
                            + "SUB " + a + " \n"
                            + "STR " + bol + " \n"
                            + "LDA " + a + " \n"
                            + "BR " + cont + " \n"
                            + loop + ": \n"
                            + "STR " + a + " \n"
                            + "LDA " + bol + " \n"
                            + "ADD #1 \n"
                            + "STR " + bol + " \n"
                            + "LDA " + a + " \n"
                            + cont + ": \n"
                            + "SUB " + right + " \n"
                            + "BRP " + loop + " \n"
                            + "LDA " + bol + " \n";
                } break;
                case Tag.Assign: {
                    regStat = l;
                    kod = String.format("LDA %s \nSTO %s \n", right, left);
                    return kod;
                }
                case '<':
                case '>':
                case Tag.LE:
                case Tag.GE:
                case Tag.EQ:
                case Tag.NE:
                    kod = "SUB "+right+" \n";
                    break;
                case Tag.IfTrue: {
                    switch (arr[l][0]) {
                        case Tag.LE:
                            kod = String.format("BRN $%d \nBRZ $%d \n", r,r);
                            break;
                        case Tag.GE:
                            kod = String.format("BRP $%d \nBRZ $%d \n", r,r);
                            break;
                        case Tag.LT:
                            kod = "BRN $"+r+" \n"; break;
                        case Tag.GT:
                            kod = "BRP $"+r+" \n"; break;
                        case Tag.EQ:
                            kod = "BRZ $"+r+" \n"; break;
                        case Tag.NE:
                            kod = String.format("BRP $%d \nBRN $%d \n", r,r);
                            break;
                        default:
                            kod = String.format("BRP $%d \nBRN $%d \n", r,r);
                            break;
                    }
                    break;
                }
                case Tag.IfFalse: {
                    switch (arr[l][0]) {
                        case Tag.LE:
                            kod = "BRP $"+r+" \n"; break;
                        case Tag.GE:
                            kod = "BRN $"+r+" \n"; break;
                        case Tag.LT:
                            kod = String.format("BRP $%d \nBRZ $%d \n", r,r);
                            break;
                        case Tag.GT:
                            kod = String.format("BRN $%d \nBRZ $%d \n", r,r);
                            break;
                        case Tag.EQ:
                            kod = "BRZ $"+r+" \n"; break;
                        case Tag.NE:
                            kod = String.format("BRP $%d \nBRN $%d \n", r,r);
                            break;
                        default:
                            kod = "BRZ $"+r+" \n"; break;
                    }
                    break;
                }
                case Tag.Goto:
                    kod = "BR $"+l+" \n"; break;
                case Tag.Dereference:
                    int m = t[l]; if(m<0) m=-1*m;
                    kod = "LDA ["+ m +"] \n";
                    break;
                case Tag.AddressOf:
                    kod = "LDA "+ t[l]+ " \n"; break;
                default:
                    kod = ""; break;
            }
        }
        regStat = i;
        return on + kod;
        /*
        if ( arr[i][0] == Tag.IfTrue ) {
            if ( arr[arr[i][1]][0] == Tag.LE ) {
                done[i] = true;
                String res =
                        code( arr[ arr[i][1] ][2] )
                                + "STR "+push()+" \n"
                                + code( arr[ arr[i][1] ][1] )
                                + String.format("SUB %d \n", ++tmp);
                String end = newlabl();
                res = res
                        + String.format("BRP %s \n", end)
                        + code( arr[i][2] )
                        + end + ": \n";
                return res;
            }
            if ( arr[arr[i][1]][0] == Tag.GE ) {
                done[i] = true;
                String res =
                        code( arr[ arr[i][1] ][1] )
                                + "STR "+push()+" \n"
                                + code( arr[ arr[i][1] ][2] )
                                + String.format("SUB %d \n", ++tmp);
                String end = newlabl();
                res = res
                        + String.format("BRP %s \n", end)
                        + code( arr[i][2] )
                        + end + ": \n";
                return res;
            }
            if ( arr[arr[i][1]][0] == Tag.LT ) {
                done[i] = true;
                String res =
                        code( arr[ arr[i][1] ][2] )
                                + "STR "+push()+" \n"
                                + code( arr[ arr[i][1] ][1] )
                                + String.format("SUB %d \n", ++tmp);
                String end = newlabl();
                res = res
                        + String.format("BRP %s \n", end)
                        + String.format("BRZ %s \n", end)
                        + code( arr[i][2] )
                        + end + ": \n";
                return res;
            }
            if ( arr[arr[i][1]][0] == Tag.GT ) {
                done[i] = true;
                String res =
                        code( arr[ arr[i][1] ][1] )
                                + "STR "+push()+" \n"
                                + code( arr[ arr[i][1] ][2] )
                                + String.format("SUB %d \n", ++tmp);
                String end = newlabl();
                res = res
                        + String.format("BRP %s \n", end)
                        + String.format("BRZ %s \n", end)
                        + code( arr[i][2] )
                        + end + ": \n";
                return res;
            }
            if ( arr[arr[i][1]][0] == Tag.EQ ) {
                done[i] = true;
                String res =
                        code( arr[ arr[i][1] ][2] )
                                + "STR "+push()+" \n"
                                + code( arr[ arr[i][1] ][1] )
                                + String.format("SUB %d \n", ++tmp);
                String pocket = newlabl();
                String check = newlabl();
                String end = newlabl();
                res = res
                        + String.format("BR %s \n", check)
                        + pocket + ": \n"
                        + code( arr[i][2] )
                        + String.format("BR %s \n", end)
                        + String.format("%s: BRZ %s \n", check, pocket)
                        + end + ": \n";
                return res;
            }
            if ( arr[arr[i][1]][0] == Tag.NE ) {
                done[i] = true;
                String res =
                        code( arr[ arr[i][1] ][2] )
                                + "STR "+push()+" \n"
                                + code( arr[ arr[i][1] ][1] )
                                + String.format("SUB %d \n", ++tmp);
                String end = newlabl();
                res = res
                        + String.format("BRZ %s \n", end)
                        + code( arr[i][2] )
                        + end + ": \n";
                return res;
            }
            else return "??\n";
        }
        if ( arr[i][0] == Tag.IfFalse ) {
            if ( arr[arr[i][1]][0] == Tag.LE ) {
                done[i] = true;
                String res =
                        code( arr[ arr[i][1] ][1] )
                                + "STR "+push()+" \n"
                                + code( arr[ arr[i][1] ][2] )
                                + String.format("SUB %d \n", ++tmp);
                String end = newlabl();
                res = res
                        + String.format("BRP %s \n", end)
                        + String.format("BRZ %s \n", end)
                        + code( arr[i][2] )
                        + end + ": \n";
                return res;
            }
            if ( arr[arr[i][1]][0] == Tag.GE ) {
                done[i] = true;
                String res =
                        code( arr[ arr[i][1] ][2] )
                                + "STR "+push()+" \n"
                                + code( arr[ arr[i][1] ][1] )
                                + String.format("SUB %d \n", ++tmp);
                String end = newlabl();
                res = res
                        + String.format("BRP %s \n", end)
                        + String.format("BRZ %s \n", end)
                        + code( arr[i][2] )
                        + end + ": \n";
                return res;
            }
            if ( arr[arr[i][1]][0] == Tag.LT ) {
                done[i] = true;
                String res =
                        code( arr[ arr[i][1] ][1] )
                                + "STR "+push()+" \n"
                                + code( arr[ arr[i][1] ][2] )
                                + String.format("SUB %d \n", ++tmp);
                String end = newlabl();
                res = res
                        + String.format("BRP %s \n", end)
                        + code( arr[i][2] )
                        + end + ": \n";
                return res;
            }
            if ( arr[arr[i][1]][0] == Tag.GT ) {
                done[i] = true;
                String res =
                        code( arr[ arr[i][1] ][2] )
                                + "STR "+push()+" \n"
                                + code( arr[ arr[i][1] ][1] )
                                + String.format("SUB %d \n", ++tmp);
                String end = newlabl();
                res = res
                        + String.format("BRP %s \n", end)
                        + code( arr[i][2] )
                        + end + ": \n";
                return res;
            }
            if ( arr[arr[i][1]][0] == Tag.EQ ) {
                done[i] = true;
                String res =
                        code( arr[ arr[i][1] ][2] )
                                + "STR "+push()+" \n"
                                + code( arr[ arr[i][1] ][1] )
                                + String.format("SUB %d \n", ++tmp);
                String end = newlabl();
                res = res
                        + String.format("BRZ %s \n", end)
                        + code( arr[i][2] )
                        + end + ": \n";
                return res;
            }
            if ( arr[arr[i][1]][0] == Tag.NE ) {
                done[i] = true;
                String res =
                        code( arr[ arr[i][1] ][2] )
                                + "STR "+push()+" \n"
                                + code( arr[ arr[i][1] ][1] )
                                + String.format("SUB %d \n", ++tmp);
                String pocket = newlabl();
                String check = newlabl();
                String end = newlabl();
                res = res
                        + String.format("BR %s \n", check)
                        + pocket + ": \n"
                        + code( arr[i][2] )
                        + String.format("BR %s \n", end)
                        + String.format("%s: BRZ %s \n", check, pocket)
                        + end + ": \n";
                return res;
            }
            else return "??\n";
        }
        else return "...\n";
         */
    }
}
