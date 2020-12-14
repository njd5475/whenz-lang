package com.anor.roar.whenzint.actions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.VariablePath;
import com.anor.roar.whenzint.json.Json;
import com.anor.roar.whenzint.parser.Node;
import com.anor.roar.whenzint.parser.ProgramBuilder;
import com.anor.roar.whenzint.parser.TokenBuffer;
import com.anor.roar.whenzint.parser.WhenzParser;
import com.anor.roar.whenzint.parser.WhenzSyntaxError;

public class ToJsonAction extends Action {

  static {
    ProgramBuilder.registerActionBuilder(new ToJsonAction());
  }
  
  private VariablePath map;
  private VariablePath jsonObject;

  public ToJsonAction() {

  }

  public ToJsonAction(VariablePath map, VariablePath jsonObject) {
    this.map = map;
    this.jsonObject = jsonObject;
  }

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens) throws WhenzSyntaxError, IOException {
    parser.consumeWhitespace(tokens);
    Node toJsonNode = new Node(this.getActionNodeName());
    Node path = parser.globalReference(toJsonNode, tokens);

    if (path != null) {
      parser.consumeWhitespace(tokens);
      if (tokens.peek().is("as")) {
        tokens.take();
        parser.consumeWhitespace(tokens);
        if (tokens.peek().is("json")) {
          tokens.take();
          parser.consumeWhitespace(tokens);
          if (tokens.peek().is("from")) {
            tokens.take();
            parser.consumeWhitespace(tokens);
            parser.globalReference(toJsonNode, tokens);
            return toJsonNode;
          } else {
            parser.unexpectedToken(tokens);
          }
        } else {
          parser.unexpectedToken(tokens);
        }
      } else {
        parser.unexpectedToken(tokens);
      }
    } else {
      parser.unexpectedToken(tokens);
    }

    return null;
  }

  @Override
  public Action buildAction(ProgramBuilder builder, Node node) {
    VariablePath to = builder.getPath(node.children()[0]);
    VariablePath from = builder.getPath(node.children()[1]);
    if(to != null && from != null) {
      Action a = new ToJsonAction(from, to);
      return a;
    }
    return null;
  }

  @Override
  public String getActionNodeName() {
    return "ToJson";
  }

  @Override
  public void perform(Program program, Map<String, Object> context) {
    Object o = program.getObject(map.getFullyQualifiedName());
    String str = null;
    if (o instanceof ByteBuffer) {
      ByteBuffer bb = (ByteBuffer) o;
      str = new String(bb.array());
    } else if (o instanceof String) {
      str = (String) o;
    } else if (o instanceof Number) {
      str = ((Number) o).toString();
    }

    Map<String, Object> parse = Json.parse(str, (String[] parentKeys, String path, Object value) -> {
      String fullKey = String.format("%s.%s.%s", jsonObject.getFullyQualifiedName(), String.join(".", parentKeys), path);
      program.setObject(fullKey, value);
    });
    program.setObject(jsonObject.getFullyQualifiedName(), parse);
  }

}
