package com.anor.roar.whenzint.conditions;

import java.util.function.Predicate;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Condition;
import com.anor.roar.whenzint.Program;

public class BoolCondition extends Condition {

  private String ref;
  private Integer number = null;
  private String cmp = null;
  private Predicate<Program> theOp;
  private Action action;

  public BoolCondition(String op, String ref, int num, boolean repeats) {
    this.ref = ref;
    this.number = num;
    if("==".equals(op)) {
      theOp = (p) -> {
        return checkEqualEqual(p);
      };
    }else if("<=".equals(op)) {
      theOp = (p) -> {
        return checkLessEqual(p);
      };
    }else if(">=".equals(op)) {
      theOp = (p) -> {
        return checkGreaterEqual(p);
      };
    }else if("!=".equals(op)) {
      theOp = (p) -> {
        return checkNotEqual(p);
      };
    }else if("<".equals(op)) {
      theOp = (p) -> {
        return checkLess(p);
      };
    }else if(">".equals(op)) {
      theOp = (p) -> {
        return checkGreater(p);
      };
    }
    this.repeats = repeats;
  }
  
  public BoolCondition(String op, String ref, String cmp, boolean repeats) {
    this.ref = ref;
    this.cmp = cmp;
    if("==".equals(op)) {
      theOp = (p) -> {
        return checkEqualEqual(p);
      };
    }else if("<=".equals(op)) {
      theOp = (p) -> {
        return checkLessEqual(p);
      };
    }else if(">=".equals(op)) {
      theOp = (p) -> {
        return checkGreaterEqual(p);
      };
    }else if("!=".equals(op)) {
      theOp = (p) -> {
        return checkNotEqual(p);
      };
    }else if("<".equals(op)) {
      theOp = (p) -> {
        return checkLess(p);
      };
    }else if(">".equals(op)) {
      theOp = (p) -> {
        return checkGreater(p);
      };
    }
    this.repeats = repeats;
  }

  @Override
  public boolean check(Program program) {
    return theOp.test(program);
  }
  
  public boolean checkLess(Program program) {
    Object refObj = program.getObject(ref);
    if(refObj != null && number != null && refObj instanceof Number) {
      Number n = (Number)refObj;
      return n.intValue() < number;
    }
    return false;
  }
  
  public boolean checkGreater(Program program) {
    Object refObj = program.getObject(ref);
    if(refObj != null && number != null && refObj instanceof Number) {
      Number n = (Number)refObj;
      return n.intValue() > number;
    }
    return false;
  }
  
  public boolean checkGreaterEqual(Program program) {
    Object refObj = program.getObject(ref);
    if(refObj != null && number != null) {
      if(refObj instanceof Number) {
        Number n = (Number)refObj;
        return n.intValue() >= number;
      }
    }
    return false;
  }
  
  public boolean checkLessEqual(Program program) {
    Object refObj = program.getObject(ref);
    if(refObj != null && number != null) {
      if(refObj instanceof Number) {
        Number n = (Number)refObj;
        return n.intValue() <= number;
      }
    }
    return false;
  }
  
  public boolean checkEqualEqual(Program program) {
    Object refObj = program.getObject(ref);
    if(refObj != null && cmp != null) {
      return refObj.equals(cmp);
    }else if(refObj != null) {
      return refObj.equals(number);
    }else{
      //System.err.format("RefObj %s is null\n", ref);
    }
    return false;
  }
  
  private boolean checkNotEqual(Program program) {
    return !checkEqualEqual(program);
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
