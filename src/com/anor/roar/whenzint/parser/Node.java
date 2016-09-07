package com.anor.roar.whenzint.parser;

import java.util.LinkedList;
import java.util.List;

public class Node {

  private List<Node> children = new LinkedList<Node>();
  private String     name;
  private Token      token;

  public Node() {
    this("Unamed");
  }

  public Node(String name) {
    this(name, null);
  }

  public Node(String name, Token token) {
    this.name = name;
    this.token = token;
  }

  public void add(Node child) {
    children.add(child);
  }

  public String toString() {
    return toString(0);
  }

  private String toString(int tab) {
    StringBuilder builder = new StringBuilder("");
    for(int i = 0; i < tab; ++i) {
      builder.append("  ");
    }
    builder.append("> '" + name + "'");
    if(token != null) {
      builder.append(" Token: " + token.toString());
    }else{
      builder.append("|");
    }
    builder.append('\n');
    
    for(Node node : children) {
      builder.append(node.toString(tab + 1));
    }
    return builder.toString();
  }
}
