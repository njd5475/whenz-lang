package com.anor.roar.whenzint.parser;

import java.io.IOException;

public class WhenzSyntaxError extends Exception {

  private Node        tree;
  private int         line;
  private int         column;
  private String      message;
  private Token       token;
  private TokenBuffer tokenBuffer;
  private IOException tokenPeekError;
  private String defaultMessage;

  public WhenzSyntaxError(String message, TokenBuffer t, int ln, int ch) {
    super(String.format("%s: %s at line=%d col=%d", message, t.toString(), ln, ch));
    this.line = ln;
    this.column = ch;
    try {
      this.token = t.peek();
    } catch (IOException ioe) {
      this.tokenPeekError = ioe;
    }
    tokenBuffer = t;
    this.message = message;
    this.defaultMessage = String.format("%s: %s at line=%d col=%d", message, t.toString(), ln, ch);
  }

  public WhenzSyntaxError(String message, TokenBuffer t, int ln, int ch, Node top) {
    this(message, t, top);
  }


  public WhenzSyntaxError(String message, TokenBuffer tok) {
    this(message, tok, (Node)null);
  }
  
  public WhenzSyntaxError(String message, TokenBuffer tok, Node top) {
    super(message);
    this.line = -1;
    this.column = -1;
    try {
      this.token = tok.peek();
      this.line = tok.peek().getLine();
      this.column = tok.peek().getChar();
    } catch (IOException ioe) {
      this.tokenPeekError = ioe;
    }
    this.tokenBuffer = tok;
    this.tree = top;
    this.message = message;
    this.defaultMessage = String.format("%s: '%s' at line=%d col=%d", message, token.asString(), line, column);
  }
  
  public int getLine() {
    return line;
  }
  
  public int getColumn() {
    return column;
  }
  
  public String getDefaultFormattedMessage() {
    return defaultMessage;
  }

  @Override
  public void printStackTrace() {
    System.err.println(tree);
    super.printStackTrace();
  }

}
