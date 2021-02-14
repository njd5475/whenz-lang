package com.anor.roar.whenzint.expressions;

import java.math.BigDecimal;
import java.math.BigInteger;

public class PlusOperation extends Operation {

  @Override
  public ExpressionValue calculateDoubles(double lval, double rval) {
    return new DoubleValue(lval + rval);
  }

  @Override
  public ExpressionValue calculateIntegers(int lval, int rval) {
    return new IntegerValue(lval + rval);
  }

  @Override
  public ExpressionValue calculateFloats(float val, float rval) {
    return new FloatValue(val + rval);
  }

  @Override
  protected ExpressionValue calculateByteArrayValue(byte[] lval, byte[] rval) {
    return new ByteArrayValue(new BigInteger(lval).add(new BigInteger(rval)).toByteArray());
  }

  @Override
  protected ExpressionValue calculateBigInteger(BigInteger lval, BigInteger rval) {
    return new ByteArrayValue(lval.add(rval).toByteArray());
  }

  @Override
  protected ExpressionValue calculateBigDecimal(BigDecimal lval, BigDecimal rval) {
    return new BigDecimalValue(lval.add(rval));
  }

}
