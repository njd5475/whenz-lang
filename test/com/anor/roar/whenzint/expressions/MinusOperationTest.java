package com.anor.roar.whenzint.expressions;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.VariablePath;
import com.anor.roar.whenzint.expressions.operations.MinusOperation;
import com.anor.roar.whenzint.expressions.operations.Operation;
import com.anor.roar.whenzint.expressions.values.ExpressionValue;
import com.anor.roar.whenzint.expressions.values.IntegerValue;
import com.anor.roar.whenzint.expressions.values.VariableValue;

public class MinusOperationTest {

  private Operation op = new MinusOperation();
  
  @Test
  public void testSubtractVariableWithInteger() {
    Map<String, Object> context = new HashMap<>();
    context.put("testVar", 2);
    VariableValue lval = new VariableValue(VariablePath.create("testVar"));
    Program program = new Program();
    lval.realize(program, context);
    ExpressionValue rval = new IntegerValue(3);
    ExpressionValue calculated = op.calculate(lval, rval);
    assertEquals(-1, calculated.get());
  }

}
