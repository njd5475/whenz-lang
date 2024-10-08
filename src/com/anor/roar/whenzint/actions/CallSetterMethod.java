package com.anor.roar.whenzint.actions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.parser.*;
import com.anor.roar.whenzint.parser.Token.TTYPE;

public class CallSetterMethod extends AbstractAction {

  private Object value;
  private String set;
  private String name;

  static {
    ProgramBuilder
        .registerActionBuilder(new CallSetterMethod());
  }
  
  public CallSetterMethod() {
      super(CodeLocation.fake);
  }

  public CallSetterMethod(CodeLocation location, String set, String name, Object to) {
    super(location);
    if (to == null) {
      throw new NullPointerException(
          "Cannot call set" + set + " on " + name + " to a null object");
    }
    if (name == null) {
      throw new NullPointerException("Cannot set " + set + " on a null object");
    }
    this.name = name;
    this.value = to;
    this.set = set;
  }

  @Override
  public void perform(Program program, Map<String, Object> context) {
    Object object = program.getObject(name);
    try {
      Class cl = value.getClass();
      Method method = object.getClass().getMethod("set" + set, cl);
      method.invoke(object, value);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }

  }

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens)
      throws WhenzSyntaxError, IOException {
    Node setAction = new Node("Set");
    if (tokens.peek().is("set")) {
      tokens.take();
      parser.consumeWhitespace(tokens);
      if (tokens.peek().isWord()) {
        Node variableIdent = new Node("VariableIdentifier", tokens.take());

        // one or more tokens till the end of the line
        parser.consumeWhitespace(tokens);
        while (!tokens.peek().isNewline()) {
          if (tokens.peek().isSymbol() && tokens.peek().is("&")) {
            tokens.take(); // is the ampersand
            if (tokens.peek().isWord()) {
              Node obj = new Node("object", tokens.take());
              variableIdent = new Node("Reference",
                  variableIdent.getRawToken());
              variableIdent.add(obj);
            } else {
              parser.unexpectedToken(tokens);
            }
          } else {
            Node value = new Node("value", tokens.take());
            variableIdent.add(value);
          }
        }
        tokens.take(); // should be the newline

        setAction.add(variableIdent);
      }
    } else {
      parser.unexpectedToken(tokens);
    }
    return setAction;
  }

  @Override
  public Action buildAction(ProgramBuilder builder, Node node) {
    Node setNode = node.children()[0];
    String set = setNode.getToken();
    String name = setNode.getToken();
    Object obj = "";
    if ("Reference".equals(setNode.name())) {
      Node val = setNode.children()[0];
      obj = builder.getObject(val.getTokenOrValue());
      if (obj == null) {
        System.err.println("Link Error: Reference " + val.getTokenOrValue()
            + " referes to a null object");
      }
      name = "window";
    } else if ("VariableIdentifier".equals(setNode.name())) {
      StringBuilder str = new StringBuilder("");
      for (Node v : setNode.children()) {
        str.append(v.getTokenOrValue());
      }
      obj = str.toString();
      if (obj == null) {
        System.err.println("Issues linking program");
      }
      name = "window";
      builder.setObject(name, obj);
    }

    return new CallSetterMethod(CodeLocation.toLocation(node), set, name, obj);
  }

  @Override
  public String getActionNodeName() {
    return "Set";
  }
}
