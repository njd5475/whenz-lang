package com.anor.roar.whenzint.conditions;

import com.anor.roar.whenzint.Condition;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.actions.StateChangeListener;

public class StateCondition extends Condition implements StateChangeListener {

  private String  stateName;
  private boolean isTrue;
  private boolean hasRegisteredListener;
  private String  objectName;
  private boolean reset;

  public StateCondition(String objectName, String stateName) {
    this.stateName = stateName;
    this.objectName = objectName;
  }

  @Override
  public boolean check(Program program) {
    if (!this.hasRegisteredListener) {
      program.registerStateListener(objectName, stateName, this);
      this.hasRegisteredListener = true;
    }
    if (isTrue && reset) {
      isTrue = false; // reset
      reset = false;
      return true;
    }
    return isTrue;
  }

  @Override
  public void changed(String newState, String oldState) {
    isTrue = true; // we registered for the specific state change so we know what is going on
    reset = true;
  }

}
