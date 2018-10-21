package com.anor.roar.whenzint.conditions;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Condition;
import com.anor.roar.whenzint.Program;

public class ConditionalChain extends Condition {

  public enum Op {
    AND, OR
  };

  private ConditionalChain previous;
  private final Condition  toCheck;
  private Op               prevOp;

  private ConditionalChain(Condition c) {
    this.toCheck = c;
  }

  @Override
  public boolean check(Program program) {
    boolean check = toCheck.check(program);
    if(previous != null) {
      if(prevOp == Op.AND) {
        return previous.check(program) && check;
      }else if(prevOp == Op.OR) {
        return previous.check(program) || check;
      }
    }
    return check;
  }

  @Override
  public Action getAction() {
    Action a = toCheck.getAction();
    if(a == null && previous != null) {
      a = previous.getAction();
    }
    return a;
  }

  @Override
  public void setAction(Action action) {
    toCheck.setAction(action);
  }

  public ConditionalChain next(Condition c, Op cOp) {
    ConditionalChain next = wrap(c);
    next.previous = this;
    next.prevOp = cOp;
    return next;
  }

  public static ConditionalChain wrap(Condition c) {
    if(c instanceof ConditionalChain) {
      return (ConditionalChain) c;
    }
    return new ConditionalChain(c);
  }
}
