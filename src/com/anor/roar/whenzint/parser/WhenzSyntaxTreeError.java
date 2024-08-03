package com.anor.roar.whenzint.parser;

public class WhenzSyntaxTreeError extends Exception {

	  private Node        tree;
	  private String      message;
	  
	  public WhenzSyntaxTreeError(String message, Node node) {
	    super(String.format("%s:\n%s", message, node.toString()));
	    this.message = String.format("%s:\n%s", message, node.toString());
	  }
	  
	  public String getDefaultFormattedMessage() {
	    return message;
	  }

	  @Override
	  public void printStackTrace() {
	    System.err.println(tree);
	    super.printStackTrace();
	  }

}
