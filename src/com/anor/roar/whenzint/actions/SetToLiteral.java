package com.anor.roar.whenzint.actions;

import java.io.IOException;
import java.util.Map;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.parser.Node;
import com.anor.roar.whenzint.parser.ProgramBuilder;
import com.anor.roar.whenzint.parser.TokenBuffer;
import com.anor.roar.whenzint.parser.TrackableTokenBuffer;
import com.anor.roar.whenzint.parser.WhenzParser;
import com.anor.roar.whenzint.parser.WhenzSyntaxError;

public class SetToLiteral extends Action {

  private String name;
  private Object literal;

  static {
    ProgramBuilder.registerActionBuilder(new SetToLiteral(null, null));
  }

  public SetToLiteral(String name, Object literal) {
    this.name = name;
    this.literal = literal;
  }

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens) throws WhenzSyntaxError, IOException {
    Node assignNode = new Node("Assignment");
    parser.globalReference(assignNode, tokens);
    parser.consumeWhitespace(tokens);
    parser.assignment(assignNode, tokens);
    parser.consumeWhitespace(tokens);
    parser.expression(assignNode, tokens);
    parser.consumeWhitespace(tokens, true);
    return assignNode;
  }

  @Override
  public void perform(Program program, Map<String, Object> context) {
    program.setObject(name, literal);
  }

  @Override
  public Action buildAction(ProgramBuilder builder, Node node) {
    Node lval = node.children()[0];
    Node rval = node.children()[2];
    String quickRef = builder.referenceString(lval.children());
    if ("Literals".equals(rval.name())) {
      return new SetToLiteral(quickRef, rval.children()[0].getTokenOrValue());
    } else if ("Number".equals(rval.name())) {
      return new SetToLiteral(quickRef, Integer.parseInt(rval.children()[0].getTokenOrValue()));
    } else if ("HexLiteral".equals(rval.name())) {
      return new SetToLiteral(quickRef, Integer.parseInt(rval.children()[0].getTokenOrValue(), 16));
    }
    return null;
  }

  @Override
  public String getActionNodeName() {
    return "Assignment";
  }

}
