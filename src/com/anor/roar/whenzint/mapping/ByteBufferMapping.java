package com.anor.roar.whenzint.mapping;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Map;

import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.VariablePath;

public class ByteBufferMapping {

  private int          numberOfBytes;
  private VariablePath path;
  private int          location;

  public ByteBufferMapping(int numberOfBytes, VariablePath path, int location) {
    this.numberOfBytes = numberOfBytes;
    this.location = location;
    this.path = path;
  }

  public void apply(Program program, Map<String, Object> context, Object value) {
    Object o = path.get(context);
    if (o instanceof ByteBuffer) {
      ByteBuffer bbm = (ByteBuffer) o;
      if (value instanceof Integer && this.numberOfBytes == Integer.BYTES) {
        IntBuffer asIntBuffer = bbm.asIntBuffer();
        asIntBuffer.position(location/Integer.BYTES);
        asIntBuffer.put((Integer) value);
      }else if(value instanceof Integer && this.numberOfBytes < Integer.BYTES) {
        int val = (Integer)value;
        byte temp[] = new byte[this.numberOfBytes];
        for(int i = 0; i < temp.length; ++i) {
          temp[i] = (byte)val;
          val = val>>2;
        }
        bbm.position(this.location);
        bbm.put(temp);
      }
    }
  }

  public int getLocation() {
    return this.location;
  }

  public int getNumberOfBytes() {
    return this.numberOfBytes;
  }
}
