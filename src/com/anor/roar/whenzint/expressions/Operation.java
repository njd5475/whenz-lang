package com.anor.roar.whenzint.expressions;

public abstract class Operation {

  public final ExpressionValue calculate(ExpressionValue lval, ExpressionValue rval) {
    return lval.calculate(this, rval);
  }

  protected final ExpressionValue calculateDouble(double lval, ExpressionValue rval) {
    return rval.calculateDoubleRight(this, lval);
  }

  protected final ExpressionValue calculateInteger(int lval, ExpressionValue rval) {
    return rval.calculateIntegerRight(this, lval);
  }

  protected final ExpressionValue calculateFloat(float lval, ExpressionValue rval) {
    return rval.calculateFloatRight(this, lval);
  }

  protected abstract ExpressionValue calculateDoubles(double lval, double rval);
  
  protected abstract ExpressionValue calculateIntegers(int lval, int rval);

  protected abstract ExpressionValue calculateFloats(float lval, float rval);

}
