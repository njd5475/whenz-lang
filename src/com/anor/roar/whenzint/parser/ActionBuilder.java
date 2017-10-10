package com.anor.roar.whenzint.parser;

import com.anor.roar.whenzint.Action;

public interface ActionBuilder {

  public Action buildAction(ProgramBuilder builder, Node node);
  
  public String getActionNodeName();
  
}
