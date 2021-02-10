package compiler;

import lexer.*;
import optimizer.BasicBlock;
import optimizer.InsInfo;
import optimizer.Prep;
import symbols.*;
import syntax.eval.RelOp;
import syntax.eval.*;
import syntax.stmt.*;
import syntax.Node;
import syntax.Expr;
import syntax.Stmt;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    /*private char lookahead;
    static int pos;
    private String inp;
    public Parser(String inp) {
        this.inp = inp;
        pos = 0;
        lookahead = inp.toCharArray()[pos];
    }*/
    private final Lexer lx;
    private final ItmCodeGen gen;
    private Env top,saved;
    private Token lookahead;

    public Parser(String s) {
        lx = new Lexer(s);
        lookahead = lx.scan();
        gen = new ItmCodeGen();
    }

    String program() {
        top = null;
        Seq s = (Seq) block();
        s.gen(gen); gen.Node(Tag.Halt, -1, -1);
        gen.toString();
        Coder y = new Coder(gen);
        return y.generate(gen.getSize());
    }

    Node block() {
        Seq n = new Seq();
        saved = top;
        top = new Env(top);
        match('{');
        try {
            while ( lookahead.tag == Tag.VAR &&
                    ((Word)lookahead).lexeme.equals("var") )
            { Node d = decl(); if (d != null) n.put(d); }
            while ( lookahead.tag != '}' ) { n.put(stmt()); }
        }
        catch (Exception e) {
            throw new SyntaxError("missing '}'");
        }
        match('}');
        top = saved;
        return n;
    }

    Node decl() {
        Node e;
        Seq s = new Seq();
        match("var"); e = id();
        if (e.root == Tag.Array) {
            if (lookahead.tag == '=') {
                match('=');
                ((Term)e).entry.array = initvals();
                s.put(e);
            }
        }
        else if ( lookahead.tag == '=' ) {
            match('=');
            s.put(new BinOp('=', e, level_3()));
        } else s.put(e);
        while ( lookahead.tag == ',' ) {
            match(','); e = id();
            if ( lookahead.tag == '=' ) {
                match('=');
                s.put(new BinOp('=', e, level_3()));
            }
            else s.put(e);
        }
        match(';');
        return s;
    }

    int[] initvals() {
        ArrayList<Integer> vals=new ArrayList<Integer>();
        match('{');
        int num=((Num)lookahead).value;
        vals.add(num); match();
        while ( lookahead.tag == ',' )
        {
            match(',');
            if ( lookahead instanceof Num ) {
                num=((Num) lookahead).value;
                vals.add(num);
                match();
            }
            else throw new Error("syntax error.");
        }
        match('}');
        int[] v = new int[vals.size()];
        int j=0;
        for (int i: vals) {
            v[j++] = i;
        }
        return v;
    }

    Node body() {
        if ( lookahead.tag == '{' ) return block();
        Node n = optexpr(); match(';'); return n;
    }

    Node stmt() {
        Seq st = new Seq();
        if ( !(lookahead instanceof Word) ) {
            Node ex = expr();
            match(';');
            return ex;
        }
        switch ( ((Word)lookahead).lexeme ) {
            case "if":
                Node stmt; Expr expr;
                match("if"); match('('); expr = (Expr)expr(); match(')');
                stmt = body();
                if ( ((Word)lookahead).lexeme.equals("else") ) {
                    match("else");
                    Stmt eb;
                    if ( ((Word)lookahead).lexeme.equals("if") )
                        eb = stmt();
                    else eb = body();
                    return new IfElse(expr, stmt, eb);
                } else return new If(expr, stmt);
            case "for":
                Expr e1,e2,e3; Stmt b;
                match("for"); match('(');
                e1 = (Expr)optexpr(); match(';');
                e2 = (Expr)optexpr(); match(';');
                e3 = (Expr)optexpr(); match(')'); b = (Stmt)body();
                return new For(e1, e2, e3, b);
            case "while":
                Expr e; Stmt bd;
                match("while"); match('('); e = (Expr)expr(); match(')');
                bd = (Stmt)body(); return new While(e, bd);
            case "do":
                Expr exp; Stmt s;
                match("do"); s = (Stmt)body();
                match("while"); match('('); exp = (Expr)expr(); match(')');
                return new DoWhile(exp, s);
            default:
                Node ex = expr();
                match(';');
                return ex;
        }
    }

    Node optexpr() {
        if ( lookahead.tag != ';' ) return expr();
        else return null;
    }

    Node id() {
        Symbol s;
        Seq e = new Seq();
        if ( lookahead.tag == Tag.ID ) {
            String name = ((Word)lookahead).lexeme;
            match(name);
            while ( lookahead.tag == '[' )
            {
                match('[');
                e.put(expr());
                match(']');
            }
            if ( e.children.size() == 0 ) {
                s = top.get(name);
                if ( s != null ) return new Term(s);
                s = new Symbol(Tag.ID, name);
                top.put(name, s);
                return new Term(s);
            }
            else {
                int length=1;
                for (int i = e.children.size()-1; i >= 0; i--) {
                    Expr x=(Expr)e.children.get(i);
                    length*=x.rvalue();
                }
                s = top.get(name);
                if ( s != null ) return new Term(s);
                s = new Symbol(Tag.Array, name);
                s.length = length;
                top.put(name, s);
                return new Term(s);
            }
        }
        else throw new SyntaxError(lx, "Identifier expected.");
    }

    Node expr() {
        return level_4();
    }

    Node level0() {
        Node n = null,l,r;
        l = level1();
        n = l;
        while (true) {
            if ( lookahead.tag == '+' ) {
                match('+');
                r = level1();
                n = new BinOp('+', l, r);
                System.out.write('+');
            } else if ( lookahead.tag == '-' ) {
                match('-');
                r = level1();
                n = new BinOp('-', l, r);
                System.out.write('-');
            } else return n;
            l = n;
        }

    }

    Node level1() {
        Node n = null,l,r;
        l = level2();
        n = l;
        while (true) {
            if ( lookahead.tag == '*' ) {
                match('*');
                r = level2();
                n = new BinOp('*', l, r);
                System.out.write('*');
            } else if ( lookahead.tag == '%' ) {
                match('%');
                r = level2();
                n = new BinOp('%', l, r);
                System.out.write('%');
            } else if (lookahead.tag == '/') {
                match('/');
                r = level2();
                n = new BinOp('/', l, r);
                System.out.write('/');
            } else return n;
            l = n;
        }
    }

    Node level2() {
        UnOp root,n,t;
        if ( lookahead.tag == '&' ) {
            match('&');
            return new UnOp(Tag.AddressOf, factor());
        }
        if ( lookahead.tag != '*' ) return factor();
        match('*');
        n = new UnOp(Tag.Dereference, null);
        root = n;
        while ( lookahead.tag == '*' ) {
            match('*');
            t = new UnOp(Tag.Dereference, null);
            n.operand = t;
            n = t;
            System.out.write('*');
        }
        n.operand = factor();
        return root;
    }

    Node level_3() {
        BinOp root;
        BinOp n,t;
        Node l,r = null;
        l = level_2();
        if ( lookahead.tag != '=' ) return l;
        match('=');
        r = level_2();
        n = new BinOp('=', l, r);
        root = n; l = r;
        while ( lookahead.tag == '=' ) {
            match('=');
            r = level_2();
            t = new BinOp('=', l, r);
            n.right = t;
            n = t;
            l = r;
        }
        return root;
    }

    Node level_4() {
        Seq n;
        Node t;
        t = level_3();
        if ( lookahead.tag != ',' ) return t;
        match(',');
        n = new Seq(); n.put(t); n.put(level_3());
        while (lookahead.tag == ',') {
            t = level_3();
            n.put(t);
        }
        return n;
    }

    Node level_2() {
        Node n = null,l,r;
        l = level_1();
        n = l;
        while (true) {
            if ( lookahead.tag == Tag.EQ ) {
                match("==");
                r = level_1();
                n = new RelOp(Tag.EQ, l, r);
                System.out.print("==");
            } else if (lookahead.tag == Tag.NE) {
                match("!=");
                r = level_1();
                n = new RelOp(Tag.NE, l, r);
                System.out.print("!=");
            } else return n;
            l = n;
        }
    }

    Node level_1() {
        Node n = null,l,r;
        l = level0();
        n = l;
        while (true) {
            if ( lookahead.tag == Tag.LT ) {
                match('<');
                r = level0();
                n = new RelOp(Tag.LT, l, r);
                System.out.write('<');
            } else if (lookahead.tag == Tag.LE) {
                match("<=");
                r = level0();
                n = new RelOp(Tag.LE, l, r);
                System.out.print("<=");
            } else if (lookahead.tag == Tag.GT) {
                match('>');
                r = level0();
                n = new RelOp(Tag.GT, l, r);
                System.out.write('>');
            } else if (lookahead.tag == Tag.GE) {
                match(">=");
                r  = level0();
                n = new RelOp(Tag.GE, l, r);
                System.out.print(">=");
            } else return n;
            l = n;
        }
    }

    Node factor() {
        Node n = null;
        if ( lookahead.tag == '(' ) {
            match('(');
            n = expr();
            match(')');
        }
        else if ( lookahead.tag == Tag.ID ) {
            String name = ((Word)lookahead).lexeme;
            match( name );
            Symbol s = top.get(name);
            if ( s != null ) n = new Term( s );
            else throw new SyntaxError(lx,"syntax error");
            System.out.print(name);
            while ( lookahead.tag == '[' ) {
                match('[');
                n = new Access(Tag.Access, expr(), n);
                match(']');
                System.out.print("[]");
            }
        }
        else if ( lookahead.tag == '-' ) {
            match('-');
            if ( lookahead instanceof Num ) {
                int val = ((Num)lookahead).value;
                n = new Term( new Symbol(Tag.NUM, -1*val) );
                match();
                System.out.print(val);
            }
            else throw new SyntaxError(lx,"syntax error");
        }
        else if ( lookahead.tag == Tag.NUM ) {
            int val = ((Num)lookahead).value;
            n = new Term( new Symbol(Tag.NUM, val) );
            match();
            System.out.print(val);
        }
        else throw new SyntaxError(lx,"syntax error");
        return n;
    }

    void match(String t) {
        switch (lookahead.tag) {
            case Tag.DO:
            case Tag.WHILE:
            case Tag.FOR:
            case Tag.VAR:
            case Tag.ID:
                if (((Word) lookahead).lexeme.equals(t))
                    lookahead = lx.scan();
                break;
            case Tag.GE:
                if (t.equals(">="))
                    lookahead = lx.scan();
                break;
            case Tag.LE:
                if (t.equals("<="))
                    lookahead = lx.scan();
                break;
            case Tag.EQ:
                if (t.equals("=="))
                    lookahead = lx.scan();
                break;
            case Tag.NE:
                if (t.equals("!="))
                    lookahead = lx.scan();
                break;
            default:
                throw new SyntaxError(lx,"syntax error");
        }
    }
    void match(char t) {
        if ( ((char)lookahead.tag) == t ) lookahead = lx.scan();
        else throw new SyntaxError(lx,"syntax error");
    }
    void match() {
        lookahead = lx.scan();
    }
    /*DEBUG
    void expr() throws IOException {
        term();
        while(true) {
            if ( lookahead == '+' ) {
                match('+'); term(); System.out.print('+');
            }
            else if ( lookahead == '-' ) {
                match('-'); term(); System.out.print('-');
            }
            else return;
        }
    }

    void term() throws IOException {
        factor();
        while(true) {
            if ( lookahead == '*' ) {
                match('*'); factor(); System.out.print('*');
            }
            else if ( lookahead == '/' ) {
                match('/'); factor(); System.out.print('/');
            }
            else return;
        }
    }

    void factor() throws IOException {
        if ( lookahead == '(' ) {
            match('('); expr(); match(')');
        }
        else if ( Character.isDigit( (char)lookahead ) ) {
            System.out.print( (char)lookahead ); match(lookahead);
        }
        else if ( Character.isLetter( (char)lookahead ) ) {
            System.out.print( (char)lookahead ); match(lookahead);
        }
        else throw new Error("syntax error");
    }

    void match(char t) throws IOException {
        if ( lookahead == t ) lookahead = inp.toCharArray()[++pos];
        else throw new Error("syntax error");
    }
     */
}
