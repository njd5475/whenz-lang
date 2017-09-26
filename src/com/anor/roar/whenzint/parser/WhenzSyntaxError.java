package com.anor.roar.whenzint.parser;

public class WhenzSyntaxError extends Exception {

  private Node tree;

  public WhenzSyntaxError(String message, Token t, int ln, int ch) {
    super(message + ": " + t.toString() + " at line=" + ln + " col=" + ch);
  }

  public WhenzSyntaxError(String message, Token t, int ln, int ch,
      Node top) {
    this(message, t, ln, ch);
    this.tree = top;
  }

  public WhenzSyntaxError(String message, Token tok) {
    this(message, tok, tok.getLine(), tok.getChar());
  }

  @Override
  public void printStackTrace() {
    System.err.println(tree);
    super.printStackTrace();
  }

}
