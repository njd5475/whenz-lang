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
import com.anor.roar.whenzint.parser.Token.TTYPE;

public class CustomAction extends Action {

  private TTYPE[] types;
  private String  nodeName;

  public CustomAction(String nodeName, TTYPE... types) {
    this.types = types;
    this.nodeName = nodeName;
  }

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens)
      throws WhenzSyntaxError, IOException {
    Node n = new Node(nodeName);
    
    int cur = 0;
    while(tokens.peek().getType() == types[cur]) {
      n.addChild("ActionToken", tokens.take());
      
      ++cur;
      
      if(cur == types.length) {
        break;
      }
    }
    
    if(cur == types.length) {
      //all types found
      return n;
    }
    
    parser.unexpectedToken(tokens.peek());
    
    return null;
  }

  @Override
  public Action buildAction(ProgramBuilder builder, Node node) {

    return null;
  }

  @Override
  public String getActionNodeName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void perform(Program program, Map<String, Object> context) {
    // TODO Auto-generated method stub

  }

}
