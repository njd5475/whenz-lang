package com.anor.roar.whenzint;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class VariablePath {

  private List<String> paths     = new LinkedList<String>();
  private boolean      makePaths = false;

  protected VariablePath(String... pathArray) {
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

    for(String path : paths) {
      Map<String, Object> nextObj = (Map<String, Object>) curObj.get(path);
      if(nextObj == null && makePaths) {
        nextObj = new HashMap<String, Object>();
        curObj.put(path, nextObj);
      }
      curObj = nextObj;
    }
    return curObj;
  }

  public void set(Map<String, Object> context, Object value) {
    Map<String, Object> curObj = context;

    List<String> pathStack = paths;
    String last = null;
    Iterator<String> iter = pathStack.iterator();
    while(iter.hasNext()) {
      String path = iter.next();
      if(iter.hasNext()) {
        Map<String, Object> nextObj = (Map<String, Object>) curObj.get(path);
        if(nextObj == null && makePaths) {
          nextObj = new HashMap<String, Object>();
          curObj.put(path, nextObj);
        }
        curObj = nextObj;
      } else {
        last = path;
        break;
      }
    }

    if(last != null) {
      curObj.put(last, value);
    }
  }
}
