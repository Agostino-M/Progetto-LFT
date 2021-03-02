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
    while (peek == ' ' || peek == '\t' || peek == '\n' || peek == '\r' || peek == '/') {
      // escludo il controllo del contenuto dei commenti
      if (peek == '/') {
        readch(br);

        if (peek == '/') { // commento monolinea //
          while (peek != '\n' && peek != (char) -1) { // ignoro tutto fino alla fine della riga o del file
            readch(br);
          }
        } else if (peek == '*') { // commento multilinea
          boolean close = false;
          while (peek != (char) -1 && !close) { // ignoro tutto fino a EOF o quando viene chiuso il commento
            readch(br);
            while (peek == '*') { // se trovo un *, ciclo finché ne trovo altri
              readch(br);
              if (peek == '/')
                close = true;
            }
          }
          if (peek == (char) -1) {
            System.err.println("Error: Multi line comment never got closed.");
            return null;
          }

        } else {
          return Token.div;
        }
      }

      if (peek == '\n')
        line++;

      readch(br);
    }

    switch (peek) {
      case '!':
        peek = ' ';
        return Token.not;

      // Casi di (, ), {, }, +, -, *, /, ;
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

        // Casi di ||, <, >, <=, >=, ==, <>, =
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
        if (Character.isLetter(peek) || peek == '_') {
          // Caso degli identificatori e delle parole chiave
          String words = "";
          while (peek == '_') {// controllo se sono solo underscore
            words += "_";
            readch(br);
            if (!Character.isDigit(peek) && !Character.isLetter(peek)) { // ho letto solo underscore
              System.err.println("Erroneous character" + " after _ : " + peek);
              return null;
            }
          }

          while (Character.isLetter(peek) || Character.isDigit(peek) || peek == '_') { // leggiamo lettere, numeri o _
            words += peek;
            readch(br);
          }
          switch (words) { // caso delle parole chiave
            case "cond":
              return Word.cond;
            case "when":
              return Word.when;
            case "then":
              return Word.then;
            case "else":
              return Word.elsetok;
            case "while":
              return Word.whiletok;
            case "do":
              return Word.dotok;
            case "seq":
              return Word.seq;
            case "print":
              return Word.print;
            case "read":
              return Word.read;
            default:
              return new Word(Tag.ID, words);
          }

        } else if (Character.isDigit(peek)) {
          // Caso dei numeri
          if (peek == '0') {
            readch(br);

            if (!Character.isDigit(peek)) { // ho letto solo 0
              return new NumberTok(Tag.NUM, 0);
            } else {
              System.err.println("Erroneous character" + " after 0 : " + peek);
              return null;
            }
          }

          // non inizia per 0
          String number = "" + peek;
          while (Character.isDigit(peek)) { // leggiamo i numeri
            readch(br);
            if (Character.isDigit(peek)) {
              number += peek;
            } else {
              return new NumberTok(Tag.NUM, Integer.parseInt(number));
            }
          }

        } else { // caratteri speciali non riconosciuti
          System.err.println("Erroneous character: " + peek);
          return null;
        }
    }
    return null;
  }

  public static void main(String[] args) {
    Lexer lex = new Lexer();
    // il percorso del file da leggere
    String path = "Input.lft";

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