package com.anor.roar.whenzint.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StreamTokenBuffer implements TokenBuffer {

  private List<Token>       tokens;
  private int               bufferSize;
  private TokenStreamReader reader;
  private boolean           endOfFileFound = false;

  public StreamTokenBuffer(TokenStreamReader reader, int bufferSize) {
    this.bufferSize = bufferSize;
    this.tokens = new ArrayList<Token>(bufferSize);
    this.reader = reader;
  }

  private void fillBuffer() throws IOException {
    int amountToRead = bufferSize - tokens.size();
    if (amountToRead > 0) {
      Token t = null;
      while (tokens.size() != bufferSize) {
        t = reader.readToken();
        if (t != null) {
          tokens.add(t);
        } else {
          endOfFileFound = true;
          break;
        }
      }
    }
  }

  public boolean isEmpty() {
    return tokens.isEmpty() && endOfFileFound;
  }

  public Token peek() throws IOException {
    if (tokens.isEmpty() && !endOfFileFound) {
      fillBuffer();
    }

    if (isEmpty()) {
      return Token.eof();
    }

    return tokens.get(0);
  }

  public Token take() throws IOException {
    if (tokens.isEmpty() && !endOfFileFound) {
      fillBuffer();
    }

    if (isEmpty()) {
      return Token.eof();
    }

    return tokens.remove(0);
  }
}
