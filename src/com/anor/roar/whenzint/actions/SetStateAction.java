package com.anor.roar.whenzint.actions;

import java.io.IOException;
import java.util.Map;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.VariablePath;
import com.anor.roar.whenzint.parser.*;

public class SetStateAction extends AbstractAction {

  static {
    ProgramBuilder.registerActionBuilder(new SetStateAction(CodeLocation.fake, null, null));
  }
  
  private VariablePath path;
  private String       state;

  public SetStateAction(CodeLocation location, VariablePath path, String state) {
    super(location);
    this.path = path;
    this.state = state;
  }

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens) throws WhenzSyntaxError, IOException {
    Node node = new Node(this.getActionNodeName());
    parser.globalReference(node, tokens);
    parser.consumeWhitespace(tokens);
    if (tokens.peek().is("is")) {
      tokens.take();
      parser.consumeWhitespace(tokens);
      Node ident = parser.identifier(tokens);
      node.add(ident);
      parser.consumeWhitespace(tokens);
    } else {
      parser.unexpectedToken(tokens);
    }
    return node;
  }

  @Override
  public Action buildAction(ProgramBuilder builder, Node node) throws WhenzSyntaxTreeError {
    if (node.isNamed(getActionNodeName())) {
      VariablePath path = builder.getPath(node.getChildNamed("Reference"));
      String name = node.getChildNamed("Identifier").getTokenOrValue();
      return new SetStateAction(CodeLocation.toLocation(node), path, name);
    }
    return null;
  }

  @Override
  public String getActionNodeName() {
    return "SetState";
  }

  @Override
  public void perform(Program program, Map<String, Object> context) {
    program.changeState(path.getFullyQualifiedName(), this.state);
  }

}
