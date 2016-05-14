package com.anor.roar.whenzint.actions;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;

public class ExitAction extends Action {

	@Override
	public void perform(Program program) {
		System.exit(0);
	}

}
