package com.anor.roar.whenzint.expressions;

public class DivideOperation extends Operation {

  @Override
  protected ExpressionValue calculateDoubles(double lval, double rval) {
    return new DoubleValue(lval / rval);
  }

  @Override
  protected ExpressionValue calculateIntegers(int lval, int rval) {
    return new IntegerValue(lval / rval);
  }

  @Override
  protected ExpressionValue calculateFloats(float lval, float rval) {
    return new FloatValue(lval / rval);
  }

}
