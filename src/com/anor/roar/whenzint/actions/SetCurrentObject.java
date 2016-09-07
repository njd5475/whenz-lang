package com.anor.roar.whenzint.actions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.patterns.Node;
import com.anor.roar.whenzint.patterns.TokenBuffer;
import com.anor.roar.whenzint.patterns.WhenzParser;
import com.anor.roar.whenzint.patterns.WhenzSyntaxError;

public class SetCurrentObject extends Action {

	private Object value;
	private String set;
	private String name;

	public SetCurrentObject(String set, String name, Object to) {
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
	public void perform(Program program) {
		Object object = program.getObject(name);
		try {
			Class cl = value.getClass();
			Method method = object.getClass().getMethod("set" + set, cl);
			method.invoke(object, value);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens)
      throws WhenzSyntaxError, IOException {
    // TODO Auto-generated method stub
    return null;
  }
}
