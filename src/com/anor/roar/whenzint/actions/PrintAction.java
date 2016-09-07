package com.anor.roar.whenzint.actions;

import java.io.IOException;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.parser.Node;
import com.anor.roar.whenzint.parser.TokenBuffer;
import com.anor.roar.whenzint.parser.WhenzParser;
import com.anor.roar.whenzint.parser.WhenzSyntaxError;

public class PrintAction extends Action {

  private String toPrint;

  public PrintAction(String toPrint) {
    this.toPrint = toPrint;
  }

  @Override
  public void perform(Program program) {
    System.out.println(toPrint);
  }

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens)
      throws WhenzSyntaxError, IOException {
    Node printAction = new Node("PrintAction");
    if (tokens.peek().is("print")) {
      tokens.take();
      parser.consumeWhitespace(tokens);
      while(!tokens.peek().isNewline()) {
        printAction.add(new Node("string part", tokens.take()));
      }
      tokens.take(); //consume the newline token
    } else {
      parser.unexpectedToken(tokens.peek());
    }
    return printAction;
  }

}
