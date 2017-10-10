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

public class RunShellCommand extends Action {

  private String varName;
  
  static {
    ProgramBuilder.registerActionBuilder(new RunShellCommand(null));
  }

  public RunShellCommand(String varName) {
    this.varName = varName;
  }

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens) throws WhenzSyntaxError, IOException {
    if (tokens.peek().is("execute")) {
      Node exec = new Node("RunShellCommand");
      tokens.take();
      parser.consumeWhitespace(tokens);
      if (tokens.peek().isWord()) {
        while (!tokens.peek().isNewline()) {
          Node arg = new Node("Arg");
          parser.consumeWhitespace(tokens);
          while (!tokens.peek().isWhitespace() && !tokens.peek().isNewline()) {
            Node argParts = new Node("ArgPart", tokens.take());
            arg.add(argParts);
          }
          exec.add(arg);
        }
        tokens.take();

        return exec;
      }

    }
    parser.unexpectedToken(tokens.peek());
    return null;
  }

  @Override
  public void perform(Program program, Map<String, Object> context) {
    System.out.println("Executing Command '" + varName + "'");
    try {
      Runtime.getRuntime().exec(varName);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public Action buildAction(ProgramBuilder builder, Node node) {
    StringBuilder sb = new StringBuilder("");
    for (Node ch : node.children()) {
      if ("Arg".equals(ch.name())) {
        for (Node arg : ch.children()) {
          sb.append(arg.getToken());
        }
        sb.append(' ');
      }
    }
    return new RunShellCommand(sb.toString().trim());
  }

  @Override
  public String getActionNodeName() {
    return "RunShellCommand";
  }

}
