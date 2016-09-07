package com.anor.roar.whenzint.patterns;

public class WhenzSyntaxError extends Exception {

  private Node tree;

  public WhenzSyntaxError(String message, Token t, int ln, int ch) {
    super(message + ": " + t.toString() + " at " + ln + ":" + ch);
  }

  public WhenzSyntaxError(String message, Token t, int ln, int ch,
      Node top) {
    this(message, t, ln, ch);
    this.tree = top;
  }

  @Override
  public void printStackTrace() {
    System.err.println(tree);
    super.printStackTrace();
  }

}
