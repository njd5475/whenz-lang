package com.anor.roar.whenzint;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.anor.roar.whenzint.conditions.BoolCondition;
import com.anor.roar.whenzint.conditions.EventCondition;

public class Program {

  // private Queue<Condition> condQueue = new ConcurrentLinkedQueue<>();
  private Map<String, List<Condition>>     waitingForEvents  = new HashMap<>();
  private Map<String, List<Condition>>     waitingForObjects = new HashMap<>();
  // private Stack<Condition> conditions = new Stack<>();
  private Map<String, Object>              objects           = new HashMap<>();
  private Stack<Action>                    actions           = new Stack<>();
  private Map<Action, Map<String, Object>> actionContexts    = new HashMap<>();
  private Map<Condition, Boolean>          enabled           = new HashMap<>();

  public void run() {
    boolean noConditions = false;
    while (!noConditions) {
      actions.clear();

      Condition c;
      noConditions = true;
      for (Map.Entry<Condition, Boolean> e : enabled.entrySet()) {
        if (e.getValue()) {
          noConditions = false;
          c = e.getKey();
          if (c.check(this)) {
            actions.push(c.getAction());
          }

          if (!c.repeats() || c instanceof EventCondition) {
            //System.out.println("Disabled condition for " + c.getClass());
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
      for (Condition c : list) {
        enabled.put(c, true); // enable conditions that are waiting for an event
      }
    }
  }

  public void setObject(String name, Object object) {
    this.objects.put(name, object);
    triggerListener(name);
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
    return this.objects.get(name);
  }

  public void setListener(String ref, Condition cond) {
    List<Condition> list = waitingForObjects.get(ref);
    if (list == null) {
      list = new LinkedList<Condition>();
      this.waitingForObjects.put(ref, list);
    }
    list.add(cond);
  }

}
