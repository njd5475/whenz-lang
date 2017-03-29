package com.anor.roar.whenzint.actions;

import java.io.IOException;
import java.util.Map;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.parser.Node;
import com.anor.roar.whenzint.parser.TokenBuffer;
import com.anor.roar.whenzint.parser.WhenzParser;
import com.anor.roar.whenzint.parser.WhenzSyntaxError;

public class RunShellCommand extends Action {

  private String varName;

  public RunShellCommand(String varName) {
    this.varName = varName;
  }

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens) throws WhenzSyntaxError, IOException {
    
    return null;
  }

  @Override
  public void perform(Program program, Map<Object, Object> context) {
    // TODO Auto-generated method stub

  }

}
