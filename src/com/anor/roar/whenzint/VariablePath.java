package com.anor.roar.whenzint;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class VariablePath {

  private List<String> paths = new LinkedList<String>();
  private boolean makePaths = false;
  
  protected VariablePath(String...pathArray) {
    for(String p : pathArray) {
      this.paths.add(p);
    }
  }
  
  public VariablePath makePaths() {
    makePaths = true;
    return this;
  }

  public VariablePath add(String pathEntry) {
    paths.add(pathEntry);
    return this;
  }
  
  public Object get(Map<String, Object> context) {
    Map<String, Object> curObj = context;
    
    LinkedList<String> pathStack = new LinkedList<String>(paths);
    for(String path : pathStack) {
      Map<String, Object> nextObj = (Map<String, Object>) curObj.get(path);
      if(nextObj == null && makePaths) {
        nextObj = new HashMap<String, Object>();
        curObj.put(path, nextObj);
      }
      curObj = nextObj;
    }
    return curObj;
  }
}
