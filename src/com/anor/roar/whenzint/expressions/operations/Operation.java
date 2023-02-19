package com.anor.roar.whenzint.expressions.operations;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.anor.roar.whenzint.expressions.values.ExpressionValue;

public abstract class Operation {

  public final ExpressionValue calculate(ExpressionValue lval, ExpressionValue rval) {
    return lval.calculate(this, rval);
  }

  public final ExpressionValue calculateDouble(double lval, ExpressionValue rval) {
    return rval.calculateDoubleRight(this, lval);
  }

  public final ExpressionValue calculateInteger(int lval, ExpressionValue rval) {
    return rval.calculateIntegerRight(this, lval);
  }

  public final ExpressionValue calculateFloat(float lval, ExpressionValue rval) {
    return rval.calculateFloatRight(this, lval);
  }
  
  public final ExpressionValue calculateByteArrayValue(byte[] lval, ExpressionValue rval) {
    return rval.calculateByteArrayValue(this, lval);
  }

  public abstract ExpressionValue calculateDoubles(double lval, double rval);
  
  public abstract ExpressionValue calculateIntegers(int lval, int rval);

  public abstract ExpressionValue calculateFloats(float lval, float rval);
  
  public abstract ExpressionValue calculateByteArrayValue(byte[] lval, byte[] rval);
  
  public abstract ExpressionValue calculateBigInteger(BigInteger lval, BigInteger rval);
  
  public abstract ExpressionValue calculateBigDecimal(BigDecimal lval, BigDecimal rval);

}
