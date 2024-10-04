package com.anor.roar.whenzint.actions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.VariablePath;
import com.anor.roar.whenzint.parser.*;

public class NewByteBuffer extends Action {

  static {
    //WhenzParser.getInstance().registerAction(new NewByteBuffer());
    ProgramBuilder.registerActionBuilder(new NewByteBuffer());
  }

  private VariablePath path;
  private VariablePath sizePath;
  private int          staticSize;

  public NewByteBuffer() {

  }

  public NewByteBuffer(VariablePath path, VariablePath size) {
    this.path = path;
    this.sizePath = size;
  }

  public NewByteBuffer(VariablePath path, int size) {
    this.path = path;
    this.staticSize = size;
    if(size <= 0) { 
      throw new IllegalArgumentException("Cannot create a buffer of size less than or zero");
    }
  }

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens)
      throws WhenzSyntaxError, IOException {
    Node byteNode = new Node("NewByteBuffer");
    parser.consumeWhitespace(tokens);
    Node path = parser.globalReference(byteNode, tokens);;

    if (path != null) {
      parser.consumeWhitespace(tokens);
      if (tokens.peek().is("is")) {
        tokens.take();
        parser.consumeWhitespace(tokens);
        try {
          path = parser.globalReference(byteNode, tokens); //reset path to reuse variable
        }catch(WhenzSyntaxError e) {
        }
        
        if (tokens.peek().isNumber()) {
          byteNode.add(new Node("Number", tokens.take()));
        }

        parser.consumeWhitespace(tokens);
        if (tokens.peek().is("bytes")) {
          tokens.take();
        }else{
          parser.unexpectedToken(tokens);
        }
        
        return byteNode;
      } else {
        parser.unexpectedToken(tokens);
      }
    } else {
      parser.unexpectedToken(tokens);
    }
    return null;
  }

  @Override
  public void perform(Program program, Map<String, Object> context) {
    int size = -1;
    if (sizePath != null) {
      Object obj = sizePath.get(context);
      if(obj == null) {
        obj = program.getObject(sizePath.getFullyQualifiedName());
      }
      if (obj instanceof Number) {
        size = ((Number) obj).intValue();
      }
    }

    if (staticSize > 0) {
      size = staticSize;
    }

    if (size > 0) {
      path.set(program, context, ByteBuffer.allocate(size));
    }
  }

  @Override
  public Action buildAction(ProgramBuilder builder, Node node) throws WhenzSyntaxTreeError {
    NewByteBuffer act;
    VariablePath path2 = builder.getPath(node.children()[0]);
    if(node.children()[1].isNamed("Number")) {
      act = new NewByteBuffer(path2, node.children()[1].getRawToken().asNumber());
    }else{
      act = new NewByteBuffer(path2, builder.getPath(node.children()[1]));
    }
    
    return act;
  }

  @Override
  public String getActionNodeName() {
    return "NewByteBuffer";
  }
}
