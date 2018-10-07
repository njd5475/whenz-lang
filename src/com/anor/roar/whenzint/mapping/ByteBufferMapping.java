package com.anor.roar.whenzint.mapping;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Map;

import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.VariablePath;

public class ByteBufferMapping {

  private int          numberOfBytes;
  private VariablePath path;
  private VariablePath pathToBytes;
  private int          location;

  public ByteBufferMapping(VariablePath pathToBytes, VariablePath path, int location) {
    this.pathToBytes = pathToBytes;
    this.path = path;
    this.location = location;
  }
  
  public ByteBufferMapping(int numberOfBytes, VariablePath path, int location) {
    this.numberOfBytes = numberOfBytes;
    this.location = location;
    this.path = path;
  }

  public void apply(Program program, Map<String, Object> context, Object value) {
    Object o = path.get(context);
    int numBytes = this.getNumberOfBytes(program);
    
    if (o instanceof ByteBuffer) {
      ByteBuffer bbm = (ByteBuffer) o;
      if (value instanceof Integer && numBytes == Integer.BYTES) {
        IntBuffer asIntBuffer = bbm.asIntBuffer();
        asIntBuffer.position(location/Integer.BYTES);
        asIntBuffer.put((Integer) value);
      }else if(value instanceof Integer && numBytes < Integer.BYTES) {
        int val = (Integer)value;
        byte temp[] = new byte[numBytes];
        for(int i = 0; i < temp.length; ++i) {
          temp[i] = (byte)(val & 0xFF);
          val = val>>8; //shift a whole byte to the right
        }
        bbm.position(this.location);
        bbm.put(temp);
      }
    }
  }
  
  public VariablePath getPath() {
    return path;
  }

  public int getLocation() {
    return this.location;
  }

  public int getNumberOfBytes(Program program) {
    int numBytes = this.numberOfBytes;
    if(pathToBytes != null) {
      numBytes = ((Number)program.getObject(pathToBytes.getFullyQualifiedName())).intValue();
      this.numberOfBytes = numBytes;
    }
    return numBytes;
  }
}
