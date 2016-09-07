package com.anor.roar.whenzint.patterns;

public class WhenzSyntaxError extends Exception {

  public WhenzSyntaxError(String message, Token t, int ln, int ch) {
    super(message + ": " + t.toString() + " at " + ln + ":" + ch);
  }

}
