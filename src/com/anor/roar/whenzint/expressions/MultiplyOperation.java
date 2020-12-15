package com.anor.roar.whenzint.expressions;

import java.math.BigDecimal;
import java.math.BigInteger;

public class MultiplyOperation extends Operation {

  @Override
  protected ExpressionValue calculateDoubles(double lval, double rval) {
    return new DoubleValue(lval * rval);
  }

  @Override
  protected ExpressionValue calculateIntegers(int lval, int rval) {
    return new IntegerValue(lval * rval);
  }

  @Override
  protected ExpressionValue calculateFloats(float lval, float rval) {
    return new FloatValue(lval * rval);
  }

  @Override
  protected ExpressionValue calculateByteArrayValue(byte[] lval, byte[] rval) {
    return new ByteArrayValue(new BigInteger(lval).multiply(new BigInteger(rval)).toByteArray());
  }

  @Override
  protected ExpressionValue calculateBigInteger(BigInteger lval, BigInteger rval) {
    return new ByteArrayValue(lval.subtract(rval).toByteArray());
  }

  @Override
  protected ExpressionValue calculateBigDecimal(BigDecimal lval, BigDecimal rval) {
    return new BigDecimalValue(lval.multiply(rval));
  }

}
