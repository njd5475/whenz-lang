package com.anor.roar.whenzint.parser;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.anor.roar.whenzint.actions.ExitAction;
import com.anor.roar.whenzint.actions.LaunchWindowAction;
import com.anor.roar.whenzint.actions.PrintAction;
import com.anor.roar.whenzint.actions.SetCurrentObject;
import com.anor.roar.whenzint.actions.TriggerEventAction;

public class WhenzParser {

  private Set<TokenAction> definedActions = new HashSet<TokenAction>();
  private Node             top;

  public WhenzParser() {
    definedActions.add(new PrintAction(""));
    definedActions.add(new LaunchWindowAction());
    definedActions.add(new SetCurrentObject("", "", ""));
    definedActions.add(new TriggerEventAction(""));
    definedActions.add(new ExitAction());
  }

  public Node parse(TokenBuffer tokens) throws IOException, WhenzSyntaxError {
    Node root = new Node("root");
    program(root, tokens);
    return root;
  }

  private void program(Node root, TokenBuffer tokens)
      throws IOException, WhenzSyntaxError {
    this.top = root;
    while (!tokens.isEmpty()) {
      when(root, tokens);
    }
  }

  private void when(Node parent, TokenBuffer tokens)
      throws IOException, WhenzSyntaxError {
    Node whenNode = new Node("whenz");
    consumeWhitespace(tokens, true);
    consume("when", tokens);
    conditions(whenNode, tokens);
    actions(whenNode, tokens);
    parent.add(whenNode);
  }

  private void actions(Node whenNode, TokenBuffer tokens)
      throws IOException, WhenzSyntaxError {
    consumeWhitespace(tokens);
    while (!tokens.isEmpty() && tokens.peek().isIdentifier()
        && tokens.peek().isNot("when")) {
      Node action = new Node("action");
      if (tokens.peek().isIdentifier() && tokens.peek().isNot("when")) {
        TrackableTokenBuffer tb = TrackableTokenBuffer.wrap(tokens);
        tb.mark();
        WhenzSyntaxError error = null;
        error = classOrMethod(action, tb);
        if (error != null) {
          tb.rewind();
          error = definedAction(action, tb);
        }
        if (error != null) {
          throw error;
        }
        consumeWhitespace(tokens, true);
      }
      whenNode.add(action);
    }
  }

  private WhenzSyntaxError definedAction(Node action, TokenBuffer tokens)
      throws IOException {
    Node defAction = new Node("defined action");
    consumeWhitespace(tokens);
    WhenzSyntaxError error = null;
    TrackableTokenBuffer tb = TrackableTokenBuffer.wrap(tokens);
    tb.mark();
    Node actionNode = null;
    for (TokenAction ta : definedActions) {
      try {
        actionNode = ta.buildNode(this, tb);
        error = null;
        break;
      } catch (WhenzSyntaxError e) {
        tb.rewind();
        error = e;
      }
    }
    defAction.add(actionNode);
    action.add(defAction);
    return error;
  }

  private WhenzSyntaxError classOrMethod(Node action, TokenBuffer tokens)
      throws IOException {
    Node classMethod = new Node("Class & Method");
    className(classMethod, tokens);
    WhenzSyntaxError error = null;
    try {
      consume("#", tokens);
      methodSignature(classMethod, tokens);
      action.add(classMethod);
    } catch (WhenzSyntaxError e) {
      error = e;
    }
    return error;
  }

  private void methodSignature(Node action, TokenBuffer tokens)
      throws IOException, WhenzSyntaxError {
    Node methodSignature = new Node("methodSignature");
    if (tokens.peek().isIdentifier()) {
      methodSignature.add(new Node(tokens.take().asString()));
      while (tokens.peek().isSymbol() && tokens.peek().is(":")) {
        tokens.take();
        if (tokens.peek().isNumber()) {
          methodSignature
              .add(new Node(String.valueOf(tokens.take().asNumber())));
        }
      }
    } else {
      unexpectedToken(tokens.peek());
    }

    action.add(methodSignature);
  }

  private void className(Node action, TokenBuffer tokens) throws IOException {
    Node className = new Node("classname");
    String strClass = "";
    while (tokens.peek().isIdentifier() || (tokens.peek().is("."))) {
      strClass += tokens.take().asString();
    }
    className.add(new Node(strClass));
    action.add(className);
  }

  private void conditions(Node whenNode, TokenBuffer tokens)
      throws IOException, WhenzSyntaxError {
    // one or more identifiers followed by a newline
    consumeWhitespace(tokens);
    Node conditions = new Node("conditions");
    while (tokens.peek().isIdentifier() && !tokens.peek().isNewline()) {
      conditions.add(new Node("identifier", tokens.take()));
      consumeWhitespace(tokens);
    }
    if (!tokens.peek().isNewline()) {
      Token tk = tokens.take();
      unexpectedToken(tk);
    } else {
      tokens.take();
    }
    whenNode.add(conditions);
  }

  private Node consume(String term, TokenBuffer tokens)
      throws IOException, WhenzSyntaxError {
    consumeWhitespace(tokens);
    if (tokens.peek().asString().equals(term)) {
      return new Node(term, tokens.take());
    } else {
      unexpectedToken(tokens.peek());
      return null;
    }
  }

  public void unexpectedToken(Token t) throws WhenzSyntaxError {
    unexpectedToken(top, t);
  }

  public void unexpectedToken(Node subtree, Token t) throws WhenzSyntaxError {
    throw new WhenzSyntaxError("Unexpected token: ", t, t.getLine(),
        t.getChar(), subtree);
  }

  public void consumeWhitespace(TokenBuffer tokens) throws IOException {
    consumeWhitespace(tokens, false);
  }

  private void consumeWhitespace(TokenBuffer tokens, boolean newline)
      throws IOException {
    while(!tokens.isEmpty()) {
      if((tokens.peek().isWhitespace()
          || (newline && tokens.peek().isNewline()))) {
        Token t = tokens.take();
        if(t == null) {
          break;
        }
      }else{
        break;
      }
    }
  }

  public static void main(String[] args) throws IOException {
    TokenStreamReader tsr = new TokenStreamReader(
        new FileReader("./scripts/hello.whenz"));
    WhenzParser parser = new WhenzParser();

    Node root = null;
    try {
      root = parser.parse(new StreamTokenBuffer(tsr, 128));
      System.out.println("Parse completed!");
      System.out.println(root);
    } catch (WhenzSyntaxError e) {
      e.printStackTrace();
    }
  }

}
