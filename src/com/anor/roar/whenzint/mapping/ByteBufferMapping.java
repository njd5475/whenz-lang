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
  private VariablePath offsetValue;
  private int          location;

  public ByteBufferMapping(VariablePath pathToBytes, VariablePath path, VariablePath offsetValue, int location) {
    this.pathToBytes = pathToBytes;
    this.path = path;
    this.location = location;
    this.offsetValue = offsetValue;
  }

  public ByteBufferMapping(int numberOfBytes, VariablePath path, VariablePath offsetValue, int location) {
    this.numberOfBytes = numberOfBytes;
    this.location = location;
    this.path = path;
    this.offsetValue = offsetValue;
  }

  public void apply(Program program, Map<String, Object> context, Object value) {
    Object o = path.get(context);
    int numBytes = this.getNumberOfBytes(program);
    int offset = 0;

    if (offsetValue != null) {
      Object offsetObject = offsetValue.get(context);
      if (offsetObject == null) {
        offsetObject = program.getObject(offsetValue.getFullyQualifiedName());
      }
      if (offsetObject != null && offsetObject instanceof Integer) {
        offset = (Integer) offsetObject;
      }
    }

    if (o instanceof ByteBuffer) {
      ByteBuffer bbm = (ByteBuffer) o;
      if (value instanceof Integer && numBytes == Integer.BYTES) {
        IntBuffer asIntBuffer = bbm.asIntBuffer();
        asIntBuffer.position(location / Integer.BYTES + offset / Integer.BYTES);
        asIntBuffer.put((Integer) value);
      } else if (value instanceof Integer && numBytes < Integer.BYTES) {
        int val = (Integer) value;
        byte temp[] = new byte[numBytes];
        for (int i = 0; i < temp.length; ++i) {
          temp[i] = (byte) (val & 0xFF);
          val = val >> 8; // shift a whole byte to the right
        }
        bbm.position(this.location + offset);
        bbm.put(temp);
      } else if (value instanceof String) {
        // TODO; handle wide characters here for unicode support
        byte[] strBytes = ((String) value).getBytes();
        bbm.position(this.location + offset);
        bbm.put(strBytes, 0, Math.min(strBytes.length, numBytes));
      }
    }
  }

  public byte[] getBytes(Program program, Map<String, Object> context) {
    Object o = path.get(context);
    if (o == null) {
      o = program.getObject(path.getFullyQualifiedName());
    }
    int numBytes = this.getNumberOfBytes(program);
    int offset = 0;

    if (offsetValue != null) {
      Object offsetObject = offsetValue.get(context);
      if (offsetObject == null) {
        offsetObject = program.getObject(offsetValue.getFullyQualifiedName());
      }
      if (offsetObject != null && offsetObject instanceof Integer) {
        offset = (Integer) offsetObject;
      }
    }

    if (o instanceof ByteBuffer) {
      ByteBuffer bbm = (ByteBuffer) o;
      bbm.position(this.location + offset);
      byte[] bytes = new byte[numBytes];
      bbm.get(bytes);
      return bytes;
    }
    return null;
  }

  public VariablePath getPath() {
    return path;
  }

  public int getLocation() {
    return this.location;
  }

  public int getNumberOfBytes(Program program) {
    int numBytes = this.numberOfBytes;
    if (pathToBytes != null) {
      numBytes = ((Number) program.getObject(pathToBytes.getFullyQualifiedName())).intValue();
      this.numberOfBytes = numBytes;
    }
    return numBytes;
  }

  public VariablePath getOffsetPath() {
    return offsetValue;
  }
}
