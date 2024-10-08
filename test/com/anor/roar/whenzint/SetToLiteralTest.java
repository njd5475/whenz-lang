package com.anor.roar.whenzint;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.anor.roar.whenzint.parser.CodeLocation;
import org.junit.Test;

import com.anor.roar.whenzint.actions.SetToLiteral;

public class SetToLiteralTest {

  @Test
  public void testCreateOpFunctionForPlus() {
    SetToLiteral stl = new SetToLiteral(CodeLocation.fake, null, null);
    Function<Number[], Number> fn = stl.createOpFunction("+");
    Number res = fn.apply(new Number[] { 4, 4 });
    assertEquals(8.0, res);
  }

  @Test
  public void testCreateOpFunctionForMinus() {
    SetToLiteral stl = new SetToLiteral(CodeLocation.fake, null, null);
    Function<Number[], Number> fn = stl.createOpFunction("-");
    Number res = fn.apply(new Number[] { 4, 4 });
    assertEquals(0.0, res);
  }

  @Test
  public void testCreateOpFunctionForMult() {
    SetToLiteral stl = new SetToLiteral(CodeLocation.fake, null, null);
    Function<Number[], Number> fn = stl.createOpFunction("*");
    Number res = fn.apply(new Number[] { 4, 4 });
    assertEquals(16.0, res);
  }

  @Test
  public void testCreateOpFunctionForDiv() {
    SetToLiteral stl = new SetToLiteral(CodeLocation.fake, null, null);
    Function<Number[], Number> fn = stl.createOpFunction("/");
    Number res = fn.apply(new Number[] { 4, 4 });
    assertEquals(1.0, res);
  }
  
  @Test
  public void testCanCoerceByteBuffer() {
    ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
    while(bb.remaining() > 1) { 
      bb.put((byte) 0);
    }
    bb.put((byte)35);
    bb.rewind();
    assertEquals(35, bb.getInt());
    
  }
}
