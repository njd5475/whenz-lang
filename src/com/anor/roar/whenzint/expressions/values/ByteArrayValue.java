package com.anor.roar.whenzint.expressions.values;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.anor.roar.whenzint.expressions.operations.Operation;

public class ByteArrayValue implements ExpressionValue {

  private byte[] val;
  private BigInteger asBigInt;
  
  public ByteArrayValue(byte[] val) {
    this.val = val;
    this.asBigInt = new BigInteger(val);
  }
  
  @Override
  public Object get() {
    return val;
  }

  public String toString() {
    return new String(val);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Integer) {
      return asBigInt.equals(BigInteger.valueOf(((Integer) o).longValue()));
    } else if(o instanceof String) {
      return toString().equals((String)o);
    } else if (o instanceof ExpressionValue) {
      Object other = ((ExpressionValue) o).get();
      return other.equals(val);
    }
    return false;
  }

  @Override
  public ExpressionValue calculate(Operation op, ExpressionValue rval) {
    return op.calculateByteArrayValue(val, rval);
  }

  @Override
  public ExpressionValue calculateDouble(Operation op, double rval) {
    return op.calculateBigDecimal(new BigDecimal(asBigInt), new BigDecimal(rval));
  }

  @Override
  public ExpressionValue calculateInteger(Operation op, int rval) {
    return op.calculateBigInteger(asBigInt, BigInteger.valueOf((long)rval));
  }

  @Override
  public ExpressionValue calculateFloat(Operation op, float rval) {
    return op.calculateBigDecimal(new BigDecimal(asBigInt), BigDecimal.valueOf((double)rval));
  }
  
  @Override
  public ExpressionValue calculateDoubleRight(Operation op, double lval) {
    return op.calculateBigDecimal(BigDecimal.valueOf((double)lval), new BigDecimal(asBigInt));
  }

  @Override
  public ExpressionValue calculateIntegerRight(Operation op, int lval) {
    return op.calculateBigInteger(BigInteger.valueOf((long)lval), asBigInt);
  }

  @Override
  public ExpressionValue calculateFloatRight(Operation op, float lval) {
    return op.calculateBigDecimal(BigDecimal.valueOf((double)lval), new BigDecimal(asBigInt));
  }

  @Override
  public ExpressionValue calculateByteArrayValue(Operation op, byte[] lval) {
    return op.calculateByteArrayValue(lval, val);
  }

}
