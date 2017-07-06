package com.anor.roar.whenzint.actions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.parser.Node;
import com.anor.roar.whenzint.parser.Token;
import com.anor.roar.whenzint.parser.TokenBuffer;
import com.anor.roar.whenzint.parser.WhenzParser;
import com.anor.roar.whenzint.parser.WhenzSyntaxError;
import com.anor.roar.whenzint.parser.Token.TTYPE;

public class CallSetterMethod extends Action {

	private Object value;
	private String set;
	private String name;

	public CallSetterMethod(String set, String name, Object to) {
		if(to == null) {
			throw new NullPointerException("Cannot call set" + set + " on " + name + " to a null object");
		}
		if(name == null) {
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
    if(tokens.peek().is("set")) {
      tokens.take();
      parser.consumeWhitespace(tokens);
      if(tokens.peek().isIdentifier()) {
        Node variableIdent = new Node("VariableIdentifier", tokens.take());
        
        // one or more tokens till the end of the line
        parser.consumeWhitespace(tokens);
        while(!tokens.peek().isNewline()) {
          if(tokens.peek().isSymbol() && tokens.peek().is("&")) {
            tokens.take(); // is the ampersand
            if(tokens.peek().isIdentifier()) {
              Node obj = new Node("object", tokens.take());
              variableIdent = new Node("Reference", variableIdent.getRawToken());
              variableIdent.add(obj);
            }else{
              parser.unexpectedToken(tokens.peek());
            }
          }else{
            Node value = new Node("value", tokens.take());
            variableIdent.add(value);
          }
        }
        tokens.take(); // should be the newline
        
        setAction.add(variableIdent);
      }
    }else{
      parser.unexpectedToken(tokens.peek());
    }
    return setAction;
  }
}
