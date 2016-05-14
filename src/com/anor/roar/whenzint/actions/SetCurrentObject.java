package com.anor.roar.whenzint.actions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;

public class SetCurrentObject extends Action {

	private Object value;
	private String set;

	public SetCurrentObject(String set, Object to) {
		if(to == null) {
			throw new NullPointerException("Cannot call set" + set + " on null object");
		}
		this.value = to;
		this.set = set;
	}

	@Override
	public void perform(Program program) {
		Object object = program.getObject();
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
}
