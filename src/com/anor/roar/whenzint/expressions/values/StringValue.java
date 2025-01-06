package com.anor.roar.whenzint.expressions.values;

import com.anor.roar.whenzint.expressions.operations.Operation;

public class StringValue implements ExpressionValue {

    private String value;

    public StringValue(String object) {
        value = object;
    }

    @Override
    public ExpressionValue calculate(Operation op, ExpressionValue rval) {
        return null;
    }

    @Override
    public ExpressionValue calculateDouble(Operation op, double rval) {
        return null;
    }

    @Override
    public ExpressionValue calculateInteger(Operation op, int rval) {
        return null;
    }

    @Override
    public ExpressionValue calculateFloat(Operation op, float rval) {
        return null;
    }

    @Override
    public Object get() {
        return value;
    }

    @Override
    public ExpressionValue calculateDoubleRight(Operation op, double lval) {
        return null;
    }

    @Override
    public ExpressionValue calculateIntegerRight(Operation op, int lval) {
        return null;
    }

    @Override
    public ExpressionValue calculateFloatRight(Operation op, float lval) {
        return null;
    }

    @Override
    public ExpressionValue calculateByteArrayValue(Operation op, byte[] lval) {
        return null;
    }

    public boolean equals(Object obj) {
        return value.equals(obj);
    }
}
