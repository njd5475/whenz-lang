package com.anor.roar.whenzint.actions;

import java.io.IOException;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.patterns.Node;
import com.anor.roar.whenzint.patterns.TokenBuffer;
import com.anor.roar.whenzint.patterns.WhenzParser;
import com.anor.roar.whenzint.patterns.WhenzSyntaxError;

public class ExitAction extends Action {

	@Override
	public void perform(Program program) {
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
