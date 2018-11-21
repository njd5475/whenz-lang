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

public class PutsAction extends Action {

  private String toPrint;
  private String format;
  private VariablePath[] array;
  
  static {
    ProgramBuilder.registerActionBuilder(new PutsAction((String)null));
  }

  public PutsAction(String toPrint) {
    this.toPrint = toPrint;
    this.format = null;
    this.array = null;
  }

  public PutsAction(List<Object> opts) {
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
          bb.rewind();
          vals[i] = new String(bb.array(), mapping.getLocation(), mapping.getNumberOfBytes(program));
        }else if(vals[i] instanceof ByteBuffer) {
          ByteBuffer bb = (ByteBuffer)vals[i];
          bb.rewind();
          vals[i] = new String(bb.array()).trim();
        }
        ++i;
      }
      System.out.format(format, vals);
    }else{
      System.out.print(toPrint);
    }
  }

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens)
      throws WhenzSyntaxError, IOException {
    Node printAction = new Node(this.getActionNodeName());
    if (tokens.peek().is("put")) {
      tokens.take();
      parser.consumeWhitespace(tokens);
      boolean skipNext = false;
      while (!tokens.peek().isNewline()) {
        if(!skipNext && tokens.peek().isSymbol("@")) {
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
    return new PutsAction(opts);
  }

  @Override
  public String getActionNodeName() {
    return "PutsAction";
  }

}
