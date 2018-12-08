package com.anor.roar.whenzint;

import static org.junit.Assert.assertEquals;

import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Test;

import com.anor.roar.whenzint.actions.SetToLiteral;

public class SetToLiteralTest {

  @Test
  public void testCreateOpFunctionForPlus() {
    SetToLiteral stl = new SetToLiteral(null, null);
    Function<Number[], Number> fn = stl.createOpFunction("+");
    Number res = fn.apply(new Number[] { 4, 4 });
    assertEquals(8.0, res);
  }

  @Test
  public void testCreateOpFunctionForMinus() {
    SetToLiteral stl = new SetToLiteral(null, null);
    Function<Number[], Number> fn = stl.createOpFunction("-");
    Number res = fn.apply(new Number[] { 4, 4 });
    assertEquals(0.0, res);
  }

  @Test
  public void testCreateOpFunctionForMult() {
    SetToLiteral stl = new SetToLiteral(null, null);
    Function<Number[], Number> fn = stl.createOpFunction("*");
    Number res = fn.apply(new Number[] { 4, 4 });
    assertEquals(16.0, res);
  }

  @Test
  public void testCreateOpFunctionForDiv() {
    SetToLiteral stl = new SetToLiteral(null, null);
    Function<Number[], Number> fn = stl.createOpFunction("/");
    Number res = fn.apply(new Number[] { 4, 4 });
    assertEquals(1.0, res);
  }
}
