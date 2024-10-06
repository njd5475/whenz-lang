package com.anor.roar.whenzint.actions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.VariablePath;
import com.anor.roar.whenzint.parser.*;

public class WriteVariableToFile extends AbstractAction {
  
  static {
    ProgramBuilder.registerActionBuilder(new WriteVariableToFile());
  }

  private String fileLiteral;
  private VariablePath path;

  public WriteVariableToFile() {
    super(CodeLocation.fake);
    // TODO Auto-generated constructor stub
  }

  public WriteVariableToFile(CodeLocation location, VariablePath path, String literal) {
    super(location);
    if(path == null) {
      throw new NullPointerException("Nothing to be written to file!");
    }
    if(literal == null || literal.trim().isEmpty())  {
      throw new IllegalArgumentException("Cannot use a empty string for a file");
    }
    this.path = path;
    this.fileLiteral = literal;
  }

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens) throws WhenzSyntaxError, IOException {
    if(tokens.peek().is("write")) {
      Node writeNode = new Node("WriteToFile");
      tokens.take();
      parser.consumeWhitespace(tokens);
      parser.globalReference(writeNode, tokens);
      parser.consumeWhitespace(tokens);
      if(tokens.peek().is("to")) {
        tokens.take();
        parser.consumeWhitespace(tokens);
        try {
          parser.globalReference(writeNode, tokens);  
        }catch(WhenzSyntaxError e) {
          parser.literals(writeNode, tokens);
        }
        return writeNode;
      }else {
        parser.unexpectedToken(tokens);
      }
    }else {
      parser.unexpectedToken(tokens);
    }
    return null;
  }

  @Override
  public Action buildAction(ProgramBuilder builder, Node node) throws WhenzSyntaxTreeError {
    VariablePath path = builder.getPath(node.getChildNamed("Reference"));
    Node literalParts = node.getChildNamed("Literals");
    Node parts[] = literalParts.children();
    String literal = builder.referenceString(parts);
    
    return new WriteVariableToFile(CodeLocation.toLocation(node), path, literal);
  }

  @Override
  public String getActionNodeName() {
    return "WriteToFile";
  }

  @Override
  public void perform(Program program, Map<String, Object> context) {
    // TODO Auto-generated method stub
    if(fileLiteral != null) {
      File f = new File(fileLiteral);
      if(!f.exists()) {
        try {
          f.createNewFile();
        } catch(IOException e) {
          e.printStackTrace();
        }
      }
      
      Object o = path.get(context);
      if(o == null) {
        o = program.getObject(path.getFullyQualifiedName());
      }
      
      if(o != null && o instanceof ByteBuffer) {
        ByteBuffer bb = (ByteBuffer) o;
        
        try {
          FileOutputStream fos = new FileOutputStream(f);
          FileChannel channel = fos.getChannel();
          
          bb.rewind();
          channel.write(bb);
          fos.flush();
          fos.close();
          
        } catch(FileNotFoundException e) {
          e.printStackTrace();
        } catch(IOException e) {
          e.printStackTrace();
        }
      }else {
        
      }
      
    }
  }

}
