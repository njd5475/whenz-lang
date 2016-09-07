package com.anor.roar.whenzint.parser;

import com.anor.roar.whenzint.Program;

public class ProgramBuilder implements NodeVisitor {

  private Node    root;
  private Program program;

  public ProgramBuilder(Node root) {
    this.root = root;
    this.program = new Program();
  }

  public Program build() {
    convertTree();
    return program;
  }

  private void convertTree() {
    root.traverse(this);
  }

  @Override
  public void visit(Node node) {
    
  }

}
