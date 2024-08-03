package com.anor.roar.whenzint.expressions.operations;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.anor.roar.whenzint.expressions.values.BigDecimalValue;
import com.anor.roar.whenzint.expressions.values.ByteArrayValue;
import com.anor.roar.whenzint.expressions.values.DoubleValue;
import com.anor.roar.whenzint.expressions.values.ExpressionValue;
import com.anor.roar.whenzint.expressions.values.FloatValue;
import com.anor.roar.whenzint.expressions.values.IntegerValue;

public class MinusOperation extends Operation {

  @Override
  public ExpressionValue calculateDoubles(double lval, double rval) {
    return new DoubleValue(lval - rval);
  }


  @Override
  public ExpressionValue calculateIntegers(int lval, int rval) {
    return new IntegerValue(lval - rval);
  }


  @Override
  public ExpressionValue calculateFloats(float lval, float rval) {
    return new FloatValue(lval - rval);
  }


  @Override
  public ExpressionValue calculateByteArrayValue(byte[] lval, byte[] rval) {
    return new ByteArrayValue((new BigInteger(lval)).subtract(new BigInteger(rval)).toByteArray());
  }


  @Override
  public ExpressionValue calculateBigInteger(BigInteger lval, BigInteger rval) {
    return new ByteArrayValue(lval.subtract(rval).toByteArray());
  }


  @Override
  public ExpressionValue calculateBigDecimal(BigDecimal lval, BigDecimal rval) {
    return new BigDecimalValue(lval.subtract(rval));
  }

}
