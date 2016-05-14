package com.anor.roar.whenzint;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.anor.roar.whenzint.conditions.EventCondition;

public class Program {

	private Queue<Condition>							condQueue					= new ConcurrentLinkedQueue<Condition>();
	private Map<String, List<Condition>>	waitingForEvents	= new HashMap<String, List<Condition>>();
	private Stack<Condition>							conditions				= new Stack<Condition>();
	private Object												object;

	public void run() {
		while (!conditions.isEmpty() || !condQueue.isEmpty()
				|| !waitingForEvents.isEmpty()) {
			Stack<Action> actions = new Stack<Action>();

			emptyCondQueue();
			Condition c;
			while (!conditions.isEmpty()) {
				c = conditions.pop();
				if (c.check(this)) {
					actions.push(c.getAction());
				}
				if (!c.repeats() && c instanceof EventCondition) {
					EventCondition ec = (EventCondition) c;
					List<Condition> list = waitingForEvents.get(ec);
					if (list != null) {
						list.remove(c);
						if (list.isEmpty()) {
							waitingForEvents.remove(ec.getEventName());
						}
					}
				}
			}

			Action a;
			while (!actions.isEmpty()) {
				a = actions.pop();
				a.perform(this);
			}
		}
	}

	private void emptyCondQueue() {
		while (!condQueue.isEmpty()) {
			Condition c = condQueue.poll();
			conditions.add(c);
		}
	}

	public void add(Condition c) {
		if (c instanceof EventCondition) {
			EventCondition ec = (EventCondition) c;
			List<Condition> conds = waitingForEvents.get(ec.getEventName());
			if (conds == null) {
				conds = new LinkedList<Condition>();
				waitingForEvents.put(ec.getEventName(), conds);
			}
			conds.add(c);
		} else {
			conditions.add(c);
		}
	}

	public void trigger(String eventName) {
		List<Condition> list = waitingForEvents.get(eventName);
		for (Condition c : list) {
			condQueue.add(c);
		}
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public Object getObject() {
		return object;
	}

}
