package com.anor.roar.whenzint.actions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.VariablePath;
import com.anor.roar.whenzint.mapping.ByteBufferMapping;
import com.anor.roar.whenzint.parser.Node;
import com.anor.roar.whenzint.parser.ProgramBuilder;
import com.anor.roar.whenzint.parser.TokenBuffer;
import com.anor.roar.whenzint.parser.WhenzParser;
import com.anor.roar.whenzint.parser.WhenzSyntaxError;

public class ByteBufferMappingAction extends Action {

  static {
    // WhenzParser.getInstance().registerAction(new NewByteBuffer());
    ProgramBuilder.registerActionBuilder(new ByteBufferMappingAction());
  }

  private ByteBufferMapping map;
  private VariablePath      link;

  public ByteBufferMappingAction() {

  }

  public ByteBufferMappingAction(VariablePath link, ByteBufferMapping map) {
    this.map = map;
    this.link = link;
  }

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens) throws WhenzSyntaxError, IOException {
    parser.consumeWhitespace(tokens);
    Node mappingNode = new Node(this.getActionNodeName());
    Node path = parser.globalReference(mappingNode, tokens);

    if (path != null) {
      parser.consumeWhitespace(tokens);
      if (tokens.peek().is("is")) {
        tokens.take();
        parser.consumeWhitespace(tokens);
        try {
          path = parser.globalReference(mappingNode, tokens); // reset path to reuse variable
        } catch (WhenzSyntaxError e) {
        }

        if (tokens.peek().isNumber()) {
          mappingNode.add(new Node("Number", tokens.take()));
        }

        parser.consumeWhitespace(tokens);
        if (tokens.peek().is("bytes")) {
          tokens.take();
        } else {
          parser.unexpectedToken(tokens.take());
        }

        parser.consumeWhitespace(tokens);
        if (tokens.peek().is("from")) {
          tokens.take();
          parser.consumeWhitespace(tokens);
          mappingNode.add(parser.identifier(tokens));
        } else {
          parser.unexpectedToken(tokens.peek());
        }
        parser.consumeWhitespace(tokens);

        return mappingNode;
      } else {
        parser.unexpectedToken(tokens.peek());
      }
    } else {
      parser.unexpectedToken(tokens.peek());
    }
    return null;
  }

  @Override
  public Action buildAction(ProgramBuilder builder, Node node) {
    // throw new IllegalArgumentException("Not implemented yet");
    VariablePath path = builder.getPath(node.children()[0]);
    VariablePath parent = path.getParent();
    int numberOfBytes = Integer.parseInt(node.children()[1].getTokenOrValue());
    Node locationNode = node.children()[2];
    int location = -1;
    if(locationNode.is("head")) {
      location = 0;
    } else if(locationNode.isNamed("Identifier")) {
      VariablePath derive = parent.derive(locationNode.getTokenOrValue());
      ByteBufferMapping mapping = builder.getMapping(derive.getFullyQualifiedName());
      location = mapping.getLocation() + mapping.getNumberOfBytes();
    }
    ByteBufferMapping bmm = new ByteBufferMapping(numberOfBytes, parent, location);
    builder.registerMapping(bmm, path);
    ByteBufferMappingAction bbmA = new ByteBufferMappingAction(path, bmm);
    return bbmA;
  }

  @Override
  public String getActionNodeName() {
    return "ByteBufferMapping";
  }

  @Override
  public void perform(Program program, Map<String, Object> context) {
    program.addMapping(this.map, this.link);
  }
}
