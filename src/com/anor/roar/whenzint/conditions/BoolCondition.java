package com.anor.roar.whenzint.conditions;

import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Predicate;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Condition;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.VariablePath;
import com.anor.roar.whenzint.expressions.Expression;
import com.anor.roar.whenzint.expressions.values.IntegerValue;
import com.anor.roar.whenzint.expressions.values.VariableValue;
import com.anor.roar.whenzint.mapping.ByteBufferMap;
import com.anor.roar.whenzint.mapping.ByteBufferMapping;

public class BoolCondition extends Condition {

  // TODO: rebuild the constructors to make these final
  private Expression expressionValue;
  private VariableValue cmpValue;
  private String ref;
  private Integer number = null;
  private String cmp = null;
  private Predicate<Program> theOp;

  public BoolCondition(String op, String ref, int num, boolean repeats) {
    this.ref = ref;
    this.number = num;
    if("==".equals(op)) {
      theOp = (p) -> {
        return checkEqualEqual(p);
      };
    }else if("<=".equals(op)) {
      theOp = (p) -> {
        return checkLessEqual(p);
      };
    }else if(">=".equals(op)) {
      theOp = (p) -> {
        return checkGreaterEqual(p);
      };
    }else if("!=".equals(op)) {
      theOp = (p) -> {
        return checkNotEqual(p);
      };
    }else if("<".equals(op)) {
      theOp = (p) -> {
        return checkLess(p);
      };
    }else if(">".equals(op)) {
      theOp = (p) -> {
        return checkGreater(p);
      };
    }
    this.repeats = repeats;
  }
  
  public BoolCondition(String op, String ref, String cmp, boolean repeats) {
    this.ref = ref;
    this.cmp = cmp;
    if("==".equals(op)) {
      theOp = (p) -> {
        return checkEqualEqual(p);
      };
    }else if("<=".equals(op)) {
      theOp = (p) -> {
        return checkLessEqual(p);
      };
    }else if(">=".equals(op)) {
      theOp = (p) -> {
        return checkGreaterEqual(p);
      };
    }else if("!=".equals(op)) {
      theOp = (p) -> {
        return checkNotEqual(p);
      };
    }else if("<".equals(op)) {
      theOp = (p) -> {
        return checkLess(p);
      };
    }else if(">".equals(op)) {
      theOp = (p) -> {
        return checkGreater(p);
      };
    }
    this.repeats = repeats;
  }

  public BoolCondition(String op, String ref, VariableValue cmpValue, boolean repeats) {
    this(op, ref, (String)null, repeats);
    if(cmpValue == null) {
      throw new NullPointerException("Need a value to compare");
    }
    this.cmpValue = cmpValue;
  }

  public BoolCondition(String op, String ref, Expression expression, boolean repeats) {
    this(op, ref, (String)null, repeats);
    if(expression == null) {
      throw new NullPointerException("Missing valid expression for Bool condition");
    }
    this.expressionValue = expression;
  }

  @Override
  public boolean check(Program program) {
    return theOp.test(program);
  }
  
  public boolean checkLess(Program program) {
    Object refObj = program.getObject(ref);
    Number num = getNumber(program);
    if(refObj != null && num != null && refObj instanceof Number) {
      Number n = (Number)refObj;
      return n.intValue() < num.intValue();
    }
    return false;
  }

  private Number getNumber(Program program) {
    Object value = number;

    if(expressionValue != null) {
      value = expressionValue.evaluate(program, null);

    }
    if(cmpValue != null && cmpValue.realize(program, program.getObjects())) {
        value = cmpValue.get();
        if(value instanceof  IntegerValue) {
          value = (Number)((IntegerValue)value).get();
        }
    }

    if(value != null && value instanceof Number) {
      return (Number)value;
    }

    return null;
    //throw new RuntimeException("NaN");
  }

  public boolean checkGreater(Program program) {
    Object refObj = program.getObject(ref);
    Number num = getNumber(program);
    if(refObj != null && num != null && refObj instanceof Number) {
      Number n = (Number)refObj;
      return n.intValue() > num.intValue();
    }
    return false;
  }
  
  public boolean checkGreaterEqual(Program program) {
    Object refObj = program.getObject(ref);
    Number num = getNumber(program);
    if(refObj != null && num != null) {
      if(refObj instanceof Number) {
        Number n = (Number)refObj;
        return n.intValue() >= num.intValue();
      }
    }
    return false;
  }
  
  public boolean checkLessEqual(Program program) {
    Object refObj = program.getObject(ref);
    Number num = getNumber(program);
    if(refObj != null && num != null) {
      if(refObj instanceof Number) {
        Number n = (Number)refObj;
        return n.intValue() <= num.intValue();
      }
    }
    return false;
  }

  // TODO: Abstract the retrieval of the number
  public boolean checkEqualEqual(Program program) {
    Object refObj = program.getObject(ref);
    if(refObj != null && cmp != null && !(refObj instanceof ByteBufferMapping)) {
      return refObj.equals(cmp);
    }else if(refObj != null && cmp != null && (refObj instanceof ByteBufferMapping)) {
      ByteBufferMapping mapping = (ByteBufferMapping) refObj;
      return mapping.equals(cmp, program);
    }else if(refObj != null && cmpValue != null && cmp == null && !(refObj instanceof ByteBufferMapping)) {
      if(cmpValue.realize(program, program.getObjects())) {
        return cmpValue.get().equals(refObj);
      }else{
        //TODO: throw runtime exception for invalid or unrealizable value
      }
    }else if(refObj != null && expressionValue != null && cmpValue == null && cmp == null && !(refObj instanceof ByteBufferMapping)) {
      Object value = expressionValue.evaluate(program, null);
      if(value != null) {
        return value.equals(refObj);
      }
      // TODO: throw runtime exception event when expression fails
    }else if(refObj instanceof ByteBuffer) {
      ByteBuffer bb = (ByteBuffer)refObj;
      ByteBuffer forInt = ByteBuffer.allocate(Integer.BYTES);
      bb.rewind();
      forInt.put(bb);
      forInt.rewind();
      int refNum = Integer.reverseBytes(forInt.getInt());
      return number == refNum;
    }else if(refObj != null) {
      return refObj.equals(number);
    }else{
      //System.err.format("RefObj %s is null\n", ref);
    }
    return false;
  }
  
  private boolean checkNotEqual(Program program) {
    return !checkEqualEqual(program);
  }
}
