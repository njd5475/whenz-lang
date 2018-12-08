package com.anor.roar.whenzint.expressions;

public class MathOpData {

  private MathOps op;
  private Object  data;

  public MathOpData(MathOps op, Object data) {
    this.op = op;
    this.data = data;
  }
  
  public MathOps getOp() {
    return op;
  }
  
  public Object getValue() {
    return data;
  }
  
  public String toString() {
    return String.format("%s[%s]", op.toString(), String.valueOf(data));
  }
}
