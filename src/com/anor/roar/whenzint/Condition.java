package com.anor.roar.whenzint;

public abstract class Condition {

  protected boolean repeats = true;
  private Action    action;
  private Condition next = null;

  public Condition pushDown(Condition c) {
    if (!c.hasNext()) {
      c.setNext(this);
    }
    return c;
  }

  public abstract boolean check(Program program);
  
  public Condition getNext() {
    return next;
  }

  public boolean hasNext() {
    return next != null;
  }

  private void setNext(Condition c) {
    this.next = c;
  }
  
  public Action getAction() {
    return action != null ? action : this.next.getAction();
  }

  // TODO: this needs refactoring for more controlled method
  public void setAction(Action action) {
    this.action = action;
  }

  public final boolean repeats() {
    return repeats;
  }

  public final void once() {
    repeats = false;
  }

}
