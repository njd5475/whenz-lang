package com.anor.roar.whenzint.conditions;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Condition;
import com.anor.roar.whenzint.Program;

public class BoolCondition extends Condition {

  private String op;
  private String ref;
  private int number;
  private Action action;

  public BoolCondition(String op, String ref, int num) {
    this.op = op;
    this.ref = ref;
    this.number = num;
  }

  @Override
  public boolean check(Program program) {
    //TODO: this needs optimization just putting this here to get it working
    if("==".equals(op)) {
      Object refObj = program.getObject(ref);
      if(refObj != null) {
        return refObj.equals(number);
      }else{
        System.err.format("RefObj %s is null\n", ref);
      }
    }
    return false;
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
