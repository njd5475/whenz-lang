package com.anor.roar.whenzint.expressions;

public enum MathOps {
  PLUS(2), MINUS(2), MULT(1), DIV(1), GROUP_BEGIN(4), GROUP_END(3), NUMBER(99), VARIABLE(99);

  private int priority;
  
  private MathOps(int priority) {
    this.priority = priority;
  }
  
  public boolean isHigher(MathOps op) {
    return priority > op.priority;
  }

  public MathOpData with(Object object) {
    return new MathOpData(this, object);
  }

}