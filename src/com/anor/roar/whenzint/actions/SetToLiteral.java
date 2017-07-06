package com.anor.roar.whenzint.actions;

import java.io.IOException;
import java.util.Map;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.parser.Node;
import com.anor.roar.whenzint.parser.TokenBuffer;
import com.anor.roar.whenzint.parser.WhenzParser;
import com.anor.roar.whenzint.parser.WhenzSyntaxError;

public class SetToLiteral extends Action {

  private String name;
  private Object literal;

  public SetToLiteral(String name, Object literal) {
    this.name = name;
    this.literal = literal;
  }

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens)
      throws WhenzSyntaxError, IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void perform(Program program, Map<String, Object> context) {
    program.setObject(name, literal);
  }

}
