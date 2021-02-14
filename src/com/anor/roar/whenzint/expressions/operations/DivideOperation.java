package com.anor.roar.whenzint.expressions;

import java.math.BigDecimal;
import java.math.BigInteger;

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

  @Override
  protected ExpressionValue calculateByteArrayValue(byte[] lval, byte[] rval) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected ExpressionValue calculateBigInteger(BigInteger lval, BigInteger rval) {
    return new ByteArrayValue(lval.divide(rval).toByteArray());
  }

  @Override
  protected ExpressionValue calculateBigDecimal(BigDecimal lval, BigDecimal rval) {
    return new BigDecimalValue(lval.divide(rval));
  }

}
