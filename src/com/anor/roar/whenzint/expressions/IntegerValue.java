package com.anor.roar.whenzint.expressions;

public class IntegerValue implements ExpressionValue {

  private int val;

  public IntegerValue(int val) {
    this.val = val;
  }

  @Override
  public ExpressionValue calculate(Operation op, ExpressionValue rval) {
    return op.calculateInteger(val, rval);
  }

  @Override
  public ExpressionValue calculateDouble(Operation op, double rval) {
    return op.calculateDoubles((double)val, rval);
  }

  @Override
  public ExpressionValue calculateInteger(Operation op, int rval) {
    return op.calculateIntegers(val, rval);
  }

  @Override
  public ExpressionValue calculateFloat(Operation op, float rval) {
    return op.calculateFloats((float)val, rval);
  }

  @Override
  public Object get() {
    return val;
  }
  
  public String toString() {
    return Integer.toString(val);
  }

  @Override
  public ExpressionValue calculateDoubleRight(Operation op, double lval) {
    return op.calculateDoubles(lval, (double)val);
  }

  @Override
  public ExpressionValue calculateIntegerRight(Operation op, int lval) {
    return op.calculateIntegers(lval, val);
  }

  @Override
  public ExpressionValue calculateFloatRight(Operation op, float lval) {
    return op.calculateFloats(lval, (float)val);
  }

}