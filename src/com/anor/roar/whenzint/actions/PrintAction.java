package com.anor.roar.whenzint.actions;

import java.io.IOException;
import java.util.Map;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.parser.Node;
import com.anor.roar.whenzint.parser.ProgramBuilder;
import com.anor.roar.whenzint.parser.TokenBuffer;
import com.anor.roar.whenzint.parser.WhenzParser;
import com.anor.roar.whenzint.parser.WhenzSyntaxError;

public class PrintAction extends Action {

  private String toPrint;
  
  static {
    ProgramBuilder.registerActionBuilder(new PrintAction(null));
  }

  public PrintAction(String toPrint) {
    this.toPrint = toPrint;
  }

  @Override
  public void perform(Program program, Map<String, Object> context) {
    System.out.println(toPrint);
  }

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens)
      throws WhenzSyntaxError, IOException {
    Node printAction = new Node("PrintAction");
    if (tokens.peek().is("print")) {
      tokens.take();
      parser.consumeWhitespace(tokens);
      while (!tokens.peek().isNewline()) {
        printAction.add(new Node("string part", tokens.take()));
      }
      if (tokens.peek().isNewline()) {
        tokens.take(); // consume the newline token
      } else {
        parser.unexpectedToken(tokens.peek());
      }
    } else {
      parser.unexpectedToken(tokens.peek());
    }
    return printAction;
  }

  @Override
  public Action buildAction(ProgramBuilder builder, Node node) {
    StringBuilder printStr = new StringBuilder("");
    for (Node part : node.children()) {
      printStr.append(part.getToken());
    }
    return new PrintAction(printStr.toString());
  }

  @Override
  public String getActionNodeName() {
    return "PrintAction";
  }

}
