package com.anor.roar.whenzint.actions;

import static com.anor.roar.whenzint.expressions.MathOps.DIV;
import static com.anor.roar.whenzint.expressions.MathOps.GROUP_BEGIN;
import static com.anor.roar.whenzint.expressions.MathOps.GROUP_END;
import static com.anor.roar.whenzint.expressions.MathOps.MINUS;
import static com.anor.roar.whenzint.expressions.MathOps.MULT;
import static com.anor.roar.whenzint.expressions.MathOps.NUMBER;
import static com.anor.roar.whenzint.expressions.MathOps.PLUS;
import static com.anor.roar.whenzint.expressions.MathOps.VARIABLE;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Stack;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.expressions.ExpressionValue;
import com.anor.roar.whenzint.expressions.MathOpData;
import com.anor.roar.whenzint.expressions.Operation;
import com.anor.roar.whenzint.expressions.VariableValue;
import com.anor.roar.whenzint.parser.Node;
import com.anor.roar.whenzint.parser.ProgramBuilder;
import com.anor.roar.whenzint.parser.TokenBuffer;
import com.anor.roar.whenzint.parser.WhenzParser;
import com.anor.roar.whenzint.parser.WhenzSyntaxError;

public class Expression extends Action {

  private String            ref = null;
  private Stack<MathOpData> ops;

  public Expression(String quickRef, Stack<MathOpData> ops) {
    this.ref = quickRef;
    this.ops = ops;
    Collections.reverse(this.ops);
    this.ops = convertToPostFix(ops);
    Collections.reverse(this.ops);
  }

  private void printOps(Stack<MathOpData> ops2) {
    Stack<MathOpData> clone = (Stack<MathOpData>) ops.clone();
    StringBuilder expression = new StringBuilder();
    while(!clone.isEmpty()) {
      MathOpData d = clone.pop();
      if(d.getOp() == PLUS) {
        expression.append('+');
      }else if(d.getOp() == MINUS) {
        expression.append('-');
      }else if(d.getOp() == DIV) {
        expression.append('/');
      }else if(d.getOp() == MULT) {
        expression.append('*');
      }else if(d.getOp() == NUMBER) {
        expression.append(d.getValue().toString());
      }else if(d.getOp() == VARIABLE) {
        expression.append("@");
        expression.append(d.getValue().toString());
      }
      expression.append(' ');
    }
    System.out.println(expression.toString().trim());
  }

  private Stack<MathOpData> convertToPostFix(Stack<MathOpData> ops2) {
    Stack<MathOpData> output = new Stack<MathOpData>();
    Stack<MathOpData> work = new Stack<MathOpData>();
    while (!ops2.isEmpty()) {
      MathOpData pop = ops2.pop();
      if (pop.getOp() == NUMBER || pop.getOp() == VARIABLE) {
        output.push(pop);
      } else if (work.isEmpty()) {
        work.push(pop);
      } else if (pop.getOp() == GROUP_END) {
        // pop till group begin
        while (work.peek().getOp() != GROUP_BEGIN) {
          output.push(work.pop());
        }
        work.pop();
      } else if(pop.getOp() == GROUP_BEGIN) {
        work.push(pop);
      } else {
        while (!work.isEmpty() && pop.getOp().isHigher(work.peek().getOp())) {
          MathOpData pop2 = work.pop();
          output.push(pop2);
        }
        work.push(pop);
      }
    }
    while(!work.empty()) {
      MathOpData d = work.pop();
      if ((d.getOp() != GROUP_BEGIN || d.getOp() != GROUP_END)) {
        output.push(d);
      }
    }
    return output;
  }

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens) throws WhenzSyntaxError, IOException {
    return null;
  }

  @Override
  public Action buildAction(ProgramBuilder builder, Node node) {
    return null;
  }

  @Override
  public String getActionNodeName() {
    return "Assignment";
  }

  @Override
  public void perform(Program program, Map<String, Object> context) {
    Stack<MathOpData> toProcess = (Stack<MathOpData>) ops.clone();
    Stack<ExpressionValue> waiting = new Stack<>();
    while(!toProcess.isEmpty()) {
      MathOpData d = toProcess.pop();
      if(d.getValue() instanceof Operation) {
        Operation op = (Operation) d.getValue();
        ExpressionValue rval = waiting.pop();
        ExpressionValue lval = waiting.pop();
        if(rval instanceof VariableValue) {
          ((VariableValue)rval).realize(program, context);
        }
        if(lval instanceof VariableValue) {
          ((VariableValue)lval).realize(program, context);
        }
        waiting.push(op.calculate(lval, rval));
      }else {
        waiting.push((ExpressionValue) d.getValue());
      }
    }
    if(!waiting.isEmpty()) {
      ExpressionValue pop = waiting.pop();
      if(pop instanceof VariableValue) {
        VariableValue v = (VariableValue) pop;
        v.realize(program, context);
      }
      program.setObject(ref, pop.get());
      context.put(ref, pop.get());
    }
  }

  public String toString() {
    return "Expression";
  }
}
