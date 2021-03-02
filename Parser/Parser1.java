package Parser;

import java.io.*;
import Lexer.*;

public class Parser1 {
  private Lexer lex;
  private BufferedReader pbr;
  private Token look;

  public Parser1(Lexer l, BufferedReader br) {
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

  public void start() {
    switch (look.tag) {
      // <start> → <expr>EOF
      case '(', Tag.NUM:
        expr();
        match(Tag.EOF);
        break;

      default:
        error("Start");
    }
  }

  private void expr() {
    switch (look.tag) {
      // <expr> → <term><exprp>
      case '(', Tag.NUM:
        term();
        exprp();
        break;

      default:
        error("Expr");
    }
  }

  private void exprp() {
    switch (look.tag) {
      // <exprp> → +<term><exprp>
      case '+':
        match('+');
        term();
        exprp();
        break;

      // <exprp> → -<term><exprp>
      case '-':
        match('-');
        term();
        exprp();
        break;

      // <exprp> → ε
      case Tag.EOF, ')':
        break;

      default:
        error("Exprp");
    }
  }

  private void term() {
    switch (look.tag) {
      // <term> → <fact><termp>
      case '(', Tag.NUM:
        fact();
        termp();
        break;

      default:
        error("Term");
    }
  }

  private void termp() {
    switch (look.tag) {
      // <termp> → *<fact><termp>
      case '*':
        match('*');
        fact();
        termp();
        break;

      // <termp> → /<fact><termp>
      case '/':
        match('/');
        fact();
        termp();
        break;

      // <termp> → ε
      case Tag.EOF, '+', '-', ')':
        break;

      default:
        error("Termp");
    }
  }

  private void fact() {
    switch (look.tag) {
      // <fact> → <expr>
      case '(':
        match('(');
        expr();
        match(')');
        break;

      // <fact> → <expr>
      case Tag.NUM:
        match(Tag.NUM);
        break;

      default:
        error("Fact");
    }
  }

  public static void main(String[] args) {
    Lexer lex = new Lexer();
    // il percorso del file da leggere
    String path = "Input.lft";

    try {
      BufferedReader br = new BufferedReader(new FileReader(path));
      Parser1 parser = new Parser1(lex, br);

      parser.start();
      System.out.println("Input OK");
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
