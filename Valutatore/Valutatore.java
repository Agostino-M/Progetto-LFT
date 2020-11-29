package Valutatore;

import java.io.*;
import Lexer.*;

public class Valutatore {
  private Lexer lex;
  private BufferedReader pbr;
  private Token look;

  public Valutatore(Lexer l, BufferedReader br) {
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
    int expr_val;
    switch (look.tag) {
      // <start> → <expr>EOF
      case '(', Tag.NUM:
        expr_val = expr();
        match(Tag.EOF);
        System.out.println(expr_val);
        break;

      default:
        error("Start");
    }
  }

  private int expr() {
    int term_val, exprp_val = 0;
    switch (look.tag) {
      // <expr> → <term><exprp>
      case '(', Tag.NUM:
        term_val = term();
        exprp_val = exprp(term_val);
        break;

      default:
        error("Expr");
    }
    return exprp_val;
  }

  private int exprp(int exprp_i) {
    int term_val, exprp_val = 0;
    switch (look.tag) {
      // <exprp> → +<term><exprp>
      case '+':
        match('+');
        term_val = term();
        exprp_val = exprp(exprp_i + term_val);
        break;

      // <exprp> → -<term><exprp>
      case '-':
        match('-');
        term_val = term();
        exprp_val = exprp(exprp_i - term_val);
        break;

      // <exprp> → ε
      case Tag.EOF, ')':
        exprp_val = exprp_i;
        break;

      default:
        error("Exprp");
    }
    return exprp_val;
  }

  private int term() {
    int fact_val, termp_val = 0;
    switch (look.tag) {
      // <term> → <fact><termp>
      case '(', Tag.NUM:
        fact_val = fact();
        termp_val = termp(fact_val);
        break;

      default:
        error("Term");
    }
    return termp_val;
  }

  private int termp(int termp_i) {
    int fact_val, termp_val = 0;
    switch (look.tag) {
      // <termp> → *<fact><termp>
      case '*':
        match('*');
        fact_val = fact();
        termp_val = termp(termp_i * fact_val);
        break;

      // <termp> → /<fact><termp>
      case '/':
        match('/');
        fact_val = fact();
        termp_val = termp(termp_i / fact_val);
        break;

      // <termp> → ε
      case Tag.EOF, '+', '-', ')':
        termp_val = termp_i;
        break;

      default:
        error("Termp");
    }
    return termp_val;
  }

  private int fact() {
    int fact_val = 0;
    switch (look.tag) {
      // <fact> → <expr>
      case '(':
        match('(');
        fact_val = expr();
        match(')');
        break;

      // <fact> → <expr>
      case Tag.NUM:
        NumberTok NUM_value = (NumberTok) look;
        fact_val = NUM_value.numLexeme;
        match(Tag.NUM);

        break;

      default:
        error("Fact");
    }
    return fact_val;
  }

  public static void main(String[] args) {
    Lexer lex = new Lexer();
    String path = "Input.txt"; // il percorso del file da leggere
    try {
      BufferedReader br = new BufferedReader(new FileReader(path));
      Valutatore valutatore = new Valutatore(lex, br);
      valutatore.start();
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
