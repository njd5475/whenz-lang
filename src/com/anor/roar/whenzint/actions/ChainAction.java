package com.anor.roar.whenzint.actions;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;

public class ChainAction extends Action {

	private Action action;
	private Action next;

	public ChainAction(Action action, Action next) {
		this.action = action;
		this.next = next;
	}

	@Override
	public void perform(Program program) {
		action.perform(program);
		next.perform(program);
	}

}
