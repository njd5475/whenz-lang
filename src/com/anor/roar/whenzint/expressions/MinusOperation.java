package com.anor.roar.whenzint.expressions;

public class MinusOperation extends Operation {

  @Override
  public ExpressionValue calculateDoubles(double val, double rval) {
    return new DoubleValue(val - rval);
  }


  @Override
  public ExpressionValue calculateIntegers(int lval, int rval) {
    return new IntegerValue(lval - rval);
  }


  @Override
  public ExpressionValue calculateFloats(float val, float rval) {
    return new FloatValue(val - rval);
  }

}
