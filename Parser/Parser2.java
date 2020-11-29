package Parser;

import java.io.*;
import Lexer.*;

public class Parser2 {

  private Lexer lex;
  private BufferedReader pbr;
  private Token look;

  public Parser2(Lexer l, BufferedReader br) {
    lex = l;
    pbr = br;
    move();
  }

  void move() {
    look = lex.lexical_scan(pbr);
    System.out.println("token = " + look);
  }

  void error(String s) {
    throw new Error("near line " + Lexer.line + ": " + s + " token: " + look.tag);
  }

  void match(int t) {
    if (look.tag == t) {
      if (look.tag != Tag.EOF)
        move();
    } else
      error("Syntax Error: instead of " + t + " found");
  }

  public void prog() { // P
    switch (look.tag) {
      // <prog> → <statlist> EOF
      case '=', Tag.PRINT, Tag.READ, Tag.COND, Tag.WHILE, '{':
        statlist();
        match(Tag.EOF);
        break;

      default:
        error("Prog");
    }
  }

  private void statlist() { // Sl
    switch (look.tag) {
      // <statlist> → <stat> <statlistp>
      case '=', Tag.PRINT, Tag.READ, Tag.COND, Tag.WHILE, '{':
        stat();
        statlistp();
        break;

      default:
        error("Statlist");
    }
  }

  private void statlistp() { // Slp
    switch (look.tag) {
      // <statlistp> → <stat> <statlistp>
      case ';':
        match(';');
        stat();
        statlistp();
        break;

      // <statlistp> → ε
      case Tag.EOF, '}':
        break;

      default:
        error("Statlistp");
    }
  }

  private void stat() { // S
    switch (look.tag) {
      // <stat> → = ID <expr>
      case '=':
        match('=');
        match(Tag.ID);
        expr();
        break;

      // <stat> → print(<expr>)
      case Tag.PRINT:
        match(Tag.PRINT);
        match('(');
        exprlist();
        match(')');
        break;

      // <stat> → read(ID)
      case Tag.READ:
        match(Tag.READ);
        match('(');
        match(Tag.ID);
        match(')');
        break;

      // <stat> → cond <whenlist> else <stat>
      case Tag.COND:
        match(Tag.COND);
        whenlist();
        match(Tag.ELSE);
        stat();
        break;

      // <stat> → while(<bexpr>) <stat>
      case Tag.WHILE:
        match(Tag.WHILE);
        match('(');
        bexpr();
        match(')');
        stat();
        break;

      // <stat> → {<statlist>}
      case '{':
        match('{');
        statlist();
        match('}');
        break;

      default:
        error("Stat");
    }
  }

  private void whenlist() { // Wl
    switch (look.tag) {
      // <whenlist> → <whenitem> <whenlistp>
      case Tag.WHEN:
        whenitem();
        whenlistp();
        break;

      default:
        error("Whenlist");
    }
  }

  private void whenlistp() { // Wlp
    switch (look.tag) {
      // <whenlistp> → <whenitem> <whenlistp>
      case Tag.WHEN:
        whenitem();
        whenlistp();
        break;

      // <whenlistp> → ε
      case Tag.ELSE:
        break;

      default:
        error("Whenlistp");
    }
  }

  private void whenitem() { // Wi
    switch (look.tag) {
      // <whenitem> → when(<bexpr>) do <stat>
      case Tag.WHEN:
        match(Tag.WHEN);
        match('(');
        bexpr();
        match(')');
        match(Tag.DO);
        stat();
        break;

      default:
        error("Whenitem");
    }
  }

  private void bexpr() { // B
    switch (look.tag) {
      // <bexpr> → RELOP <expr> <expr>
      case Tag.RELOP:
        match(Tag.RELOP);
        expr();
        expr();
        break;

      default:
        error("Bexpr");
    }
  }

  private void expr() { // E
    switch (look.tag) {
      // <expr> → + <exprlist>
      case '+':
        match('+');
        match('(');
        exprlist();
        match(')');
        break;

      // <expr> → - <expr> <expr>
      case '-':
        match('-');
        expr();
        expr();
        break;

      // <expr> → * <exprlist>
      case '*':
        match('*');
        match('(');
        exprlist();
        match(')');
        break;

      // <expr> → / <expr> <expr>
      case '/':
        match('/');
        expr();
        expr();
        break;

      // <expr> → NUM
      case Tag.NUM:
        match(Tag.NUM);
        break;

      // <expr> → ID
      case Tag.ID:
        match(Tag.ID);
        break;

      default:
        error("Expr");
    }
  }

  private void exprlist() { // El
    switch (look.tag) {
      // <exprlist> → <expr> <exprlistp>
      case '+', '-', '/', Tag.NUM, Tag.ID:
        expr();
        exprlistp();
        break;

      default:
        error("Exprlist");
    }
  }

  private void exprlistp() { // Elp
    switch (look.tag) {
      // <exprlistp> → <expr> <exprlistp>
      case '+', '-', '*', '/', Tag.NUM, Tag.ID:
        expr();
        exprlistp();
        break;

      // <exprlistp> → ε
      case ')':
        break;

      default:
        error("Exprlistp");
    }
  }

  public static void main(String[] args) {
    Lexer lex = new Lexer();
    // il percorso del file da leggere
    String path = "Input.txt";

    try {
      BufferedReader br = new BufferedReader(new FileReader(path));
      Parser2 parser = new Parser2(lex, br);

      parser.prog();
      System.out.println("Input OK");
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
