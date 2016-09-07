package com.anor.roar.whenzint.patterns;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class TokenStreamReader extends Reader {

  private Reader stream;
  private Token  current;

  public TokenStreamReader(Reader stream) {
    if (stream == null) {
      throw new NullPointerException("Need a valid stream to create tokens");
    }
    this.stream = stream;
  }

  public Token readToken() throws IOException {
    Token next = current;
    Token toRet = null;
    int r = -1;
    do {
      r = read();

      if (r == -1 && current == null) {
        return null;
      } else  if (r == -1 && current != null) {
        toRet = current;
        current = null;
        break;
      } else {

        if (next == null) {
          next = new Token((char) r, 1, 0);
        } else {
          next = next.addLex((char) r);
        }

        if (next != current && current != null) {
          toRet = current;
          current = next;
          break;
        }

        current = next;
      }
    } while (true);

    return toRet;
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    return stream.read(cbuf, off, len);
  }

  @Override
  public void close() throws IOException {
    stream.close();
  }

  public static void main(String... args) throws IOException {
    TokenStreamReader tsr = new TokenStreamReader(
        new FileReader("./scripts/hello.whenz"));

    Token t = null;
    while ((t = tsr.readToken()) != null) {
      System.out.println(t);
    }
  }
}
