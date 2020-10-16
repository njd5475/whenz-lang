package com.anor.roar.whenzint.expressions;

import java.util.Map;

import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.VariablePath;

public class VariableValue implements ExpressionValue {

  private VariablePath path;
  private ExpressionValue realizedValue;

  public VariableValue(VariablePath path) {
    this.path = path;
  }
  
  public boolean realize(Program program, Map<String, Object> context) {
    Object object = path.get(context);
    if(object == null) {
      object = program.getObject(path.getFullyQualifiedName());
    }
    if(object instanceof Double) {
      realizedValue = new DoubleValue((double)object);
      return true;
    }else if(object instanceof Integer) {
      realizedValue = new IntegerValue((int)object);
      return true;
    }else if(object instanceof Float) {
      realizedValue = new FloatValue((float)object);
      return true;
    }
    return false;
  }
  
  public boolean hasRealizedValue() {
    return realizedValue != null;
  }
  
  @Override
  public ExpressionValue calculate(Operation op, ExpressionValue rval) {
    return op.calculate(realizedValue, rval);
  }

  @Override
  public ExpressionValue calculateDouble(Operation op, double rval) {
    return realizedValue.calculateDouble(op, rval);
  }

  @Override
  public ExpressionValue calculateInteger(Operation op, int rval) {
    return realizedValue.calculateInteger(op, rval);
  }

  @Override
  public ExpressionValue calculateFloat(Operation op, float rval) {
    return realizedValue.calculateFloat(op, rval);
  }

  @Override
  public Object get() {
    return realizedValue;
  }
  
  public String toString() {
    return this.path.getFullyQualifiedName();
  }

  @Override
  public ExpressionValue calculateDoubleRight(Operation op, double lval) {
    return op.calculate(new DoubleValue(lval), realizedValue);
  }

  @Override
  public ExpressionValue calculateIntegerRight(Operation op, int lval) {
    return op.calculate(new IntegerValue(lval), realizedValue);
  }

  @Override
  public ExpressionValue calculateFloatRight(Operation op, float lval) {
    return op.calculate(new FloatValue(lval), realizedValue);
  }

}
