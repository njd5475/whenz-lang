package com.anor.roar.whenzint.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.actions.ByteBufferMappingAction;
import com.anor.roar.whenzint.actions.CallSetterMethod;
import com.anor.roar.whenzint.actions.ExitAction;
import com.anor.roar.whenzint.actions.IncrementAction;
import com.anor.roar.whenzint.actions.LaunchModuleAction;
import com.anor.roar.whenzint.actions.LaunchWindowAction;
import com.anor.roar.whenzint.actions.NewByteBuffer;
import com.anor.roar.whenzint.actions.PrintAction;
import com.anor.roar.whenzint.actions.PutsAction;
import com.anor.roar.whenzint.actions.ReadFromFileChannel;
import com.anor.roar.whenzint.actions.RunShellCommand;
import com.anor.roar.whenzint.actions.SetStateAction;
import com.anor.roar.whenzint.actions.SetToLiteral;
import com.anor.roar.whenzint.actions.ToJsonAction;
import com.anor.roar.whenzint.actions.TriggerEventAction;
import com.anor.roar.whenzint.actions.WriteVariableToFile;

public class WhenzParser {

  private Set<TokenAction>              definedActions = new LinkedHashSet<>();
  private Node                          top;
  private static WhenzParser            instance       = new WhenzParser();
  private Map<String, Set<TokenAction>> moduleActions  = new HashMap<>();

  private WhenzParser() {
    definedActions.add(new SetToLiteral());
    definedActions.add(new PrintAction(CodeLocation.fake, ""));
    definedActions.add(new PutsAction(CodeLocation.fake, (String) null));
    definedActions.add(new LaunchWindowAction());
    definedActions.add(new LaunchModuleAction());
    definedActions.add(new TriggerEventAction(CodeLocation.fake, ""));
    definedActions.add(new ExitAction(CodeLocation.fake));
    definedActions.add(new CallSetterMethod());
    definedActions.add(new RunShellCommand(CodeLocation.fake, ""));
    definedActions.add(new IncrementAction(CodeLocation.fake, null));
    definedActions.add(new SetStateAction(CodeLocation.fake,null, ""));
    definedActions.add(new ByteBufferMappingAction());
    definedActions.add(new NewByteBuffer());
    definedActions.add(new WriteVariableToFile());
    definedActions.add(new ReadFromFileChannel(CodeLocation.fake, null, null));
    definedActions.add(new ToJsonAction());
  }

  public void registerAction(TokenAction action) {
    this.definedActions.add(action);
  }

  public static WhenzParser getInstance() {
    return instance;
  }

  public Node parse(TokenBuffer tokens) throws IOException, WhenzSyntaxError {
    Node root = new Node("root");
    ignoreShebang(root, tokens);
    program(root, tokens);
    return root;
  }

  private void ignoreShebang(Node root, TokenBuffer tokens) throws IOException {
    if (tokens.peek().isSymbol("#")) {
      tokens.take();
      if (tokens.peek().isSymbol("!")) {
        this.consumeLine(tokens);
      }
    }
  }

  private void consumeLine(TokenBuffer tokens) throws IOException {
    while (!tokens.peek().isNewline()) {
      tokens.take();
    }
    this.consumeWhitespace(tokens, true);
  }

