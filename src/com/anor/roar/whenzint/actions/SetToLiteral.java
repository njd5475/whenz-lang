package com.anor.roar.whenzint.actions;

import java.io.IOException;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.VariablePath;
import com.anor.roar.whenzint.expressions.Expression;
import com.anor.roar.whenzint.expressions.MathOpData;
import com.anor.roar.whenzint.expressions.MathOps;
import com.anor.roar.whenzint.expressions.operations.DivideOperation;
import com.anor.roar.whenzint.expressions.operations.MinusOperation;
import com.anor.roar.whenzint.expressions.operations.MultiplyOperation;
import com.anor.roar.whenzint.expressions.operations.PlusOperation;
import com.anor.roar.whenzint.expressions.values.DoubleValue;
import com.anor.roar.whenzint.expressions.values.IntegerValue;
import com.anor.roar.whenzint.expressions.values.VariableValue;
import com.anor.roar.whenzint.parser.*;

public class SetToLiteral extends Action {

  private String name;
  private Object literal;

  static {
    ProgramBuilder.registerActionBuilder(new SetToLiteral(null, null));
  }

  public SetToLiteral(String name, Object literal) {
    this.name = name;
    this.literal = literal;
  }

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens) throws WhenzSyntaxError, IOException {
    Node assignNode = new Node("Assignment");
    parser.globalReference(assignNode, tokens);
    parser.consumeWhitespace(tokens);
    parser.assignment(assignNode, tokens);
    parser.consumeWhitespace(tokens);
    parser.expression(assignNode, tokens);
    parser.consumeWhitespace(tokens, true);
    return assignNode;
  }

  @Override
  public void perform(Program program, Map<String, Object> context) {
    if(literal instanceof VariablePath) {
      VariablePath pathObj = (VariablePath) literal;
      program.setObject(name, program.getObject(pathObj.getFullyQualifiedName()));
      return;
    }
    program.setObject(name, literal);
  }

  @Override
  public Action buildAction(ProgramBuilder builder, Node node) throws WhenzSyntaxTreeError {
    Node lval = node.children()[0];
    Node rval = node.children()[2];
    String quickRef = builder.referenceString(lval.children());
    if ("Expression".equals(rval.name())) {
      boolean containsExp = rval.hasChildNamed("Expression");
      if (rval.hasChildNamed("Literals") && !containsExp) {
        Node literal = rval.getChildNamed("Literals");
        return new SetToLiteral(quickRef, literal.children()[0].getTokenOrValue());
      } else if (rval.hasChildNamed("Number") && !containsExp) {
        Node literal = rval.getChildNamed("Number");
        return new SetToLiteral(quickRef, Integer.parseInt(literal.children()[0].getTokenOrValue()));
      } else if (rval.hasChildNamed("HexLiteral") && !containsExp) {
        Node literal = rval.getChildNamed("HexLiteral");
        return new SetToLiteral(quickRef, Integer.parseInt(literal.children()[0].getTokenOrValue(), 16));
      } else if (rval.hasChildNamed("Reference") && !containsExp) {
        return new SetToLiteral(quickRef, builder.getPath(rval.getChildNamed("Reference")));
      } else {
        Stack<MathOpData> ops = new Stack<>();
        buildExpression(builder, ops, rval);
        return new Expression(quickRef, ops);
      }
    }

    return null;
  }

  public Function<Number[], Number> createOpFunction(String op) {
    Function<Number[], Number> fn = null;
    if ("+".equals(op)) {
      fn = (args) -> {
        return args[0].doubleValue() + args[1].doubleValue();
      };
    } else if ("-".equals(op)) {
      fn = (args) -> {
        return args[0].doubleValue() - args[1].doubleValue();
      };
    } else if ("*".equals(op)) {
      fn = (args) -> {
        return args[0].doubleValue() * args[1].doubleValue();
      };
    } else if ("/".equals(op)) {
      fn = (args) -> {
        return args[0].doubleValue() / args[1].doubleValue();
      };
    }

    return fn;
  }

  public void buildExpression(ProgramBuilder builder, Stack<MathOpData> expressions, Node expression) throws WhenzSyntaxTreeError {
    boolean grp = false;
    boolean skipChildren = false;
    if (expression.name().contains("Operator")) {
      if ("+".equals(expression.getToken())) {
        expressions.push(MathOps.PLUS.with(new PlusOperation()));
      } else if ("-".equals(expression.getToken())) {
        expressions.push(MathOps.MINUS.with(new MinusOperation()));
      } else if ("/".equals(expression.getToken())) {
        expressions.push(MathOps.DIV.with(new DivideOperation()));
      } else if ("*".equals(expression.getToken())) {
        expressions.push(MathOps.MULT.with(new MultiplyOperation()));
      }
    } else if (expression.name().contains("ExpGroup")) {
      grp = true;
      expressions.push(MathOps.GROUP_BEGIN.with(null));
    } else if (expression.name().contains("Decimal")) {
      double decimal = builder.buildDecimal(expression);
      expressions.push(MathOps.NUMBER.with(new DoubleValue(decimal)));
      skipChildren = true;
    } else if (expression.name().contains("Number")) {
      int num = Integer.parseInt(expression.children()[0].getTokenOrValue());
      if (expression.hasChildNamed("Sign")) {
        if ("-".equals(expression.getChildNamed("Sign").getTokenOrValue())) {
          num = -num;
        }
      }
      expressions.push(MathOps.NUMBER.with(new IntegerValue(num)));
    } else if (expression.name().contains("Reference")) {
      expressions.push(MathOps.VARIABLE.with(new VariableValue(builder.getPath(expression))));
    }

    if (expression.children().length > 0 && !skipChildren) {
      for (Node exp : expression.children()) {
        buildExpression(builder, expressions, exp);
      }
    } else if (expression.children().length == 0) {

    }

    if (grp) {
      expressions.push(MathOps.GROUP_END.with(null));
    }
  }

  @Override
  public String getActionNodeName() {
    return "Assignment";
  }

}
