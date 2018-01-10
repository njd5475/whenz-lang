package com.anor.roar.whenzint.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.actions.CallSetterMethod;
import com.anor.roar.whenzint.actions.ExitAction;
import com.anor.roar.whenzint.actions.IncrementAction;
import com.anor.roar.whenzint.actions.LaunchWindowAction;
import com.anor.roar.whenzint.actions.NewByteBuffer;
import com.anor.roar.whenzint.actions.PrintAction;
import com.anor.roar.whenzint.actions.PrintVarAction;
import com.anor.roar.whenzint.actions.RunShellCommand;
import com.anor.roar.whenzint.actions.SetToLiteral;
import com.anor.roar.whenzint.actions.TriggerEventAction;

public class WhenzParser {

  private Set<TokenAction>   definedActions = new HashSet<TokenAction>();
  private Node               top;
  private static WhenzParser instance       = new WhenzParser();

  private WhenzParser() {
    definedActions.add(new NewByteBuffer());
    definedActions.add(new SetToLiteral(null, null));
    definedActions.add(new PrintAction(""));
    definedActions.add(new PrintVarAction(""));
    definedActions.add(new LaunchWindowAction());
    definedActions.add(new TriggerEventAction(""));
    definedActions.add(new ExitAction());
    definedActions.add(new CallSetterMethod("", "", ""));
    definedActions.add(new RunShellCommand(""));
    definedActions.add(new IncrementAction(null));
    new SetToLiteral(null, null);
  }

  public void registerAction(TokenAction action) {
    this.definedActions.add(action);
  }

  public static WhenzParser getInstance() {
    return instance;
  }

  public Node parse(TokenBuffer tokens) throws IOException, WhenzSyntaxError {
    Node root = new Node("root");
    program(root, tokens);
    return root;
  }

  private void program(Node root, TokenBuffer tokens)
      throws IOException, WhenzSyntaxError {
    this.top = root;
    TrackableTokenBuffer ttb = TrackableTokenBuffer.wrap(tokens);
    while (!tokens.isEmpty()) {
      ttb.mark();
      try {
        when(root, ttb);
      } catch (WhenzSyntaxError e) {
        ttb.rewind();
        defineActions(root, ttb, e);
      }
    }
  }

