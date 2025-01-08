package com.anor.roar.whenzint.expressions.values;

import com.anor.roar.whenzint.expressions.operations.Operation;

public class DoubleValue implements ExpressionValue {

  private double val;

  public DoubleValue(double val) {
    this.val = val;
  }

  @Override
  public Object get() {
    return val;
  }
  
  public String toString() {
    return Double.toString(val);
  }

  public boolean equals(Object o) {
    if(o instanceof Double) {
      return ((Double)o) == val;
    }
    if(o instanceof DoubleValue) {
      return ((DoubleValue)o).val == val;
    }
    return false;
  }

  @Override
  public ExpressionValue calculate(Operation op, ExpressionValue rval) {
    return op.calculateDouble(val, rval);
  }

  @Override
  public ExpressionValue calculateDouble(Operation op, double rval) {
    return op.calculateDoubles(val, rval);
  }

  @Override
  public ExpressionValue calculateInteger(Operation op, int rval) {
    return op.calculateDoubles(val, rval);
  }

  @Override
  public ExpressionValue calculateFloat(Operation op, float rval) {
    return op.calculateDoubles(val, rval);
  }

  @Override
  public ExpressionValue calculateDoubleRight(Operation op, double lval) {
    return op.calculateDoubles(lval, val);
  }

  @Override
  public ExpressionValue calculateIntegerRight(Operation op, int lval) {
    return op.calculateDoubles(lval, val);
  }

  @Override
  public ExpressionValue calculateFloatRight(Operation op, float lval) {
    return op.calculateDoubles(lval, val);
  }

  @Override
  public ExpressionValue calculateByteArrayValue(Operation op, byte[] lval) {
    // TODO Auto-generated method stub
    return null;
  }
}
