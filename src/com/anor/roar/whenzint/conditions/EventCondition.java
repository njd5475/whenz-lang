package com.anor.roar.whenzint.conditions;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Condition;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.actions.PrintAction;

public class EventCondition extends Condition {

	private String eventName;
	private Action action;

	public EventCondition(String eventName) {
		this.eventName = eventName;
	}
	
	public String getEventName() {
		return eventName;
	}

	@Override
	public boolean check(Program program) {
		return true;
	}

	@Override
	public Action getAction() {
		return action;
	}

	@Override
	public void setAction(Action action) {
		this.action = action;
	}

}
