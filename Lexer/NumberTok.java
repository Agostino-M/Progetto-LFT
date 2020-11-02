package Lexer;

public class NumberTok extends Token {

  // ... completare ...
  public int numLexeme;

  public NumberTok(int tag, int n) {
    super(tag);
    numLexeme = n;
  }

  public String toString() {
    return "<" + tag + ", " + numLexeme + ">";
  }
}