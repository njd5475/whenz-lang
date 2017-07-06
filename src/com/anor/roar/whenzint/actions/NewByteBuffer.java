package com.anor.roar.whenzint.actions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.VariablePath;
import com.anor.roar.whenzint.parser.Node;
import com.anor.roar.whenzint.parser.TokenBuffer;
import com.anor.roar.whenzint.parser.WhenzParser;
import com.anor.roar.whenzint.parser.WhenzSyntaxError;

public class NewByteBuffer extends Action {

  static {
    WhenzParser.getInstance().registerAction(new NewByteBuffer());
  }

  private VariablePath path;
  private VariablePath sizePath;
  private int staticSize;
  
  protected NewByteBuffer() {
    
  }
  
  public NewByteBuffer(VariablePath path, VariablePath size) {
    this.path = path;
    this.sizePath = size;
  }
  
  public NewByteBuffer(VariablePath path, int size) {
    this.path = path;
    this.staticSize = size;
  }

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens)
      throws WhenzSyntaxError, IOException {
    
    return null;
  }

  @Override
  public void perform(Program program, Map<String, Object> context) {
    int size = -1;
    if(sizePath != null) {
      Object obj = sizePath.get(context);
      if(obj instanceof Number) {
        size = ((Number)obj).intValue();
      }
    }
    
    if(staticSize > 0) {
      size = staticSize;
    }
    
    if(size > 0) {
      path.set(context, ByteBuffer.allocateDirect(size));
    }
  }

}
