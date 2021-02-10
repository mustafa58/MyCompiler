package compiler;

import lexer.*;

import java.util.Hashtable;

public class Lexer {
    private int lexemeBegin;
    private int forward;
    public char[] text;
    public int line = 1;

    private Hashtable words = new Hashtable();
    void reserve(Word t) { words.put(t.lexeme, t); }
    public Lexer(String source) {
        reserve( new Word(Tag.TRUE, "true") );
        reserve( new Word(Tag.FALSE,"false") );
        reserve( new Word(Tag.FOR,  "for") );
        reserve( new Word(Tag.WHILE,"while") );
        reserve( new Word(Tag.DO,   "do") );
        reserve( new Word(Tag.VAR,  "var") );
        text = source.toCharArray();
    }

    public void printError() {
        int i=1, k=0, j=0;
        while (i<line) {
            while (text[j] != '\n')
            {
                j++;
            }
            j++;
            i++;
        }k=lexemeBegin-j;
        System.out.println();
        while ( j<text.length && text[j]!='\n' )
        {
            System.out.print(text[j]);
            j++;
        }
        System.out.println();
        for (i = 0; i <= k; i++) {
            System.out.print(' ');
        }
        System.out.println('^');
    }
    private String lastLine() {
        int i=1, j=0;
        while (i<line) {
            while (text[j] != '\n')
            {
                j++;
            }
            j++;
            i++;
        }
        String str = "";
        while ( j<text.length && text[j]!='\n' )
        {
            System.out.print(text[j]);
            j++;
        }
        return "";
    }

    private void next() {
        forward = lexemeBegin;
        forloop:
        for ( ; ; ) {
            switch ( text[forward] )
            {
                case ' ':
                case '\t':
                    break forloop;
                case '\n':
                    line++;
                    break forloop;
                case '<':
                case '>':
                case '=':
                case '!':
                    if (text[forward] == '=')
                        forward++;
                    break forloop;
                case '/':
                    if (text[forward] == '/') {
                        while ( text[forward++] != '\n' );
                        line++;
                    }
                    else if (text[forward] == '*') {
                        while ( text[++forward] != '*' || text[forward+1] != '/' );
                    }
                    else forward++;
                    break forloop;
                default:
                    break;
            }
        }
    }

    public Token scan() {
        try {
            lexemeBegin = forward;
            char peek = text[forward++];
            for ( ; ; peek = text[forward++] )
            {
            /*
            if ( peek == ' ' || peek == '\t' ) continue;
            else if ( peek == '\n' ) line++;
            else if ( peek == '/' ) {
                peek = (char)System.in.read();
                if ( peek == '/' ) {
                    do { peek = (char)System.in.read(); } while( peek != '\n' );
                    line++;
                }
                else if ( peek == '*' ) {
                    char old; peek = ' ';
                    do {
                        old = peek;
                        peek = (char)System.in.read();
                        if ( peek == '\n' ) line++;
                    } while( old != '*' && peek != '/' );
                }
                else return new Token('/');
            }
            else if ( peek == '<' ) {
                peek = (char)System.in.read();
                if ( peek == '=' )
                    return new Token(Tag.LssOrEq);
                else return new Token(Tag.Lesser);
            }
            else if ( peek == '>' ) {
                peek = (char)System.in.read();
                if ( peek == '=' )
                    return new Token(Tag.GrtOrEq);
                else return new Token(Tag.Greater);
            }
            else if ( peek == '=' ) {
                peek = (char)System.in.read();
                if ( peek == '=' )
                    return new Token(Tag.Equals);
                else return new Token('=');
            }
            else if ( peek == '!' ) {
                peek = (char)System.in.read();
                if ( peek == '=' )
                    return new Token(Tag.NotEq);
                else return new Token('!');
            }
            else break;
             */
                switch ( peek )
                {
                    case ' ':
                    case '\t':
                        break;
                    case '\n':
                        line++;
                        break;
                    case '<':
                        if (text[forward] == '=') {
                            forward++; return new RelOp(Tag.LE);
                        }
                        else return new RelOp(Tag.LT);
                    case '>':
                        if (text[forward] == '=') {
                            forward++; return new RelOp(Tag.GE);
                        }
                        else return new RelOp(Tag.GT);
                    case '=':
                        if (text[forward] == '=') {
                            forward++; return new RelOp(Tag.EQ);
                        }
                        else return new Token(Tag.Assign);
                    case '!':
                        if (text[forward] == '=') {
                            forward++; return new RelOp(Tag.NE);
                        }
                        else return new Token(Tag.Not);
                    case '/':
                        if (text[forward] == '/') {
                            while ( text[forward++] != '\n' );
                            line++; break;
                        }
                        else if (text[forward] == '*') {
                            while ( text[++forward] != '*' && text[forward+1] != '/' );
                            forward+=2; break;
                        }
                        else return new Token('/');
                    case '[':
                    case ']':
                    case '(':
                    case ')':
                    case '{':
                    case '}':
                    case '+':
                    case '-':
                    case '*':
                    case '&':
                    case '%':
                    case ',':
                    case ';':
                        return new Token(peek);
                    default:
                        if ( Character.isDigit(peek) ) {
                            int v = 0;
                            do {
                                v = v*10 + Character.digit(peek, 10);
                                peek = text[forward++];
                            } while( Character.isDigit(peek) );
                            forward--;
                            return new Num(v);
                        }
                        if ( Character.isLetter(peek) || peek == '_' ) {
                            StringBuffer b = new StringBuffer();
                            do {
                                b.append(peek);
                                peek = text[forward++];
                            } while( Character.isLetterOrDigit(peek) || peek == '_' );
                            forward--;
                            String s = b.toString();
                            Word w = (Word)words.get(s);
                            if ( w != null ) return w;
                            w = new Word(Tag.ID, s);
                            words.put(s, w);
                            return w;
                        }
                        break;
                }
            }
        } catch ( ArrayIndexOutOfBoundsException e) {
            System.err.println("End of String.");
            return null;
        }


//        Token t = new Token( peek );
//        peek = ' ';
//        return t;
    }
}
