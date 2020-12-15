package com.anor.roar.whenzint.expressions;

import java.math.BigDecimal;

public class BigDecimalValue implements ExpressionValue {

  private BigDecimal val;

  public BigDecimalValue(BigDecimal divide) {
    this.val = divide;
  }

  @Override
  public ExpressionValue calculate(Operation op, ExpressionValue rval) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ExpressionValue calculateDouble(Operation op, double rval) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ExpressionValue calculateInteger(Operation op, int rval) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ExpressionValue calculateFloat(Operation op, float rval) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object get() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ExpressionValue calculateDoubleRight(Operation op, double lval) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ExpressionValue calculateIntegerRight(Operation op, int lval) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ExpressionValue calculateFloatRight(Operation op, float lval) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ExpressionValue calculateByteArrayValue(Operation op, byte[] lval) {
    // TODO Auto-generated method stub
    return null;
  }

}
