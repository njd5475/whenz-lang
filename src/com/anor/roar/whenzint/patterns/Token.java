package com.anor.roar.whenzint.patterns;

public class Token {

  public enum TTYPE {
    NUMBER, IDENTIFIER, OPERATION, QUOTE, WHITESPACE, NEWLINE, SYMBOL, UNKNOWN
  };

  private TTYPE         type;
  private StringBuilder token = new StringBuilder("");
  private int           line  = 1;
  private int           ch    = 0;

  public Token(char c, int line, int ch) {
    token.append(c);
    type = isType(c);
    this.line = line;
    this.ch = ch;
  }

  public Token addLex(char c) {
    if (isType(c) == type) {
      token.append(c);
      return this;
    }

    int ln = line;
    int chr = ch;
    if (c == '\n' || isNewline()) {
      ++ln;
      chr = 0;
    }

    return new Token(c, ln, chr + token.length());
  }

  public TTYPE isType(char c) {
    if (c >= '0' && c <= '9') {
      return TTYPE.NUMBER;
    } else if (c == '+' || c == '-' || c == '/' || c == '*') {
      return TTYPE.OPERATION;
    } else if (c == '"' || c == '\'') {
      return TTYPE.QUOTE;
    } else if (c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
      return TTYPE.IDENTIFIER;
    } else if (c == ' ' || c == '\t') {
      return TTYPE.WHITESPACE;
    } else if (c == '\n' || c == '\r') {
      return TTYPE.NEWLINE;
    } else if (oneOf(c, '=', '.', '|', ':', '(', ')')) {
      return TTYPE.SYMBOL;
    }
    return TTYPE.UNKNOWN;
  }

  private boolean oneOf(char test, char... chars) {
    for (char ch : chars) {
      if (ch == test) {
        return true;
      }
    }
    return false;
  }

  private boolean not(TTYPE t) {
    return type != null && (type != t);
  }

  public String toString() {
    return type.name() + "[" + token.toString().replaceAll("[\n\r]", "<NL>")
        + "]";
  }

  public String asString() {
    return token.toString();
  }

  public boolean isWhitespace() {
    return type == TTYPE.WHITESPACE;
  }

  public boolean isNewline() {
    return type == TTYPE.NEWLINE;
  }

  public boolean isIdentifier() {
    return type == TTYPE.IDENTIFIER;
  }
  
  public boolean isNumber() {
    return type == TTYPE.NUMBER;
  }

  public boolean isNot(String term) {
    return !token.toString().equals(term);
  }

  public boolean isSymbol() {
    return type == TTYPE.SYMBOL;
  }

  public boolean is(String term) {
    return token.toString().equals(term);
  }

  public int getChar() {
    return ch;
  }

  public int getLine() {
    return line;
  }

  public int asNumber() {
    return Integer.parseInt(token.toString());
  }

}