  private void defineActions(Node root, TrackableTokenBuffer ttb,
      WhenzSyntaxError previousError) throws WhenzSyntaxError {
    try {
      if (ttb.peek().is("action")) {
        ttb.take();
      } else {
        if (previousError != null) {
          throw previousError;
        }
        this.unexpectedToken(ttb.peek());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private void when(Node parent, TokenBuffer tokens)
      throws IOException, WhenzSyntaxError {
    Node whenNode = new Node("whenz");
    consumeWhitespace(tokens, true);
    if (tokens.peek().is("when")) {
      tokens.take();
      conditions(whenNode, tokens);
      actions(whenNode, tokens);
    } else if (tokens.peek().is("//")) {
      // consume it all
      while (!tokens.peek().isNewline()) {
        tokens.take();
      }
      tokens.take(); // remove newline
      return; // just skip it
    } else {
      unexpectedToken(tokens.peek());
    }
    parent.add(whenNode);
  }

  private void actions(Node whenNode, TokenBuffer tokens)
      throws IOException, WhenzSyntaxError {
    consumeWhitespace(tokens);

    while (!tokens.isEmpty()
        && (tokens.peek().is("//") || tokens.peek().isWord()
            || tokens.peek().isSymbol("@") || tokens.peek().isSymbol("&"))
        && tokens.peek().isNot("when") && tokens.peek().isNot("action")) {
      Node action = new Node("action");
      if ((tokens.peek().isWord() || tokens.peek().isSymbol("@") || tokens.peek().isSymbol("&")) 
          && tokens.peek().isNot("when")) {
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
        whenNode.add(action);
        consumeWhitespace(tokens, true);
      } else if (tokens.peek().is("//")) {
        // consume it all
        while (!tokens.peek().isNewline()) {
          tokens.take();
        }
        tokens.take(); // remove newline
        consumeWhitespace(tokens);
      }
    }
  }

  public void literals(Node node, TokenBuffer tokens)
      throws IOException, WhenzSyntaxError {
    // numbers,decimals,string literals

    // heres where lambdas are useful, instead of building interfaces
    TokenAction number = (p, t) -> {
      p.consumeWhitespace(t);
      return p.number(t);
    };
    TokenAction decimals = (p, t) -> {
      p.consumeWhitespace(t);
      Node n = new Node("Decimal");
      n.add(p.number(t));
      if (t.peek().isSymbol(".")) {
        t.take();
      }
      n.add(p.number(t));
      return n;
    };
    TokenAction stringLiteral = (p, t) -> {
      p.consumeWhitespace(t);
      Node n = new Node("Literals");
      StringBuilder sb = new StringBuilder();
      while (!t.peek().isNewline()) {
        sb.append(t.take().asString());
      }
      n.add(new Node(sb.toString()));
      return n;
    };
    TokenAction actions[] = new TokenAction[] { number, decimals,
        stringLiteral };
    TrackableTokenBuffer tb = TrackableTokenBuffer.wrapAndMark(tokens);
    Node found = null;
    for (TokenAction a : actions) {
      try {
        // assuming found is not null but if it is an exception will be raised.
        found = a.buildNode(this, tb);
        break; // break when we get a valid node otherwise we should get errors
      } catch (WhenzSyntaxError e) {
        tb.rewind();
      }
    }
    if (found == null) {
      unexpectedToken(tb.peek());
    } else {
      node.add(found);
    }
  }

  private Node number(TokenBuffer t) throws IOException, WhenzSyntaxError {
    Node num = new Node("Number");
    Token numberToken = null;
    if (t.peek().isNumber()) {
      numberToken = t.take();
    } else {
      unexpectedToken(t.peek());
    }
    num.add(new Node(numberToken.asString(), numberToken));
    return num;
  }

  public void globalReference(Node node, TokenBuffer tokens)
      throws IOException, WhenzSyntaxError {
    Node namespace = new Node("Reference");
    if (tokens.peek().isSymbol("@") || tokens.peek().isSymbol("&")) {
      tokens.take();
      Node part = this.identifier(tokens);
      while (part != null) {
        namespace.add(part);
        if (tokens.peek().isSymbol(".")) {
          tokens.take();
          part = this.identifier(tokens);
        } else if (tokens.peek().isNewline()) {
          break;
        } else {
          break;
        }
      }
      node.add(namespace);
    }

  }

  public void assignment(Node node, TokenBuffer tokens)
      throws IOException, WhenzSyntaxError {
    Node assignment = new Node("Assignment");
    consumeWhitespace(tokens);
    if (!tokens.peek().isSymbol("=")) {
      unexpectedToken(tokens.peek());
    }
    tokens.take();
    consumeWhitespace(tokens);
    node.add(assignment);
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
    if (tokens.peek().isWord()) {
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
    while (tokens.peek().isWord() || (tokens.peek().is("."))) {
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
    // TODO: try a couple of different patterns here for conditional expressions
    if (tokens.peek().isWord()) {
      if (tokens.peek().is("define") || tokens.peek().is("event")) {
        Node condChild = new Node("Event", tokens.take());
        consumeWhitespace(tokens);

        while (!(tokens.peek().isNewline() || tokens.peek().is("//"))) {
          condChild.add(identifier(tokens));
          consumeWhitespace(tokens);
        }
        conditions.add(condChild);
        if (tokens.peek().isNewline()) {
          tokens.take();
        } else if (tokens.peek().is("//")) {
          while (!tokens.peek().isNewline()) {
            tokens.take();
          }
          tokens.take();
        }
      } else {
        unexpectedToken(tokens.peek());
      }
    } else if (tokens.peek().isSymbol("@")) {
      globalReference(conditions, tokens);
      consumeWhitespace(tokens);
      conditionalOperand(conditions, tokens);
      consumeWhitespace(tokens);
      literals(conditions, tokens);
      consumeWhitespace(tokens, true);
    } else {
      unexpectedToken(tokens.peek());
    }
    whenNode.add(conditions);
  }

  private void conditionalOperand(Node conditions, TokenBuffer tokens)
      throws IOException, WhenzSyntaxError {
    Node op = new Node("Conditional Operand");
    if (tokens.peek().isSymbol("==")) {
      op.add(new Node("is equal", tokens.take()));
    } else if (tokens.peek().isSymbol(">=")) {
      op.add(new Node("greater equal", tokens.take()));
    } else if (tokens.peek().isSymbol("<=")) {
      op.add(new Node("less equal", tokens.take()));
    } else if (tokens.peek().isSymbol("!=")) {
      op.add(new Node("not equal", tokens.take()));
      // }else if(tokens.peek().isSymbol("&&")) {
      // op.add(new Node("and"));
      // }else if(tokens.peek().isSymbol("||")) {
      // op.add(new Node("or"));
    } else {
      unexpectedToken(tokens.peek());
    }
    conditions.add(op);
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

  public void consumeWhitespace(TokenBuffer tokens, boolean newline)
      throws IOException {
    while (!tokens.isEmpty()) {
      if ((tokens.peek().isWhitespace()
          || (newline && tokens.peek().isNewline()))) {
        Token t = tokens.take();
        if (t == null) {
          break;
        }
      } else {
        break;
      }
    }
  }

  public static Program compileProgram(String filename) throws IOException {
    TokenStreamReader tsr = new TokenStreamReader(
        new BufferedReader(new FileReader(filename), 4096));
    WhenzParser parser = new WhenzParser();

    Node root = null;
    try {
      root = parser.parse(new StreamTokenBuffer(tsr, 4096));
      ProgramBuilder builder = new ProgramBuilder(root);
      return builder.build();
    } catch (WhenzSyntaxError e) {
      e.printStackTrace();
    }
    return null;
  }

  public static Program compileToProgram(String filename, Program prog)
      throws IOException {
    TokenStreamReader tsr = new TokenStreamReader(
        new BufferedReader(new FileReader(filename), 4096));
    WhenzParser parser = new WhenzParser();

    Node root = null;
    try {
      root = parser.parse(new StreamTokenBuffer(tsr, 128));
      ProgramBuilder builder = new ProgramBuilder(root, prog);
      return builder.build();
    } catch (WhenzSyntaxError e) {
      e.printStackTrace();
    }
    return null;
  }

  public static void main(String[] args) throws IOException {
    TokenStreamReader tsr = new TokenStreamReader(
        new FileReader("./scripts/hello.whenz"));
    WhenzParser parser = new WhenzParser();

    Node root = null;
    try {
      root = parser.parse(new StreamTokenBuffer(tsr, 128));
      ProgramBuilder builder = new ProgramBuilder(root);
      Program program = builder.build();
    } catch (WhenzSyntaxError e) {
      e.printStackTrace();
    }
  }

  private boolean isIdentifier(TokenBuffer tb) {
    try {
      if (tb.peek().isUnderscore() || tb.peek().isWord()) {
        List<Token> tokList = new LinkedList<Token>();
        tokList.add(tb.take());
        while (tb.peek().isWord() || tb.peek().isNumber()
            || tb.peek().isUnderscore()) {
          tokList.add(tb.take());
        }
        return true;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  public Node identifier(TokenBuffer tokens) throws WhenzSyntaxError {
    TrackableTokenBuffer tb = TrackableTokenBuffer.wrap(tokens);
    try {
      tb.mark();
      if (!isIdentifier(tb)) {
        throw new WhenzSyntaxError("Expected Identifier", tokens.peek());
      }
      tb.rewind();

      if (tb.peek().isUnderscore() || tb.peek().isWord()) {
        List<Token> tokList = new LinkedList<Token>();
        tokList.add(tb.take());
        while (tb.peek().isWord() || tb.peek().isNumber()
            || tb.peek().isUnderscore()) {
          tokList.add(tb.take());
        }

        StringBuilder sb = new StringBuilder("");
        for (Token t : tokList) {
          sb.append(t.asString());
        }
        Node ident = new Node("Identifier", sb.toString());
        return ident;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

}
