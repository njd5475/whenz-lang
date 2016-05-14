package com.anor.roar.whenzint.actions;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;

public class PrintAction extends Action {

	private String toPrint;

	public PrintAction(String toPrint) {
		this.toPrint = toPrint;
	}

	@Override
	public void perform(Program program) {
		System.out.println(toPrint);
	}

}
