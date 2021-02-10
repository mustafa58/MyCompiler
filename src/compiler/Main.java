package compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
	// write your code here
        Parser p;
        try {
            File in = new File(args[0]);
            Scanner sc = new Scanner(in);
            String txt = "";
            while (sc.hasNextLine()) {
                String data = sc.nextLine();
                //System.out.println(data);
                txt += data + "\n";
            }
            p = new Parser(txt);
            File out = new File(args[0]+".vvm");
            FileOutputStream f = new FileOutputStream(out);
            String o=p.program().replaceAll("\n", "\r\n");
            f.write(o.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Lexer l = new Lexer(" _ajk5!8; = 5 //asadsap\n /*comment here*/>= abcd ; ");
        //Lexer l = new Lexer(" var a[3] [  2];");
        //Parser p = new Parser("{var i=3,j=i=i+1; i=j=0; **i=&j; while(i<5) i=i*5; }");
        //p = new Parser("{var i=30; var j=&i; }//*i=j; j=*(i+5); }");
        //Parser p = new Parser("{ var i=0; var j=1; while(i<5) {i=i+1;j=j*i;} }");
        //Parser p = new Parser("{ var i=0; var a[i+5] = {1,2,3,4,5}; a[i+8]=a[0]; }");
        //Parser p = new Parser("{ var i,j=7,t=0; for (i = 0; i < j; i=i+1) {t=t+i;} }");
        //p.program();
        /*for ( ; ; ) {
            System.out.println(l.scan().toString());
        }
        /*ItmCodeGen p = new ItmCodeGen();
        Symbol a = new Symbol(Tag.ID, "a");
        Symbol b = new Symbol(Tag.NUM, 5);
        int i,j;
        i = p.Leaf(a);
        j = p.Leaf(b);
        p.Node('=', p.Leaf(new Symbol(Tag.ID, "result")), p.Node('+', i,j));
        System.out.println(p.toString());*/
    }
}
