package com.anor.roar.whenzint.actions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.VariablePath;
import com.anor.roar.whenzint.mapping.ByteBufferMapping;
import com.anor.roar.whenzint.parser.Node;
import com.anor.roar.whenzint.parser.ProgramBuilder;
import com.anor.roar.whenzint.parser.TokenBuffer;
import com.anor.roar.whenzint.parser.WhenzParser;
import com.anor.roar.whenzint.parser.WhenzSyntaxError;

public class ReadFromFileChannel extends Action {

  private VariablePath    from;
  private VariablePath    to;
  private Set<OpenOption> options = new HashSet<>();

  static {
    ProgramBuilder.registerActionBuilder(new ReadFromFileChannel(null, null));
  }

  public ReadFromFileChannel(VariablePath from, VariablePath to) {
    this.from = from;
    this.to = to;
    options.add(StandardOpenOption.READ);
  }

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens) throws WhenzSyntaxError, IOException {
    parser.consumeWhitespace(tokens);
    if (tokens.peek().is("read")) {
      tokens.take();
      parser.consumeWhitespace(tokens);
      Node n = new Node(this.getActionNodeName());
      parser.globalReference(n.addChild("from"), tokens);
      parser.consumeWhitespace(tokens);
      if (tokens.peek().is("into")) {
        tokens.take();
        parser.consumeWhitespace(tokens);
        parser.globalReference(n.addChild("into"), tokens);
        parser.consumeWhitespace(tokens, true);
        return n;
      } else {
        parser.unexpectedToken(tokens);
      }
    } else {
      parser.unexpectedToken(tokens);
    }
    return null;
  }

  @Override
  public Action buildAction(ProgramBuilder builder, Node node) {
    Node from = node.getChildNamed("from");
    Node into = node.getChildNamed("into");
    VariablePath pathFrom = builder.getPath(from.getChildNamed("Reference"));
    VariablePath pathInto = builder.getPath(into.getChildNamed("Reference"));

    return new ReadFromFileChannel(pathFrom, pathInto);
  }

  @Override
  public String getActionNodeName() {
    return "ReadToBuf";
  }

  @Override
  public void perform(Program program, Map<String, Object> context) {
    String path = program.getObject(from.getFullyQualifiedName()).toString();
    try {
      FileChannel fileChannel = FileChannel.open(Paths.get(path), options);

      Object o = program.getObject(to.getFullyQualifiedName());
      long byteCount = -1;
      long offset = 0;
      String varName = to.getFullyQualifiedName();
      if (o instanceof ByteBuffer) {
        ByteBuffer b = (ByteBuffer) o;
        b.rewind();
        if(program.hasObject(varName + ".position")) {
          offset = (long) program.getObject(varName + ".position");
        }
        fileChannel.position(offset);
        byteCount = fileChannel.read(b);
        program.setObject(varName + ".position", fileChannel.position());
        program.setObject(varName + ".lastReadLength", byteCount);
        program.changeState(varName + ".monitor", "bufferFull");
        if(byteCount < b.limit()) {
          program.changeState(varName + ".monitor", "eof");  
        }
      } else if (o instanceof ByteBufferMapping) {
        ByteBufferMapping bbm = (ByteBufferMapping) o;

        if(program.hasObject(varName + ".position")) {
          offset = (long) program.getObject(varName + ".position");
        }
        
        ByteBuffer b = (ByteBuffer) bbm.getPath().get(context);
        fileChannel.position(offset);
        byteCount = fileChannel.read(new ByteBuffer[] { b }, bbm.getLocation(), 1);
        program.setObject(varName + ".position", fileChannel.position());
        program.setObject(varName + ".lastReadLength", byteCount);
        program.changeState(varName + ".monitor", "bufferFull");
        if(byteCount < b.limit()) {
          program.changeState(varName + ".monitor", "eof");  
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

}
