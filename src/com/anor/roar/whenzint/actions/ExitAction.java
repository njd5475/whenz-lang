package com.anor.roar.whenzint.actions;

import java.io.IOException;
import java.util.Map;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.parser.*;

public class ExitAction extends AbstractAction {

  static {
    ProgramBuilder.registerActionBuilder(new ExitAction(CodeLocation.fake));
  }

  public ExitAction(CodeLocation location) {
    super(location);
  }
  
	@Override
	public void perform(Program program, Map<String, Object> context) {
		program.exit(0);
	}

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens)
      throws WhenzSyntaxError, IOException {
    Node exitNode = new Node("Exit");
    if(tokens.peek().is("exit")) {
      tokens.take();
      parser.consumeWhitespace(tokens);
      if(tokens.peek().isNewline()) {
        tokens.take();
      }else{
        parser.unexpectedToken(tokens);
      }
    }else{
      parser.unexpectedToken(tokens);
    }
    return exitNode;
  }

  @Override
  public Action buildAction(ProgramBuilder builder, Node node) {
    return new ExitAction(CodeLocation.fake);
  }

  @Override
  public String getActionNodeName() {
    return "Exit";
  }

}
