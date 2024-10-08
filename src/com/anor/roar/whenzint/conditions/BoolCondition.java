package com.anor.roar.whenzint.conditions;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Predicate;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Condition;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.mapping.ByteBufferMap;
import com.anor.roar.whenzint.mapping.ByteBufferMapping;

public class BoolCondition extends Condition {

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

  @Override
  public boolean check(Program program) {
    return theOp.test(program);
  }
  
  public boolean checkLess(Program program) {
    Object refObj = program.getObject(ref);
    if(refObj != null && number != null && refObj instanceof Number) {
      Number n = (Number)refObj;
      return n.intValue() < number;
    }
    return false;
  }
  
  public boolean checkGreater(Program program) {
    Object refObj = program.getObject(ref);
    if(refObj != null && number != null && refObj instanceof Number) {
      Number n = (Number)refObj;
      return n.intValue() > number;
    }
    return false;
  }
  
  public boolean checkGreaterEqual(Program program) {
    Object refObj = program.getObject(ref);
    if(refObj != null && number != null) {
      if(refObj instanceof Number) {
        Number n = (Number)refObj;
        return n.intValue() >= number;
      }
    }
    return false;
  }
  
  public boolean checkLessEqual(Program program) {
    Object refObj = program.getObject(ref);
    if(refObj != null && number != null) {
      if(refObj instanceof Number) {
        Number n = (Number)refObj;
        return n.intValue() <= number;
      }
    }
    return false;
  }
  
  public boolean checkEqualEqual(Program program) {
    Object refObj = program.getObject(ref);
    if(refObj != null && cmp != null && !(refObj instanceof ByteBufferMapping)) {
      return refObj.equals(cmp);
    }else if(refObj != null && cmp != null && (refObj instanceof ByteBufferMapping)) {
      ByteBufferMapping mapping = (ByteBufferMapping) refObj;
      return mapping.equals(cmp, program);
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
