package com.anor.roar.whenzint.actions;

import java.io.IOException;
import java.util.Map;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Event;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.parser.*;

public class TriggerEventAction extends AbstractAction {

  private final Action[] actions;
  private final String onReference;
  private final String eventName;
  
  static {
    ProgramBuilder.registerActionBuilder(new TriggerEventAction());
  }
  
  public TriggerEventAction() {
    super(CodeLocation.fake);
    this.actions = null;
    this.onReference = null;
    this.eventName = null;
  }

  public TriggerEventAction(CodeLocation location, String eventName) {
    this(location, eventName, null, null);
  }

  public TriggerEventAction(CodeLocation location, String eventName, String onReference, Action[] actions) {
    super(location);
    if(eventName == null) {
      throw new NullPointerException("Cannot trigger a null event");
    }
    this.eventName = eventName;
    this.onReference = onReference;
    this.actions = actions;
  }

  @Override
  public void perform(Program program, Map<String, Object> context) {
    if(onReference != null) {
      Object object = program.getObject(onReference);
      if(object != null) {
        //TODO: throw runtime exception when supported by the program
        //program.throwCatchableException();
        if(object instanceof LaunchModuleAction.ModuleReference) {
          LaunchModuleAction.ModuleReference ref = (LaunchModuleAction.ModuleReference) object;
          ref.triggerEventOnModule(this.eventName, actions);
        }
      }
    }else {
      program.trigger(eventName);
    }
  }

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens)
      throws WhenzSyntaxError, IOException {
    Node triggerEvent = new Node("Trigger");
    parser.consumeWhitespace(tokens);
    if (tokens.peek().is("trigger")) {
      tokens.take();
      parser.consumeWhitespace(tokens);
      if (tokens.peek().is("event")) {
        tokens.take();
        parser.consumeWhitespace(tokens);
        Node ident = parser.identifier(tokens);
        if (ident != null) {
          Node eventName = new Node("Event");
          eventName.add(ident);
          triggerEvent.add(eventName);
          parser.consumeWhitespace(tokens);

          if (tokens.peek().is("on")) {
            tokens.take();
            parser.consumeWhitespace(tokens);
            Node onNode = triggerEvent.addChild("on");
            parser.globalReference(onNode,tokens);
            parser.consumeWhitespace(tokens);
            if(tokens.peek().is("with")) {
              parser.withBlock(tokens, onNode.addChild("WithBlock"));
            }
          }

          if (tokens.peek().isNewline()) {
            tokens.take();
          } else {
            parser.unexpectedToken(tokens);
          }
        } else {
          parser.unexpectedToken(tokens);
        }
      } else {
        parser.unexpectedToken(tokens);
      }
    } else {
      parser.unexpectedToken(tokens);
    }
    return triggerEvent;
  }

  @Override
  public Action buildAction(ProgramBuilder builder, Node node) {
    Action[] actions = null;
    String onReference = null;
    if(node.hasChildNamed("on")) {
      Node onNode = node.getChildNamed("on");
      onReference = onNode.getChildNamed("Reference").getChildNamed("Identifier").getTokenOrValue();
      if(onNode.hasChildNamed("WithBlock")) {
        WithAction with = new WithAction();
        try {
          WithAction withClause = (WithAction) with.buildAction(builder, onNode);
          actions = withClause.getActions();
        } catch (WhenzSyntaxTreeError e) {
          throw new RuntimeException(e);
        }
      }
    }
    return new TriggerEventAction(CodeLocation.toLocation(node), node.getChildNamed("Event").getChildNamed("Identifier").getTokenOrValue(), onReference, actions);
  }

  @Override
  public String getActionNodeName() {
    return "Trigger";
  }

}