  private void program(Node root, TokenBuffer tokens) throws IOException, WhenzSyntaxError {
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

  private void defineActions(Node root, TrackableTokenBuffer ttb, WhenzSyntaxError previousError)
      throws WhenzSyntaxError {
    try {
      if (ttb.peek().is("action")) {
        ttb.take();
      } else {
        if (previousError != null) {
          throw previousError;
        }
        this.unexpectedToken(ttb);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private void when(Node parent, TokenBuffer tokens) throws IOException, WhenzSyntaxError {
    Node whenNode = new Node("whenz");
    consumeWhitespace(tokens, true);
    if (tokens.peek().is("when")) {
      tokens.take();
      conditions(whenNode, tokens);
      actions(whenNode, tokens);
    } else if (tokens.peek().is("//")) {
      // consume it all
      consumeComment(tokens, whenNode);
      return; // just skip it
    } else {
      unexpectedToken(tokens);
    }
    parent.add(whenNode);
  }

  private void actions(Node whenNode, TokenBuffer tokens) throws IOException, WhenzSyntaxError {
    consumeWhitespace(tokens);

    while (!tokens.isEmpty() && (tokens.peek().is("//") || tokens.peek().isWord() || tokens.peek().isSymbol("@")
        || tokens.peek().isSymbol("&")) && tokens.peek().isNot("when") && tokens.peek().isNot("action")) {
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
        consumeComment(tokens, action);
        whenNode.add(action);
      }
    }
  }

  public void consumeComment(TokenBuffer tokens, Node action) throws IOException, WhenzSyntaxError {
    if(tokens.peek().is("//")) {
      StringBuilder commentString = new StringBuilder();
      while(!tokens.peek().isNewline()) {
        tokens.take().appendTo(commentString);
      }
      action.addChild("Comment", commentString);
      consumeWhitespace(tokens, true);
    }else{
      throw new WhenzSyntaxError("Expected Comment // here", tokens);
    }
  }

  public void literals(Node node, TokenBuffer tokens, String ...stopWords) throws IOException, WhenzSyntaxError {
    // numbers,decimals,string literals

    // heres where lambdas are useful, instead of building interfaces
    TokenAction number = (p, t) -> {
      p.consumeWhitespace(t);
      return p.signedNumber(t);
    };
    TokenAction hex = (p, t) -> {
      p.consumeWhitespace(tokens);
      return p.hexidecimal(t);
    };
    TokenAction decimals = (p, t) -> {
      p.consumeWhitespace(t);
      Node n = new Node("Decimal");
      n.add(p.signedNumber(t));
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
      while (!t.peek().isNewline() && !t.peek().isOneOf("do", "once") && !t.peek().isOneOf(stopWords)) {
        sb.append(t.take().asString());
      }
      String literal = sb.toString();
      if (t.peek().isOneOf(stopWords) || t.peek().isOneOf("once", "do")) {
        literal = literal.trim();
      }
      n.add(new Node("Part", literal));
      return n;
    };
    TokenAction actions[] = new TokenAction[] { hex, decimals, number, stringLiteral };
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
      unexpectedToken(tb);
    } else {
      node.add(found);
    }
  }

  private Node signedNumber(TokenBuffer t) throws IOException, WhenzSyntaxError {
    Node num = new Node("Number");
    Token numberToken = null;
    Token signToken = null;
    if (t.peek().isNumber()) {
      numberToken = t.take();
    } else if (t.peek().is("-") || t.peek().is("+")) {
      signToken = t.take();
      if (t.peek().isNumber()) {
        numberToken = t.take();
      } else {
        unexpectedToken(t);
      }
    } else {
      unexpectedToken(t);
    }
    num.addChild(numberToken.asString(), numberToken);
    if (signToken != null) {
      num.addChild("Sign", signToken);
    }
    return num;
  }

  private Node hexidecimal(TokenBuffer t) throws IOException, WhenzSyntaxError {
    Node hex = new Node("HexLiteral");
    this.consumeWhitespace(t);
    if (t.peek().is("0")) {
      t.take();
      if (t.peek().is("x") || (t.peek().isWord() && t.peek().asString().startsWith("x"))) {
        t.take();
        StringBuilder sb = new StringBuilder("");
        while (t.peek().isNumber() || t.peek().isWord()) {
          sb.append(t.take().asString());
        }
        try {
          String hexStr = sb.toString();
          int number = Integer.parseInt(hexStr, 16);
          hex.add(new Node("HexLiteral", hexStr));
        } catch (NumberFormatException nfe) {
          this.unexpectedToken(t);
        }
      } else {
        this.unexpectedToken(t);
      }
    } else {
      this.unexpectedToken(t);
    }
    return hex;
  }

  private Node number(TokenBuffer t) throws IOException, WhenzSyntaxError {
    Node num = new Node("Number");
    Token numberToken = null;
    if (t.peek().isNumber()) {
      numberToken = t.take();
    } else {
      unexpectedToken(t);
    }
    num.add(new Node(numberToken.asString(), numberToken));
    return num;
  }

  public Node globalReference(Node node, TokenBuffer tokens) throws IOException, WhenzSyntaxError {
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
    } else {
      this.unexpectedToken(tokens);
    }
    return namespace;
  }

  public void assignment(Node node, TokenBuffer tokens) throws IOException, WhenzSyntaxError {
    Node assignment = new Node("Assignment");
    consumeWhitespace(tokens);
    if (!tokens.peek().isSymbol("=")) {
      unexpectedToken(tokens);
    }
    tokens.take();
    consumeWhitespace(tokens);
    node.add(assignment);
  }

  private WhenzSyntaxError definedAction(Node action, TokenBuffer tokens) throws IOException {
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
    if (actionNode != null) {
      defAction.add(actionNode);
      action.add(defAction);
    }
    return error;
  }

  private WhenzSyntaxError classOrMethod(Node action, TokenBuffer tokens) throws IOException {
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

  private void methodSignature(Node action, TokenBuffer tokens) throws IOException, WhenzSyntaxError {
    Node methodSignature = new Node("methodSignature");
    if (tokens.peek().isWord()) {
      methodSignature.add(new Node(tokens.take().asString()));
      while (tokens.peek().isSymbol() && tokens.peek().is(":")) {
        tokens.take();
        if (tokens.peek().isNumber()) {
          methodSignature.add(new Node(String.valueOf(tokens.take().asNumber())));
        }
      }
    } else {
      unexpectedToken(tokens);
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

  private void conditions(Node whenNode, TokenBuffer tokens) throws IOException, WhenzSyntaxError {
    // one or more identifiers followed by a newline
    consumeWhitespace(tokens);
    Node conditions = new Node("conditions");
    // TODO: try a couple of different patterns here for conditional expressions
    if (tokens.peek().isWord()) {
      if (tokens.peek().is("define") || tokens.peek().is("event")) {
        Node condChild = new Node("Event", tokens.take());
        consumeWhitespace(tokens);

        while (!(tokens.peek().isNewline() || tokens.peek().is("//") || tokens.peek().is("do"))) {
          condChild.add(identifier(tokens));
          consumeWhitespace(tokens);
        }
        if (tokens.peek().is("do")) {
          tokens.take();
        }
        consumeWhitespace(tokens);
        if (tokens.peek().is("once")) {
          tokens.take();
        }
        conditions.add(condChild);

        if (tokens.peek().isNewline()) {
          consumeWhitespace(tokens, true);
        } else if (tokens.peek().is("//")) {
          consumeComment(tokens, condChild);
        }
      } else {
        unexpectedToken(tokens);
      }
    } else if (tokens.peek().isSymbol("@")) {
      TrackableTokenBuffer tb = TrackableTokenBuffer.wrap(tokens);
      try {
        tb.mark();
        globalReference(conditions, tb);
        consumeWhitespace(tb);
        conditionalOperand(conditions, tb);
        consumeWhitespace(tb);
        literals(conditions, tb, "and", "or");
      } catch (WhenzSyntaxError e) {
        tb.rewind();
        conditions.removeAll();
        stateCondition(conditions, tb);
      }
      consumeWhitespace(tokens);
      if (tokens.peek().is("and")) {
        tokens.take();
        Node andNode = new Node("and");
        whenNode.add(conditions);
        whenNode.add(andNode);
        this.conditions(whenNode, tokens);
        return;
      }
      if (tokens.peek().is("or")) {
        tokens.take();
        Node orNode = new Node("or");
        whenNode.add(conditions);
        whenNode.add(orNode);
        this.conditions(whenNode, tokens);
        return;
      }

      // do once block
      if (tokens.peek().is("do")) {
        tokens.take();
        consumeWhitespace(tokens);
      } else {
        unexpectedToken(tokens);
      }
      consumeWhitespace(tokens);
      if (tokens.peek().is("once")) {
        tokens.take();
        conditions.addChild("once");
      }
      consumeWhitespace(tokens, true);
    } else {
      unexpectedToken(tokens);
    }
    whenNode.add(conditions);
  }

  private void stateCondition(Node parent, TokenBuffer tokens) throws IOException, WhenzSyntaxError {
    Node child = new Node("StateCondition");
    globalReference(child, tokens);
    consumeWhitespace(tokens);
    if (tokens.peek().is("is")) {
      tokens.take();
      consumeWhitespace(tokens);
      child.add(identifier(tokens));
    } else {
      unexpectedToken(tokens);
    }
    parent.add(child);
  }

  private void conditionalOperand(Node conditions, TokenBuffer tokens) throws IOException, WhenzSyntaxError {
    Node op = new Node("Conditional Operand");
    if (tokens.peek().isSymbol("=")) {
      tokens.take();
      if (tokens.peek().isSymbol("=")) {
        tokens.take();
        op.add(new Node("is equal", "=="));
      } else {
        unexpectedToken(tokens);
      }
    } else if (tokens.peek().isSymbol(">")) {
      tokens.take();
      if (tokens.peek().isSymbol("=")) {
        tokens.take();
        op.add(new Node("greater equal", ">="));
      } else {
        op.add(new Node("greater than", ">"));
      }
    } else if (tokens.peek().isSymbol("<")) {
      tokens.take();
      if (tokens.peek().isSymbol("=")) {
        tokens.take();
        op.add(new Node("less equal", "<="));
      } else {
        op.add(new Node("less than", "<"));
      }
    } else if (tokens.peek().isSymbol("!")) {
      tokens.take();
      if (tokens.peek().isSymbol("=")) {
        tokens.take();
        op.add(new Node("not equal", "!="));
      } else {
        unexpectedToken(tokens);
      }
      // }else if(tokens.peek().isSymbol("&&")) {
      // op.add(new Node("and"));
      // }else if(tokens.peek().isSymbol("||")) {
      // op.add(new Node("or"));
    } else {
      unexpectedToken(tokens);
    }
    conditions.add(op);
  }

  private Node consume(String term, TokenBuffer tokens) throws IOException, WhenzSyntaxError {
    consumeWhitespace(tokens);
    if (tokens.peek().asString().equals(term)) {
      return new Node(term, tokens.take());
    } else {
      unexpectedToken(tokens);
      return null;
    }
  }

  public void unexpectedToken(TokenBuffer t) throws WhenzSyntaxError {
    unexpectedToken(top, t);
  }

  public void unexpectedToken(Node subtree, TokenBuffer t) throws WhenzSyntaxError {
    throw new WhenzSyntaxError("Unexpected token: ", t, subtree);
  }

  public void consumeWhitespace(TokenBuffer tokens) throws IOException {
    consumeWhitespace(tokens, false);
  }

  public void consumeWhitespace(TokenBuffer tokens, boolean newline) throws IOException {
    while (!tokens.isEmpty()) {
      if ((tokens.peek().isWhitespace() || (newline && tokens.peek().isNewline()))) {
        Token t = tokens.take();
        if (t == null) {
          break;
        }
      } else {
        break;
      }
    }
  }

  public static Program compileProgram(String filename) throws IOException, WhenzSyntaxError, WhenzSyntaxTreeError {
    TokenStreamReader tsr = new TokenStreamReader(new File(filename),new BufferedReader(new FileReader(filename), 4096));

    Node root = instance.parse(new StreamTokenBuffer(tsr, 4096));
    ProgramBuilder builder = new ProgramBuilder(root, new File(filename));
    return builder.build();
  }

  public static Program compileToProgram(String filename, Program prog) throws IOException, WhenzSyntaxError, WhenzSyntaxTreeError {
    TokenStreamReader tsr = new TokenStreamReader(new File(filename), new BufferedReader(new FileReader(filename), 4096));

    Node root = instance.parse(new StreamTokenBuffer(tsr, 128));

    boolean optionPrintTree = false;
    if(optionPrintTree) {
      System.out.println(root);
    }

    ProgramBuilder builder = new ProgramBuilder(root, new File(filename), prog);
    return builder.build();
  }
  
	public static Program compileProgram(StringBuilder contents, String filename) throws IOException, WhenzSyntaxError, WhenzSyntaxTreeError {
		TokenStreamReader tsr = new TokenStreamReader(new File(filename), new StringReader(contents.toString()));
	
		Node root = instance.parse(new StreamTokenBuffer(tsr, 4096));
		ProgramBuilder builder = new ProgramBuilder(root, new File(filename));
		return builder.build();
	}
	
	public static Program compileToProgram(StringBuilder contents, String filename, Program prog) throws IOException, WhenzSyntaxError, WhenzSyntaxTreeError {
		TokenStreamReader tsr = new TokenStreamReader(new File(filename), new StringReader(contents.toString()));
	
		Node root = instance.parse(new StreamTokenBuffer(tsr, 4096));
		ProgramBuilder builder = new ProgramBuilder(root, new File(filename), prog);
		return builder.build();
	}


  public static void main(String[] args) throws IOException {
    TokenStreamReader tsr = new TokenStreamReader(new File("./scripts/hello.whenz"),new FileReader("./scripts/hello.whenz"));

    Node root = null;
    try {
      root = instance.parse(new StreamTokenBuffer(tsr, 128));
      ProgramBuilder builder = new ProgramBuilder(root, new File("./scripts/hello.whenz"));
      Program program = builder.build();
    } catch (WhenzSyntaxError | WhenzSyntaxTreeError e) {
      e.printStackTrace();
    }
  }

  private boolean isIdentifier(TokenBuffer tb) {
    try {
      if (tb.peek().isUnderscore() || tb.peek().isWord()) {
        List<Token> tokList = new LinkedList<Token>();
        tokList.add(tb.take());
        while (tb.peek().isWord() || tb.peek().isNumber() || tb.peek().isUnderscore()) {
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
        throw new WhenzSyntaxError("Expected Identifier", tokens);
      }
      tb.rewind();

      if (tb.peek().isUnderscore() || tb.peek().isWord()) {
        List<Token> tokList = new LinkedList<Token>();
        tokList.add(tb.take());
        while (tb.peek().isWord() || tb.peek().isNumber() || tb.peek().isUnderscore()) {
          tokList.add(tb.take());
        }

        StringBuilder sb = new StringBuilder("");
        for (Token t : tokList) {
          sb.append(t.asString());
        }
        Node ident = new Node("Identifier", sb.toString(), tokList.get(0));
        return ident;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void expression(Node assignNode, TokenBuffer tokens) throws IOException, WhenzSyntaxError {
    Node expression = assignNode.addChild("Expression");
    this.consumeWhitespace(tokens);
    TrackableTokenBuffer tb = TrackableTokenBuffer.wrap(tokens);
    tb.mark();
    boolean found = false;
    WhenzSyntaxError error = null;
    if (!found) {
      try {
        globalReference(expression, tb);
        found = true;
      } catch (WhenzSyntaxError e) {
        error = e;
        tb.rewind();
      }
    }
    if (!found) {
      try {
        expressionGroup(expression, tb);
        found = true;
      } catch (WhenzSyntaxError e) {
        error = e;
        tb.rewind();
      }
    }
    if (!found) {
      try {
        literals(expression, tb);
        found = true;
      } catch (WhenzSyntaxError e) {
        error = e;
        tb.rewind();
      }
    }

    if (found) {
      this.consumeWhitespace(tb);
      Token cur = tb.peek();
      if (cur.oneOf("+", "-", "*", "/") && cur.isOperator()) {
        Token token = tb.take();
        expression.addChild("Operator", token);
        this.expression(expression, tb);
      }
    } else {
      throw error;
    }
  }

  private void expressionGroup(Node expression, TrackableTokenBuffer tb) throws IOException, WhenzSyntaxError {
    consumeWhitespace(tb);
    if (tb.peek().isSymbol("(")) {
      tb.take();
      Node group = expression.addChild("ExpGroup");
      expression(group, tb);
      if (tb.peek().isSymbol(")")) {
        tb.take();
      } else {
        unexpectedToken(tb);
      }
    } else {
      unexpectedToken(tb);
    }
  }

}
