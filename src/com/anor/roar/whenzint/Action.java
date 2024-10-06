package com.anor.roar.whenzint;

import java.io.File;
import java.util.Map;

import com.anor.roar.whenzint.parser.ActionBuilder;
import com.anor.roar.whenzint.parser.TokenAction;

public abstract class Action implements TokenAction, ActionBuilder {

	public abstract void perform(Program program, Map<String, Object> context);

    public abstract int getLine();

    public abstract int getColumn();

    public abstract File getFile();
}