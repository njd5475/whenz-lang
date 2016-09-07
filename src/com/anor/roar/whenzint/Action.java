package com.anor.roar.whenzint;

import com.anor.roar.whenzint.patterns.TokenAction;

public abstract class Action implements TokenAction {

	public abstract void perform(Program program);

}