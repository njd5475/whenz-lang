package com.anor.roar.whenzint.conditions;

import com.anor.roar.whenzint.Condition;
import com.anor.roar.whenzint.Program;

public class AndConditionGroup extends Condition {

  private Condition[] conditions;

  public AndConditionGroup(Condition...conditions) {
    this.conditions = conditions;
  }

  @Override
  public boolean check(Program program) {
    for(Condition c : conditions) {
      if(!c.check(program)) {
        return false;
      }
    }
    return true;
  }
}
