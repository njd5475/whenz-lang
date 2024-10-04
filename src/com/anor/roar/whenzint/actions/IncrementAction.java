package com.anor.roar.whenzint.actions;

import java.io.IOException;
import java.util.Map;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.VariablePath;
import com.anor.roar.whenzint.parser.*;

public class IncrementAction extends Action {

  private VariablePath path;

  static {
    ProgramBuilder.registerActionBuilder(new IncrementAction(null));
  }
  
  public IncrementAction(VariablePath path) {
    this.path = path;
  }

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens)
      throws WhenzSyntaxError, IOException {
    parser.consumeWhitespace(tokens);
    Node node = new Node("Increment");
    if (tokens.peek().is("increment")) {
      tokens.take();
      parser.consumeWhitespace(tokens);
      parser.globalReference(node, tokens);
    } else {
      parser.unexpectedToken(tokens);
    }
    return node;
  }

  @Override
  public void perform(Program program, Map<String, Object> context) {
    Object v = path.get(context);
    if(v == null) {
      //try the program context
      v = program.getObject(path.getFullyQualifiedName());
    }
    
    if (v != null) {
      if (v instanceof Integer) {
        Integer i = (Integer) v;
        path.set(program, context, i + 1);
      } else if (v instanceof String) {
        Integer i = Integer.parseInt(v.toString());
        path.set(program, context, i + 1);
      }
    }
  }

  @Override
  public Action buildAction(ProgramBuilder builder, Node node) throws WhenzSyntaxTreeError {
    Node children[] = node.children();
    VariablePath path = builder.getPath(children[0]);
    return new IncrementAction(path);
  }

  @Override
  public String getActionNodeName() {
    return "Increment";
  }

}
