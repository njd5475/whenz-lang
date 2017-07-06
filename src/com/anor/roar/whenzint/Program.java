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

  private Queue<Condition>             condQueue         = new ConcurrentLinkedQueue<Condition>();
  private Map<String, List<Condition>> waitingForEvents  = new HashMap<String, List<Condition>>();
  private Map<String, List<Condition>> waitingForObjects = new HashMap<String, List<Condition>>();
  private Stack<Condition>             conditions        = new Stack<Condition>();
  private Map<String, Object>          objects           = new HashMap<String, Object>();

  public void run() {
    while (!conditions.isEmpty() || !condQueue.isEmpty() || !waitingForEvents.isEmpty()) {
      Stack<Action> actions = new Stack<Action>();

      emptyCondQueue();
      Condition c;
      while (!conditions.isEmpty()) {
        c = conditions.pop();
        if (c.check(this)) {
          actions.push(c.getAction());
        }else if(c instanceof BoolCondition) {
          System.out.println("BoolCondition");
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
        a.perform(this, new HashMap<String, Object>());
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
    if (list != null) {
      for (Condition c : list) {
        condQueue.add(c);
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
      for(Condition c : condList) {
        condQueue.add(c);  
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
