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
  
  public Node[] children() {
    return children.toArray(new Node[children.size()]);
  }

  public void traverse(NodeVisitor visitor) {
    // depth first
    for(Node child : children) {
      child.traverse(visitor);
    }
    visitor.visit(this);
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

  public boolean is(String term) {
    return token != null && token.is(term);
  }

  public String getToken() {
    return token.asString();
  }
  
  public Token getRawToken() {
    return token;
  }

	public String name() {
		return name;
	}
}
