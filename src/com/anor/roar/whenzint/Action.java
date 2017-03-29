package com.anor.roar.whenzint;

import java.util.Map;

import com.anor.roar.whenzint.parser.TokenAction;

public abstract class Action implements TokenAction {

	public abstract void perform(Program program, Map<Object, Object> context);

}