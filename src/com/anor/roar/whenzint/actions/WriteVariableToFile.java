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
import com.anor.roar.whenzint.parser.Node;
import com.anor.roar.whenzint.parser.ProgramBuilder;
import com.anor.roar.whenzint.parser.TokenBuffer;
import com.anor.roar.whenzint.parser.WhenzParser;
import com.anor.roar.whenzint.parser.WhenzSyntaxError;

public class WriteVariableToFile extends Action {
  
  static {
    ProgramBuilder.registerActionBuilder(new WriteVariableToFile());
  }

  private String fileLiteral;
  private VariablePath path;

  public WriteVariableToFile() {
    // TODO Auto-generated constructor stub
  }

  public WriteVariableToFile(VariablePath path, String literal) {
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
        parser.unexpectedToken(tokens.peek());
      }
    }else {
      parser.unexpectedToken(tokens.peek());
    }
    return null;
  }

  @Override
  public Action buildAction(ProgramBuilder builder, Node node) {
    VariablePath path = builder.getPath(node.getChildNamed("Reference"));
    Node literalParts = node.getChildNamed("Literals");
    Node parts[] = literalParts.children();
    String literal = builder.referenceString(parts);
    
    return new WriteVariableToFile(path, literal);
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
      
      if(o != null && o instanceof ByteBuffer) {
        ByteBuffer bb = (ByteBuffer) o;
        
        try {
          FileOutputStream fos = new FileOutputStream(f);
          FileChannel channel = fos.getChannel();
          
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
