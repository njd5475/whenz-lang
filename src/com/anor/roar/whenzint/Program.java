package com.anor.roar.whenzint;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.anor.roar.whenzint.actions.StateChangeListener;
import com.anor.roar.whenzint.conditions.EventCondition;
import com.anor.roar.whenzint.mapping.ByteBufferMapping;

public class Program {

	// private Queue<Condition> condQueue = new ConcurrentLinkedQueue<>();
	private final Map<String, List<Condition>> waitingForEvents = new HashMap<>();
	private final Map<String, List<Condition>> waitingForObjects = new HashMap<>();
	// private Stack<Condition> conditions = new Stack<>();
	private final Map<String, Object> objects = new HashMap<>();
	private final Stack<Action> actions = new Stack<>();
	private final Map<Action, Map<String, Object>> actionContexts = new HashMap<>();
	private final Map<Condition, Boolean> enabled = new HashMap<>();
	private final Map<String, ByteBufferMapping> mappings = new HashMap<>();
	private final Map<String, String> states = new HashMap<>();
	private final Map<String, Program> loadedModules = new HashMap<>();

	private final Map<String, Map<String, Set<StateChangeListener>>> stateListeners = new HashMap<>();
	private final AtomicBoolean exit = new AtomicBoolean(false);

	private final Queue<Event> eventQueue = new ConcurrentLinkedQueue<>();

	public void run() {
		boolean noConditions = false;
		while (!noConditions && !exit.get()) {
			actions.clear();

			Condition c;
			noConditions = true;
			for (Map.Entry<Condition, Boolean> e : enabled.entrySet()) {
				if (e.getValue()) {
					noConditions = false;
					c = e.getKey();
					if (c.check(this)) {
						Action a = c.getAction();
						if (a != null) {
							actions.push(a);
						}

						if (!c.repeats() || c instanceof EventCondition) {
							// System.out.println("Disabled condition for " + c.getClass());
							enabled.put(c, false); // disable condition
						}
					}
				}
			}

			Action a;
			while (!actions.isEmpty()) {
				a = actions.pop();
				Map<String, Object> context = actionContexts.get(a);
				if (context == null) {
					actionContexts.put(a, context = new HashMap<String, Object>());
				} else {
					context.clear();
				}
				a.perform(this, context);
			}

			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
			enabled.put(c, false);
		} else {
			enabled.put(c, true);
		}
	}

	public void trigger(String eventName) {
		List<Condition> list = waitingForEvents.get(eventName);
		if (list != null) {
			for (Condition c : list) {
				enabled.put(c, true); // enable conditions that are waiting for an event
			}
		}
	}

	public void setObject(String name, Object object) {
		if (this.mappings.containsKey(name)) {
			ByteBufferMapping map = this.mappings.get(name);
			map.apply(this, this.objects, object);
			this.changeState(name, "changed");
		} else {
			if (this.objects.containsKey(name)) {
				this.changeState(name, "changed");
			} else {
				this.changeState(name, "set");
			}
			this.objects.put(name, object);
			triggerListener(name);

		}
	}

	private void triggerListener(String name) {
		List<Condition> condList = waitingForObjects.get(name);
		if (condList != null) {
			for (Condition c : condList) {
				enabled.put(c, true); // enable the check
			}
		}
	}

	public Object getObject(String name) {
		Object o = this.objects.get(name);
		if (o == null) {
			o = this.mappings.get(name);
		}
		return o;
	}

	public void setListener(String ref, Condition cond) {
		List<Condition> list = waitingForObjects.get(ref);
		if (list == null) {
			list = new LinkedList<Condition>();
			this.waitingForObjects.put(ref, list);
		}
		list.add(cond);
	}

	public boolean registerModule(String name, Program module) {
		if(loadedModules.containsKey(name)) {
			return false;
		}
		loadedModules.put(name, module);
		return true;
	}

	public void registerStateListener(String object, String stateName, StateChangeListener l) {
		Map<String, Set<StateChangeListener>> stateListenersMap = this.stateListeners.get(object);
		if (stateListenersMap == null) {
			stateListenersMap = new HashMap<>();
			this.stateListeners.put(object, stateListenersMap);
		}

		Set<StateChangeListener> listeners = stateListenersMap.get(stateName);
		if (listeners == null) {
			listeners = new HashSet<StateChangeListener>();
			stateListenersMap.put(stateName, listeners);
		}

		listeners.add(l);
	}

	public void addMapping(ByteBufferMapping map, VariablePath link) {
		this.mappings.put(link.getFullyQualifiedName(), map);
	}

	public void changeState(String o, String newState) {
		if (o != null) {
			String oldState = this.states.get(o);
			this.states.put(o, newState);
			Map<String, Set<StateChangeListener>> listeners = this.stateListeners.get(o);
			if (listeners != null) {
				Set<StateChangeListener> set = listeners.get(newState);
				if (set != null) {
					for (StateChangeListener l : set) {
						l.changed(newState, oldState);
					}
				}
			}
		}
	}
	
	public Map<String, Object> getObjects() {
		return this.objects;
	}

	public boolean hasObject(String name) {
		if (!this.objects.containsKey(name)) {
			return this.mappings.containsKey(name);
		}
		return true;
	}

	public void loadJavaProperties() {
		Properties props = System.getProperties();
		for (Object propName : props.keySet()) {
			this.setObject("env." + propName.toString(), props.get(propName));
		}
		this.setObject("platform", "Java");
	}

	public void exit(int i) {
		this.exit.set(true);
	}
}
