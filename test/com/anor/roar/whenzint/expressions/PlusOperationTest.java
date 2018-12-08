package com.anor.roar.whenzint.expressions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PlusOperationTest {
  
  private Operation op = new PlusOperation();

  @Test
  public void testCalculateDoubles() {
    ExpressionValue val = op.calculate(new DoubleValue(4.2), new DoubleValue(5.6));
    assertEquals(9.8, val.get());
  }
  
  @Test
  public void testCalculateIntegers() {
    ExpressionValue val = op.calculate(new IntegerValue(4), new IntegerValue(5));
    assertEquals(9, val.get());
  }
  
  @Test
  public void testCalculateFloats() {
    ExpressionValue val = op.calculate(new FloatValue(4.2f), new FloatValue(5.6f));
    assertEquals(9.8f, (float)val.get(), 0.001f);
  }
  
  @Test
  public void testCalculateDoubleWithInteger() {
    ExpressionValue val = op.calculate(new DoubleValue(4.2), new IntegerValue(5));
    assertEquals(9.2, val.get());
  }
  
  @Test
  public void testCalculateIntegerWithDouble() {
    ExpressionValue val = op.calculate(new IntegerValue(5), new DoubleValue(4.2));
    assertEquals(9.2, val.get());
  }
  
  @Test
  public void testCalculateFloatWithDouble() {
    ExpressionValue val = op.calculate(new FloatValue(4.2f), new DoubleValue(5.6));
    assertEquals(9.8, (double)val.get(), 0.001);
  }
  
  @Test
  public void testCalculateIntegerWithFloat() {
    ExpressionValue val = op.calculate(new IntegerValue(6), new FloatValue(0.4f));
    assertEquals(6.4f, val.get());
  }
  
  @Test
  public void testCalculateDoubleWithFloat() {
    ExpressionValue val = op.calculate(new DoubleValue(4.2), new FloatValue(5.6f));
    assertTrue("Val must be of type DoubleValue but was " + val.get().getClass(), val.get() instanceof Double);
    assertEquals(9.8, (double)val.get(), 0.00001);
  }
  
  @Test
  public void testCalculateFloatWithInteger() {
    ExpressionValue val = op.calculate(new FloatValue(0.4f), new IntegerValue(6));
    assertEquals(6.4f, val.get());
  }
}
