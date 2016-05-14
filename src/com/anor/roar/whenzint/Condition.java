package com.anor.roar.whenzint;

public abstract class Condition {

	// default behavior
	private boolean repeats = true;
	
	public abstract boolean check(Program program);

	public abstract Action getAction();

	public abstract void setAction(Action action);

	public final boolean repeats() {
		return repeats;
	}
	
	public final void once() {
		repeats = false;
	}

}
