package com.anor.roar.whenzint.patterns;

import java.io.IOException;

public interface TokenAction {

  public Node buildNode(WhenzParser parser, TokenBuffer tokens) throws WhenzSyntaxError, IOException;
  
}
