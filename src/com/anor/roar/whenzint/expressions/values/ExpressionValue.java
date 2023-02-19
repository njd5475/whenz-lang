package com.anor.roar.whenzint.expressions.values;

import com.anor.roar.whenzint.expressions.operations.Operation;

public interface ExpressionValue {

  ExpressionValue calculate(Operation op, ExpressionValue rval);

  ExpressionValue calculateDouble(Operation op, double rval);

  ExpressionValue calculateInteger(Operation op, int rval);

  ExpressionValue calculateFloat(Operation op, float rval);
  
  Object get();

  ExpressionValue calculateDoubleRight(Operation op, double lval);

  ExpressionValue calculateIntegerRight(Operation op, int lval);

  ExpressionValue calculateFloatRight(Operation op, float lval);
  
  ExpressionValue calculateByteArrayValue(Operation op, byte[] lval);

}
