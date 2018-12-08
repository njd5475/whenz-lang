package com.anor.roar.whenzint.expressions;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.anor.roar.whenzint.VariablePath;

public class MinusOperationTest {

  private Operation op = new MinusOperation();
  
  @Test
  public void testSubtractVariableWithInteger() {
    Map<String, Object> context = new HashMap<>();
    context.put("testVar", 2);
    VariableValue lval = new VariableValue(VariablePath.create("testVar"));
    lval.realize(context);
    ExpressionValue rval = new IntegerValue(3);
    ExpressionValue calculated = op.calculate(lval, rval);
    assertEquals(-1, calculated.get());
  }

}
