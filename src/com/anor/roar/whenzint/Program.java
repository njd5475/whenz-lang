package com.anor.roar.whenzint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.anor.roar.whenzint.actions.StateChangeListener;
import com.anor.roar.whenzint.conditions.EventCondition;
import com.anor.roar.whenzint.mapping.ByteBufferMapping;

public class Program {

  // private Queue<Condition> condQueue = new ConcurrentLinkedQueue<>();
  private Map<String, List<Condition>>     waitingForEvents  = new HashMap<>();
  private Map<String, List<Condition>>     waitingForObjects = new HashMap<>();
  // private Stack<Condition> conditions = new Stack<>();
  private Map<String, Object>              objects           = new HashMap<>();
  private Stack<Action>                    actions           = new Stack<>();
  private Map<Action, Map<String, Object>> actionContexts    = new HashMap<>();
  private Map<Condition, Boolean>          enabled           = new HashMap<>();
  private Map<String, ByteBufferMapping>   mappings          = new HashMap<>();
  private Map<String, String>              states            = new HashMap<>();

  private Map<String, Map<String, Set<StateChangeListener> > > stateListeners = new HashMap<>();

  public void run() {
    boolean noConditions = false;
    while (!noConditions) {
      actions.clear();

      Condition c;
      noConditions = true;
      for(Map.Entry<Condition, Boolean> e : enabled.entrySet()) {
        if (e.getValue()) {
          noConditions = false;
          c = e.getKey();
          if (c.check(this)) {
            actions.push(c.getAction());
          }

          if (!c.repeats() || c instanceof EventCondition) {
            // System.out.println("Disabled condition for " + c.getClass());
            enabled.put(c, false); // disable condition
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
      for(Condition c : list) {
        enabled.put(c, true); // enable conditions that are waiting for an event
      }
    }
  }

  public void setObject(String name, Object object) {
    if (this.mappings.containsKey(name)) {
      ByteBufferMapping map = this.mappings.get(name);
      map.apply(this, this.objects, object);
    } else {
      if(this.objects.containsKey(name)) {
        this.changeState(name, "changed");  
      }else {
        this.changeState(name, "set");
      }
      this.objects.put(name, object);
      triggerListener(name);
      
     }
  }

  private void triggerListener(String name) {
    List<Condition> condList = waitingForObjects.get(name);
    if (condList != null) {
      for(Condition c : condList) {
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
  
  public void registerStateListener(String object, String stateName, StateChangeListener l) {
    Map<String, Set<StateChangeListener>> stateListenersMap = this.stateListeners.get(object);
    if(stateListenersMap == null) {
      stateListenersMap = new HashMap<>();
      this.stateListeners.put(object, stateListenersMap);
    }
    
    Set<StateChangeListener> listeners = stateListenersMap.get(stateName);
    if(listeners == null) {
      listeners = new HashSet<StateChangeListener>();
      stateListenersMap.put(stateName, listeners);
    }
    
    listeners.add(l);
  }

  public void addMapping(ByteBufferMapping map, VariablePath link) {
    this.mappings.put(link.getFullyQualifiedName(), map);
  }

  public void changeState(String o, String newState) {
    if(o != null) {
      String oldState = this.states.get(o);
      this.states.put(o, newState);
      Map<String, Set<StateChangeListener>> listeners = this.stateListeners.get(o);
      if(listeners != null) {
        Set<StateChangeListener> set = listeners.get(newState);
        for(StateChangeListener l : set) {
          l.changed(newState, oldState);
        }
      }
    }
  }

  
}
