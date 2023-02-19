package com.anor.roar.whenzint.parser;

import java.util.LinkedList;
import java.util.List;

public class Node {

  private List<Node> children = new LinkedList<Node>();
  private String     name;
  private Token      token;
  private String     val;

  public Node() {
    this("Unamed");
  }

  public Node(String name) {
    this.name = name;
  }

  public Node(String name, Token token) {
    this.name = name;
    this.token = token;
  }

  public Node(String name, String val) {
    this.name = name;
    this.val = val;
  }

  public void add(Node child) {
    if(child == null) {
      throw new NullPointerException("You cannot add a null child to the tree");
    }
    children.add(child);
  }

  public Node[] children() {
    return children.toArray(new Node[children.size()]);
  }

  public void traverse(NodeVisitor visitor) throws WhenzSyntaxTreeError {
    // depth first
    for (Node child : children) {
      child.traverse(visitor);
    }
    visitor.visit(this);
  }

  public String toString() {
    return toString(0);
  }

  private String toString(int tab) {
    StringBuilder builder = new StringBuilder("");
    for (int i = 0; i < tab; ++i) {
      builder.append("  ");
    }
    builder.append("> '" + name + "'");
    if (token != null) {
      builder.append(" Token: " + token.toString());
    } else if(val != null) {
      builder.append(" Value: " + val);
    } else {
      builder.append("|");
    }
    builder.append('\n');

    for (Node node : children) {
      builder.append(node.toString(tab + 1));
    }
    return builder.toString();
  }

  public boolean isNamed(String name) {
    return this.name.equals(name);
  }

  public boolean is(String term) {
    if(hasToken()) {
      return token.is(term);
    }else if(val != null) {
      return val.equals(term);
    }
    return false;
  }

  public boolean hasToken() {
    return token != null;
  }

  public String getToken() {
    if (hasToken()) {
      return token.asString();
    }
    return null;
  }

  public Token getRawToken() {
    return token;
  }

  public String getValue() {
    return val;
  }

  public String name() {
    return name;
  }

  public boolean hasValue() {
    return val != null;
  }

  public String getTokenOrValue() {
    if(hasToken()) {
      return token.asString();
    }else if(hasValue()) {
      return val;
    }
    return null;
  }

  public boolean hasChildNamed(String childName) {
    for(Node child : children()) {
      if(child.isNamed(childName)) {
        return true;
      }
    }
    return false;
  }

  public Node addChild(String name) {
    Node n = new Node(name);
    this.children.add(n);
    return n;
  }

  public Node getChildNamed(String childName) {
    for(Node child : children) {
      if(child.isNamed(childName)) {
        return child;
      }
    }
    return null;
  }

  public void removeAll() {
    children.clear();
  }

  public Node addChild(String name, Token token) {
    Node child = new Node(name, token);
    this.children.add(child);
    return child;
  }
}
