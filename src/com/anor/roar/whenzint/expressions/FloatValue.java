package com.anor.roar.whenzint.expressions;

public class FloatValue implements ExpressionValue {

  private float val;

  public FloatValue(float val) {
    this.val = val;
  }

  @Override
  public ExpressionValue calculate(Operation op, ExpressionValue rval) {
    return op.calculateFloat(val, rval);
  }

  @Override
  public ExpressionValue calculateDouble(Operation op, double rval) {
    return op.calculateDoubles(val, rval);
  }

  @Override
  public ExpressionValue calculateInteger(Operation op, int rval) {
    return op.calculateFloats(val, rval);
  }

  @Override
  public ExpressionValue calculateFloat(Operation op, float rval) {
    return op.calculateFloats(val, rval);
  }

  @Override
  public Object get() {
    return val;
  }
  
  public String toString() {
    return Float.toString(val);
  }

  @Override
  public ExpressionValue calculateDoubleRight(Operation op, double lval) {
    return op.calculateDoubles(lval, val);
  }

  @Override
  public ExpressionValue calculateIntegerRight(Operation op, int lval) {
    return op.calculateFloats(lval, val);
  }

  @Override
  public ExpressionValue calculateFloatRight(Operation op, float lval) {
    return op.calculateFloats(lval, val);
  }
}
