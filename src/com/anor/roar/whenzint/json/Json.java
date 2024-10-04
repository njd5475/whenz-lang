package com.anor.roar.whenzint.json;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public final class Json {

  protected enum Type {
    CLOSE_ARRAY, CLOSE_OBJECT, DIGIT, DOUBLE_QUOTE, KEYVAL_SEPARATOR, NEWLINE, OBJECT_SEPARATOR, OPEN_ARRAY,
    OPEN_OBJECT, PERIOD, SINGLE_QUOTE, UNKNOWN, WHITESPACE, STRING_ESCAPE
  };

  protected static class Token {
    private Type          _type;
    private StringBuilder contents;
    private int           line = 0, col = 0;

    public Token(char ch, int line, int col) {
      _type = type(ch);
      contents = new StringBuilder();
      contents.append(ch);
      this.line = line;
      this.col = col;
    }

    public int getColumn() {
      return col;
    }

    public String getContents() {
      return contents.toString();
    }

    public int getLine() {
      return line;
    }

    public Type getType() {
      return _type;
    }

    public boolean is(Type t) {
      return _type == t;
    }

    public boolean isOneOf(Type... types) {
      for (Type t : types) {
        if (is(t)) {
          return true;
        }
      }
      return false;
    }

    public boolean isWhitespace() {
      return isOneOf(Type.NEWLINE, Type.WHITESPACE);
    }

    public boolean matches(char c) {
      return contents.toString().equals(c);
    }

    public boolean matches(String... any) {
      for (String match : any) {
        if (contents.toString().equals(match)) {
          return true;
        }
      }
      return false;
    }

    public Token next(char ch) {
      Type nextType = type(ch);
      if (_type == null) {
        _type = nextType;
      }

      int nLine = line, nCol = col + 1;
      if (nextType == Type.NEWLINE) {
        ++nLine;
        nCol = 1;
      }

      if (_type != nextType || _type == Type.NEWLINE || _type == Type.DOUBLE_QUOTE || _type == Type.CLOSE_OBJECT
          || _type == Type.OPEN_OBJECT || _type == Type.OPEN_ARRAY || _type == Type.CLOSE_ARRAY) {
        return new Token(ch, nLine, nCol);
      } else {
        contents.append(ch);
      }

      return this;
    }

    public String toString() {
      return String.format("T[c='%s',ln=%d,col=%d,t=%s]", getContents().replaceAll("\n", "\\\\n"), getLine(),
          getColumn(), getType().name());
    }

    public Type type(char ch) {
      if (ch == '{') {
        return Type.OPEN_OBJECT;
      } else if (ch == '}') {
        return Type.CLOSE_OBJECT;
      } else if (ch == '\\') {
        return Type.STRING_ESCAPE;
      } else if (ch == '[') {
        return Type.OPEN_ARRAY;
      } else if (ch == ']') {
        return Type.CLOSE_ARRAY;
      } else if (ch == '"') {
        return Type.DOUBLE_QUOTE;
      } else if (ch == '\'') {
        return Type.SINGLE_QUOTE;
      } else if (ch == ',') {
        return Type.OBJECT_SEPARATOR;
      } else if (ch == ':') {
        return Type.KEYVAL_SEPARATOR;
      } else if (ch == ' ' || ch == '\t') {
        return Type.WHITESPACE;
      } else if (ch >= '0' && ch <= '9') {
        return Type.DIGIT;
      } else if (ch == '.') {
        return Type.PERIOD;
      } else if (ch == '\n' || ch == '\r') {
        return Type.NEWLINE;
      }
      return Type.UNKNOWN;
    }
  }

  public static class TokenError extends Error {
    public TokenError(String err) {
      super(err);
    }

    public TokenError(Token t, Type t2) {
      super("Error: Unexpected token: " + t.toString() + " expected " + t2.name());
    }

    public TokenError(Type expected) {
      super("Error: Unexpected end of stream, expected " + expected.name());
    }
  }

  public static class TokenStreamEmptyError extends Error {
    public TokenStreamEmptyError(String err) {
      super(err);
    }

    public TokenStreamEmptyError(Token t, Type t2) {
      super("Error: Stream ended unxpectedly: " + t.toString() + " expected " + t2.name());
    }

    public TokenStreamEmptyError(Type expected) {
      super("Error: Stream ended unxpectedly, expected " + expected.name());
    }
  }

  private static String concateTill(List<Token> tokens, Type... t) {
    StringBuilder all = new StringBuilder("");
    try {
      while (!tokens.get(0).isOneOf(t)) {
        all.append(tokens.remove(0).getContents());
      }
    } catch (IndexOutOfBoundsException ioobe) {
      // we must have reached the end of the stream so we should return what we have.
    }
    return all.toString();
  }

  private static void expect(List<Token> tokens, Type t) {
    if (!tokens.isEmpty() && tokens.get(0).is(t)) {
      tokens.remove(0);
    } else if (!tokens.isEmpty()) {
      throw new TokenError(tokens.get(0), t);
    } else {
      // the tokens are empty so we expected one but didn't get it
      throw new TokenStreamEmptyError(t);
    }
  }

  private static List<Token> ignoreWhitespace(List<Token> tokens) {
    while (!tokens.isEmpty() && tokens.get(0).isWhitespace()) {
      tokens.remove(0);
    }
    return tokens;
  }

  public static void main(String... args) {
    String strObj = "{\"z\" : [{\"a\":\"\"},{},    { },{}, \'6\'], \" so\\\" \\\"me \":true, \"list\":[9,54,4,true,false,{}]\n, \"val_null\": null}";
    Map<String, Object> jObj = parse(strObj);
    assert (jObj != null);
    Map<String, Object> not = new LinkedHashMap<String, Object>(jObj);
    System.out.println(strObj);
    System.out.println(toJson(not));
    System.out.println("Test ran successfully");

    String strObj2 = "{ \"DATA\": \".sqlfiles\" }";
    jObj = parse(strObj2, (String[] parentKeys, String path, Object value) -> {
      System.out.format("Got key %s, path=%s value=%s \n", parentKeys, path, value);
    });
    System.out.println(toJson(jObj));
  }

  private static Map<String, Object> objectContents(List<Token> tokens, String[] keys, Map<String, Object> obj, JsonKeyListener keyListener) {
    if (tokens.isEmpty()) {
      throw new TokenError("Object contents");
    }
    ignoreWhitespace(tokens);
    try {
      while (!tokens.get(0).is(Type.CLOSE_OBJECT)) {
        objectField(tokens, keys, obj, keyListener);
        ignoreWhitespace(tokens);
        if (!tokens.get(0).is(Type.CLOSE_OBJECT)) {
          expect(tokens, Type.OBJECT_SEPARATOR);
        }
      }
    } catch (IndexOutOfBoundsException ioobe) {
      // we must have reached the end of the stream
      if (!tokens.isEmpty()) {
        ioobe.printStackTrace();
      }
    }
    return obj;
  }

  private static void objectField(List<Token> tokens, String[] keys, Map<String, Object> obj, JsonKeyListener keyListener) {
    ignoreWhitespace(tokens);
    String key = string(tokens, tokens.get(0).getType() == Type.SINGLE_QUOTE);
    List<String> depthKeys = new LinkedList<String>(Arrays.asList(keys));
    depthKeys.add(key);
    ignoreWhitespace(tokens);
    expect(tokens, Type.KEYVAL_SEPARATOR);
    Object value = objectValue(tokens, depthKeys.toArray(new String[depthKeys.size()]), keyListener);
    if (obj.containsKey(key)) {
      throw new TokenError("Duplicate key found");
    }
    if(keyListener != null) {
      keyListener.keyParse(keys, key, value);
    }
    obj.put(key, value);
  }

  private static Object objectValue(List<Token> tokens, String[] keys, JsonKeyListener keyListener) {
    ignoreWhitespace(tokens);
    Object o = null;
    Token top = tokens.get(0);
    if (top.is(Type.DOUBLE_QUOTE)) {
      o = string(tokens, false);
    } else if (top.is(Type.SINGLE_QUOTE)) {
      o = string(tokens, true);
    } else if (top.is(Type.OPEN_OBJECT)) {
      o = parseObject(tokens, keys, keyListener);
    } else if (top.is(Type.OPEN_ARRAY)) {
      o = parseArray(tokens, keys, keyListener);
    } else if (tokens.get(0).matches("true", "false")) {
      o = parseBoolean(tokens);
    } else if (tokens.get(0).matches("null")) {
      tokens.remove(0);
      o = null;
    } else if (tokens.get(0).isOneOf(Type.DIGIT, Type.PERIOD)) {
      o = parseNumber(tokens);
    }
    return o;
  }

  public static String string(List<Token> tokens, boolean singleQuote) {
    Type quote = singleQuote ? Type.SINGLE_QUOTE : Type.DOUBLE_QUOTE;
    expect(tokens, quote);
    StringBuilder builder = new StringBuilder();
    boolean cont = false;
    try {
      do {
        cont = false;
        String all = concateTill(tokens, Type.STRING_ESCAPE, quote);
        builder.append(all);
        if (tokens.get(0).is(Type.STRING_ESCAPE)) {
          tokens.remove(0);
          builder.append('\\');
          builder.append(tokens.remove(0).getContents());
          cont = true;
        }
      } while (cont);
      expect(tokens, quote);
    } catch (IndexOutOfBoundsException ioobe) {
      // we must have reached the end of the stream
    }
    return builder.toString();
  }

  public static Map<String, Object> parse(InputStream is) {
    return parse(new InputStreamReader(is));
  }

  public static Map<String, Object> parse(Reader r) {
    return parse(r, null);
  }

  public static Map<String, Object> parse(Reader r, JsonKeyListener keyListener) {

    List<Token> tokens = new LinkedList<Token>();
    try {
      Token current = new Token((char) r.read(), 1, 1);
      int nextCh = -1;
      Token next;
      while (r.ready() && (nextCh = r.read()) != -1) {
        next = current.next((char) nextCh);
        if (next != current) {
          tokens.add(current);
          current = next;
        }
      }
      tokens.add(current);

      return parseObject(tokens, new String[] {}, keyListener);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return new LinkedHashMap<String, Object>();
  }

  /**
   * While the json is being parsed a queue will be maintained in a separate
   * thread. That queue will push events to the JsonKeyListener when new values
   * are completely parsed.
   * 
   * @param json
   * @param keyListener
   * @return
   */
  public static Map<String, Object> parse(String json, JsonKeyListener keyListener) {
    return parse(new StringReader(json), keyListener);
  }

  public static Map<String, Object> parse(String json) {
    return parse(new StringReader(json), null);
  }

  public static JsonObject parseToWrapper(String json) {
    return new JsonObject(parse(json));
  }

  public static JsonObject parseFileObject(File configFile) {
    try {
      String jsonFile = new String(Files.readAllBytes(configFile.toPath()), Charset.defaultCharset());
      return parseToWrapper(jsonFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static JsonObject parseFileObject(File configFile, String defaultObj) {
    try {
      String jsonFile = new String(Files.readAllBytes(configFile.toPath()), Charset.defaultCharset());
      if (jsonFile.isEmpty() || jsonFile.trim().isEmpty()) {
        jsonFile = defaultObj;
      }
      return parseToWrapper(jsonFile);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (TokenError err) {
      System.err.println("WARNING: " + configFile + " had a syntax error in it replacing with " + defaultObj);
      return parseToWrapper(defaultObj);
    }
    return null;
  }

  private static List<Object> parseArray(List<Token> tokens, String[] keys, JsonKeyListener keyListener) {
    List<Object> array = new LinkedList<Object>();
    expect(tokens, Type.OPEN_ARRAY);
    while (!tokens.get(0).is(Type.CLOSE_ARRAY)) {
      try {
        ignoreWhitespace(tokens);
        Object o = objectValue(tokens, concatenate(keys, new String[] {String.valueOf(array.size())}), keyListener);
        array.add(o);
        ignoreWhitespace(tokens);
        if (!tokens.get(0).is(Type.CLOSE_ARRAY)) {
          expect(tokens, Type.OBJECT_SEPARATOR);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    expect(tokens, Type.CLOSE_ARRAY);
    return array;
  }

  private static Object parseBoolean(List<Token> tokens) {
    if (tokens.get(0).matches("true")) {
      tokens.remove(0);
      return true;
    } else if (tokens.get(0).matches("false")) {
      tokens.remove(0);
      return false;
    } else {
      throw new TokenError("Truly unexpected error");
    }
  }

  private static Object parseNumber(List<Token> tokens) {
    StringBuilder number = new StringBuilder();
    while (tokens.get(0).is(Type.DIGIT)) {
      number.append(tokens.remove(0).getContents());
    }
    if (tokens.get(0).is(Type.PERIOD)) {
      number.append(tokens.remove(0).getContents());
      while (tokens.get(0).is(Type.DIGIT)) {
        number.append(tokens.remove(0).getContents());
      }
      return Double.parseDouble(number.toString());
    } else {
      return Integer.parseInt(number.toString());
    }
  }

  private static Map<String, Object> parseObject(List<Token> tokens, String[] keys, JsonKeyListener keyListener) {
    Map<String, Object> newObj = new LinkedHashMap<String, Object>();
    try {
      ignoreWhitespace(tokens);
      expect(tokens, Type.OPEN_OBJECT);

      newObj = objectContents(tokens, keys, newObj, keyListener);

      ignoreWhitespace(tokens);
      try {
        expect(tokens, Type.CLOSE_OBJECT);
      } catch (IndexOutOfBoundsException | TokenStreamEmptyError ioobe) {
        // we must have reached the end of the stream
      }
      ignoreWhitespace(tokens);
    } catch (TokenError te) {

    }
    return newObj;
  }
  
  private static String[] concatenate(String[] one, String[] two) {
    List<String> listOne = Arrays.asList(one);
    List<String> listTwo = Arrays.asList(two);
    List<String> dynamic = new LinkedList<String>();
    dynamic.addAll(listOne);
    dynamic.addAll(listTwo);
    return dynamic.toArray(new String[dynamic.size()]);
  }

  public static Map<String, Object> parseResource(String resourceName) {
    return parseResource(resourceName, Json.class.getClassLoader());
  }

  public static Map<String, Object> parseResource(String resName, ClassLoader classLoader) {
    return parse(classLoader.getResourceAsStream(resName));
  }

  public static JsonObject parseResourceObject(String resName) {
    return new JsonObject(parseResource(resName));
  }

  public static JsonObject parseResourceObject(String resName, ClassLoader classLoader) {
    return new JsonObject(parseResource(resName, classLoader));
  }

  @SuppressWarnings("unchecked")
  public static String toJson(Map<String, Object> object) {
    StringBuilder toReturn = new StringBuilder("");
    Object comma = new Object();
    // reduce
    if (!object.isEmpty()) {
      Stack<Object> recurse = new Stack<Object>();
      recurse.push(object);
      toReturn.append("{");
      while (!recurse.isEmpty()) {
        Object o = recurse.pop();
        if (o instanceof Map) {
          Map<Object, Object> mp = (Map<Object, Object>) o;
          if (!mp.isEmpty()) {
            Object key = mp.keySet().iterator().next();
            Object val = mp.remove(key);
            toReturn.append(String.format("\"%s\":", key));
            recurse.push(mp);
            if (val instanceof Array || val instanceof String[] || val instanceof int[] || val instanceof float[]
                || val instanceof double[] || val instanceof short[]) {
              val = Arrays.asList((Object[]) val);
            }
            if (val instanceof Iterable) {
              toReturn.append("[");
            } else if (val instanceof Map) {
              toReturn.append("{");
            }
            if (!mp.isEmpty()) {
              recurse.push(comma);
            }
            recurse.push(val);
          } else {
            toReturn.append("}");
          }
        } else if (o instanceof Iterable || o instanceof Array || o instanceof String[] || o instanceof int[]
            || o instanceof float[] || o instanceof double[] || o instanceof short[]) {
          if (o instanceof Array || o instanceof String[] || o instanceof int[] || o instanceof float[]
              || o instanceof double[] || o instanceof short[]) {
            o = Arrays.asList((Object[]) o);
          }
          Iterable<Object> arr = (Iterable<Object>) o;
          if (arr.iterator().hasNext()) {
            List<Object> tmp = new LinkedList<Object>();
            Iterator<Object> iter = arr.iterator();
            Object first = iter.next();
            while (iter.hasNext()) {
              tmp.add(iter.next());
            }

            recurse.push(tmp);
            if (!tmp.isEmpty()) {
              recurse.push(comma);
            }
            recurse.push(first);
            if (first instanceof Map) {
              toReturn.append("{");
            } else if (first instanceof Iterable) {
              toReturn.append("[");
            }
          } else {
            toReturn.append("]");
          }
        } else if (o == comma) {
          toReturn.append(',');
        } else if (o == null) {
          toReturn.append("null");
        } else if (o instanceof Number || o instanceof Boolean) {
          toReturn.append(o.toString());
        } else {
          toReturn.append('"');
          toReturn.append(o.toString());
          toReturn.append('"');
        }
      }
    } else {
      toReturn.append("{}");
    }
    return toReturn.toString();
  }

  protected Json() {
  }

  public static class JsonObject implements Iterable<String> {
    private Map<String, Object> wrap;

    public JsonObject(Map<String, Object> toWrap) {
      this.wrap = toWrap;
    }

    public JsonObject getObject(String key) {
      return new JsonObject((Map<String, Object>) wrap.get(key));
    }

    public JsonObject putObject(String key, JsonObject obj) {
      wrap.put(key, obj.wrap);
      return this;
    }

    public boolean getBoolean(String key) {
      return Boolean.parseBoolean(wrap.get(key).toString());
    }

    public JsonObject putBoolean(String key, boolean value) {
      wrap.put(key, value);
      return this;
    }

    public int getInt(String key) {
      return Integer.parseInt(wrap.get(key).toString());
    }

    public JsonObject putInt(String key, int val) {
      wrap.put(key, val);
      return this;
    }

    public double getDouble(String key) {
      return Double.parseDouble(wrap.get(key).toString());
    }

    public JsonObject putDouble(String key, double value) {
      wrap.put(key, value);
      return this;
    }

    public String getString(String key) {
      return wrap.get(key).toString();
    }

    public JsonObject putString(String key, String value) {
      wrap.put(key, value);
      return this;
    }

    public boolean hasKey(String key) {
      return wrap.containsKey(key);
    }

    public String toJson() {
      return Json.toJson(wrap);
    }

    @Override
    public Iterator<String> iterator() {
      return wrap.keySet().iterator();
    }

    public String[] getStringArray(String key) {
      LinkedList<String> strs = (LinkedList<String>) wrap.get(key);
      return strs.toArray(new String[strs.size()]);
    }

    public JsonObject[] getObjectArray(String key) {
      List<Object> elements = (List<Object>) wrap.get(key);
      List<JsonObject> asJobjs = new LinkedList<JsonObject>();

      for (Object o : elements) {
        asJobjs.add(new JsonObject((Map<String, Object>) o));
      }

      return asJobjs.toArray(new JsonObject[asJobjs.size()]);
    }

    public Object[] getMixedArray(String key) {
      List<Object> elements = (List<Object>) wrap.get(key);
      return elements.toArray(new Object[elements.size()]);
    }

    public void putStringArray(String string, List<String> recents) {
      wrap.put(string, recents);
    }
  }

  public static interface JsonKeyListener {

    public void keyParse(String[] parentKeys, String key, Object value);

  }

}
