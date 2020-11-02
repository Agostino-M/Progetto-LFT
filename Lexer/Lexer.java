package Lexer;

import java.io.*;

public class Lexer {

  public static int line = 1;
  private char peek = ' ';

  private void readch(BufferedReader br) {
    try {
      peek = (char) br.read();
    } catch (IOException exc) {
      peek = (char) -1; // ERROR
    }
  }

  public Token lexical_scan(BufferedReader br) {
    while (peek == ' ' || peek == '\t' || peek == '\n' || peek == '\r') {
      if (peek == '\n')
        line++;
      readch(br);
    }

    switch (peek) {
      case '!':
        peek = ' ';
        return Token.not;

      // ... gestire i casi di (, ), {, }, +, -, *, /, ; ... //
      case '(':
        peek = ' ';
        return Token.lpt;

      case ')':
        peek = ' ';
        return Token.rpt;

      case '{':
        peek = ' ';
        return Token.lpg;

      case '}':
        peek = ' ';
        return Token.rpg;

      case '+':
        peek = ' ';
        return Token.plus;

      case '-':
        peek = ' ';
        return Token.minus;

      case '*':
        peek = ' ';
        return Token.mult;

      case '/':
        peek = ' ';
        return Token.div;

      case ';':
        peek = ' ';
        return Token.semicolon;

      case '&':
        readch(br);
        if (peek == '&') {
          peek = ' ';
          return Word.and;
        } else {
          System.err.println("Erroneous character" + " after & : " + peek);
          return null;
        }

        // ... gestire i casi di ||, <, >, <=, >=, ==, <>, = ... //
      case '|':
        readch(br);
        if (peek == '|') {
          peek = ' ';
          return Word.or;
        } else {
          System.err.println("Erroneous character" + " after | : " + peek);
          return null;
        }

      case '<':
        readch(br);
        if (peek == '>') {
          peek = ' ';
          return Word.ne;
        } else if (peek == '=') {
          peek = ' ';
          return Word.le;
        } else
          return Word.lt;

      case '>':
        readch(br);
        if (peek == '=') {
          peek = ' ';
          return Word.ge;
        } else
          return Word.gt;

      case '=':
        readch(br);
        if (peek == '=') {
          peek = ' ';
          return Word.eq;
        } else
          return Token.assign;

      case (char) -1:
        return new Token(Tag.EOF);

      default:
        if (Character.isLetter(peek)) {

          // ... gestire il caso degli identificatori e delle parole chiave //
        } else if (Character.isDigit(peek)) {
          // ... gestire il caso dei numeri ... //
          if (peek == '0') {
            readch(br);

            if (!Character.isDigit(peek)) {
              peek = ' ';
              return new NumberTok(Tag.NUM, 0);
            } else {
              System.err.println("Erroneous character" + " after & : " + peek);
              return null;
            }
          }

          String number = "" + peek;
          while (Character.isDigit(peek)) {
            readch(br);
            if (Character.isDigit(peek)) {
              number += peek;
            } else {
              return new NumberTok(Tag.NUM, Integer.parseInt(number));
            }
          }

        } else {
          System.err.println("Erroneous character: " + peek);
          return null;
        }
    }
    return null;
  }

  public static void main(String[] args) {
    Lexer lex = new Lexer();
    String path = "D:/Unito/Secondo anno/Linguaggi Formali e Traduttori/Laboratorio/Esercizi_2/Input.txt"; // il
                                                                                                           // percorso
                                                                                                           // del file
                                                                                                           // da leggere
    try {
      BufferedReader br = new BufferedReader(new FileReader(path));
      Token tok;
      do {
        tok = lex.lexical_scan(br);
        System.out.println("Scan: " + tok);
      } while (tok.tag != Tag.EOF);
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}