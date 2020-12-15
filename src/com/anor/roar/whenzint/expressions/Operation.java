package com.anor.roar.whenzint.expressions;

import java.math.BigDecimal;
import java.math.BigInteger;

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
  
  protected final ExpressionValue calculateByteArrayValue(byte[] lval, ExpressionValue rval) {
    return rval.calculateByteArrayValue(this, lval);
  }

  protected abstract ExpressionValue calculateDoubles(double lval, double rval);
  
  protected abstract ExpressionValue calculateIntegers(int lval, int rval);

  protected abstract ExpressionValue calculateFloats(float lval, float rval);
  
  protected abstract ExpressionValue calculateByteArrayValue(byte[] lval, byte[] rval);
  
  protected abstract ExpressionValue calculateBigInteger(BigInteger lval, BigInteger rval);
  
  protected abstract ExpressionValue calculateBigDecimal(BigDecimal lval, BigDecimal rval);

}
