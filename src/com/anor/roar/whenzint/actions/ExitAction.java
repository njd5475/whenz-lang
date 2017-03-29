package com.anor.roar.whenzint.actions;

import java.io.IOException;
import java.util.Map;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.parser.Node;
import com.anor.roar.whenzint.parser.TokenBuffer;
import com.anor.roar.whenzint.parser.WhenzParser;
import com.anor.roar.whenzint.parser.WhenzSyntaxError;

public class ExitAction extends Action {

	@Override
	public void perform(Program program, Map<Object, Object> context) {
		System.exit(0);
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
        parser.unexpectedToken(tokens.peek());
      }
    }else{
      parser.unexpectedToken(tokens.peek());
    }
    return exitNode;
  }

}
