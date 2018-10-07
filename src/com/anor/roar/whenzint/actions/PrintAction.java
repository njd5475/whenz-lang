package com.anor.roar.whenzint.actions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.VariablePath;
import com.anor.roar.whenzint.mapping.ByteBufferMapping;
import com.anor.roar.whenzint.parser.Node;
import com.anor.roar.whenzint.parser.ProgramBuilder;
import com.anor.roar.whenzint.parser.TokenBuffer;
import com.anor.roar.whenzint.parser.WhenzParser;
import com.anor.roar.whenzint.parser.WhenzSyntaxError;

public class PrintAction extends Action {

  private String toPrint;
  private String format;
  private VariablePath[] array;
  
  static {
    ProgramBuilder.registerActionBuilder(new PrintAction((String)null));
  }

  public PrintAction(String toPrint) {
    this.toPrint = toPrint;
    this.format = null;
    this.array = null;
  }

  public PrintAction(List<Object> opts) {
    StringBuilder bld = new StringBuilder("");
    List<Object> values = new LinkedList<Object>();
    for(Object opt : opts) {
      if(opt instanceof VariablePath) {
        values.add(opt);
        bld.append("%s");
      }else{
        bld.append(opt.toString());
      }
    }
    array = values.toArray(new VariablePath[values.size()]);
    toPrint = format = bld.toString();
    
  }

  @Override
  public void perform(Program program, Map<String, Object> context) {
    if(array != null && array.length > 0) {
      Object[] vals = new Object[array.length];
      int i = 0;
      for(VariablePath path : array) {
        vals[i] = program.getObject(path.getFullyQualifiedName());
        if(vals[i] instanceof ByteBufferMapping) {
          ByteBufferMapping mapping = (ByteBufferMapping)vals[i];
          VariablePath p = mapping.getPath();
          Object obj = program.getObject(p.getFullyQualifiedName());
          ByteBuffer bb = (ByteBuffer)obj;
          vals[i] = new String(bb.array(), mapping.getLocation(), mapping.getNumberOfBytes(program));
        }else if(vals[i] instanceof ByteBuffer) {
          ByteBuffer bb = (ByteBuffer)vals[i];
          vals[i] = new String(bb.array()).trim();
        }
        ++i;
      }
      System.out.format(format + "\n", vals);
    }else{
      System.out.println(toPrint);
    }
  }

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens)
      throws WhenzSyntaxError, IOException {
    Node printAction = new Node("PrintAction");
    if (tokens.peek().is("print")) {
      tokens.take();
      parser.consumeWhitespace(tokens);
      boolean skipNext = false;
      while (!tokens.peek().isNewline()) {
        if(tokens.peek().isSymbol("\\")) {
          tokens.take();
          skipNext = true;
        }else if(!skipNext && tokens.peek().isSymbol("@")) {
          parser.globalReference(printAction, tokens);
          skipNext = false;
        }else {
          skipNext = false;
          printAction.add(new Node("string part", tokens.take()));
        }
      }
      if (tokens.peek().isNewline()) {
        tokens.take(); // consume the newline token
      } else {
        parser.unexpectedToken(tokens.peek());
      }
    } else {
      parser.unexpectedToken(tokens.peek());
    }
    return printAction;
  }

  @Override
  public Action buildAction(ProgramBuilder builder, Node node) {
    List<Object> opts = new LinkedList<Object>();
    for (Node part : node.children()) {
      if(part.isNamed("Reference")) {
        VariablePath path = builder.getPath(part);
        opts.add(path);
      }else{
        opts.add(part.getToken());
      }
    }
    return new PrintAction(opts);
  }

  @Override
  public String getActionNodeName() {
    return "PrintAction";
  }

}
