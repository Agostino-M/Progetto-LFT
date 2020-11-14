package Parser;

import java.io.*;
import Lexer.*;

public class Parser {
  private Lexer lex;
  private BufferedReader pbr;
  private Token look;

  public Parser(Lexer l, BufferedReader br) {
    lex = l;
    pbr = br;
    move();
  }

  void move() {
    look = lex.lexical_scan(pbr);
    System.out.println("token = " + look);
  }

  void error(String s) {
    throw new Error("near line " + Lexer.line + ": " + s);
  }

  void match(int t) {
    if (look.tag == t) {
      if (look.tag != Tag.EOF)
        move();
    } else
      error("syntax error");
  }

  public void start() {
    System.out.println("start: " + look.tag);
    switch (look.tag) {

      // <start> → <expr>EOF
      case '(':
      case Tag.NUM: // verificare che sia corretto
        expr();
        match(Tag.EOF);
        break;

      default:
        error("start");
    }
  }

  private void expr() {
    System.out.println("expr: " + look.tag);
    switch (look.tag) {

      // <expr> → <term><exprp>
      case '(':
      case Tag.NUM: // verificare che sia corretto
        term();
        exprp();
        break;

      default:
        error("expr");
    }
  }

  private void exprp() {
    System.out.println("exprp: " + look.tag);
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
      case Tag.EOF:
      case ')':
        move();
        break;

      default:
        error("exprp");
    }
  }

  private void term() {
    System.out.println("term: " + look.tag);
    switch (look.tag) {

      // <term> → <fact><termp>
      case '(':
      case Tag.NUM: // verificare che sia corretto
        fact();
        termp();
        break;

      default:
        error("term");
    }
  }

  private void termp() {
    System.out.println("termp: " + look.tag);
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
      case Tag.EOF:
      case '+':
      case '-':
        move();
        break;

      default:
        error("termp");

    }
  }

  private void fact() {
    System.out.println("fact: " + look.tag);
    switch (look.tag) {

      // <fact> → <expr>
      case '(':
        match('(');
        expr();
        match(')');
        break;

      // <fact> → <expr>
      case Tag.NUM: // verificare che sia corretto
        match(Tag.NUM);
        break;

      default:
        error("fact");
    }
  }

  public static void main(String[] args) {
    Lexer lex = new Lexer();
    // il percorso del file da leggere
    String path = "D:/Unito/Secondo anno/Linguaggi Formali e Traduttori/Laboratorio/LFT-Project/LFT-Project/Input.txt";

    try {
      BufferedReader br = new BufferedReader(new FileReader(path));
      Parser parser = new Parser(lex, br);
      parser.start();
      System.out.println("Input OK");
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
