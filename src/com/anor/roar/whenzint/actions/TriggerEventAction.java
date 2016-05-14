package com.anor.roar.whenzint.actions;
import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Event;
import com.anor.roar.whenzint.Program;

public class TriggerEventAction extends Action {

	private String eventName;

	public TriggerEventAction(String eventName) {
		this.eventName = eventName;
	}

	@Override
	public void perform(Program program) {
		program.trigger(eventName);
	}

}
