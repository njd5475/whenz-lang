package com.anor.roar.whenzint.patterns;

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
      while ((t = reader.readToken()) != null && tokens.size() != bufferSize) {
        tokens.add(t);
      }
      if(t == null) {
        endOfFileFound = true;
      }
    }
  }

  public boolean isEmpty() {
    return tokens.isEmpty() && endOfFileFound;
  }

  public Token peek() throws IOException {
    if(tokens.isEmpty() && !endOfFileFound) {
      fillBuffer();
    }
    
    return tokens.get(0);
  }
  
  public Token take() throws IOException { 
    if(tokens.isEmpty() && !endOfFileFound) {
      fillBuffer();
    }
    return tokens.remove(0);
  }
}
