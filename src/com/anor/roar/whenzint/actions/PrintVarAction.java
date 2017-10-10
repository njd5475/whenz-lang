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

public class PrintVarAction extends Action {

  private String varName;
  
  static {
    ProgramBuilder.registerActionBuilder(new PrintVarAction(null));
  }

  public PrintVarAction(String varName) {
    this.varName = varName;
  }

  @Override
  public void perform(Program program, Map<String, Object> context) {
    Object o = program.getObject(varName);
    if (o != null) {
      System.out.println(o.toString());
    }
  }

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens)
      throws WhenzSyntaxError, IOException {

    Node printVarAction = new Node("PrintVar");
    if (tokens.peek().is("printvar")) {
      tokens.take();
      parser.consumeWhitespace(tokens);
      if (tokens.peek().isSymbol("@")) {
        tokens.take();
        Node globalRef = new Node("GlobalVariable");
        while (tokens.peek().isWord()) {
          globalRef.add(new Node("part", tokens.take()));
          if (tokens.peek().isSymbol(".")) {
            tokens.take();
          } else if (tokens.peek().isNewline()) {
            break;
          } else {
            parser.unexpectedToken(tokens.peek());
          }
        }
        printVarAction.add(globalRef);
      }

      if (tokens.peek().isNewline()) {
        tokens.take(); // consume the newline token
      } else {
        parser.unexpectedToken(tokens.peek());
      }
    } else {
      parser.unexpectedToken(tokens.peek());
    }
    return printVarAction;
  }

  @Override
  public Action buildAction(ProgramBuilder builder, Node node) {
    Node global = node.children()[0];
    StringBuilder printStr = new StringBuilder("");
    Node children[] = global.children();
    for (Node part : children) {
      if (part != children[0]) {
        printStr.append(".");
      }
      printStr.append(part.getToken());
    }
    return new PrintVarAction(printStr.toString());
  }

  @Override
  public String getActionNodeName() {
    return "PrintVar";
  }

}
