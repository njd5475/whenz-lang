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

      Object o = to.get(context);
      if (o instanceof ByteBuffer) {
        long readNum = fileChannel.read((ByteBuffer) o);
        program.setObject(to.getFullyQualifiedName() + ".lastReadLength", readNum);
        program.changeState(to.getFullyQualifiedName() + ".monitor", "bufferFull");
      } else if (o instanceof ByteBufferMapping) {
        ByteBufferMapping bbm = (ByteBufferMapping) o;
        
        ByteBuffer b = (ByteBuffer) bbm.getPath().get(context);
        long readNum = fileChannel.read(new ByteBuffer[] { b }, bbm.getLocation(), bbm.getNumberOfBytes());
        program.setObject(to.getFullyQualifiedName() + ".lastReadLength", readNum);
        program.setObject(to.getFullyQualifiedName() + ".monitor", "bufferFull");
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

}
