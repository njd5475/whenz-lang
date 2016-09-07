package com.anor.roar.whenzint.patterns;

import java.io.IOException;

public interface TokenBuffer {

  public Token take() throws IOException;
  
  public Token peek() throws IOException;

  public boolean isEmpty();
  
}
