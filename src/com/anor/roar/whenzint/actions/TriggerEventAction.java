package com.anor.roar.whenzint.actions;

import java.io.IOException;
import java.util.Map;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Event;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.parser.Node;
import com.anor.roar.whenzint.parser.ProgramBuilder;
import com.anor.roar.whenzint.parser.TokenBuffer;
import com.anor.roar.whenzint.parser.WhenzParser;
import com.anor.roar.whenzint.parser.WhenzSyntaxError;

public class TriggerEventAction extends Action {

  private String eventName;
  
  static {
    ProgramBuilder.registerActionBuilder(new TriggerEventAction());
  }
  
  public TriggerEventAction() {
    
  }

  public TriggerEventAction(String eventName) {
    if(eventName == null) {
      throw new NullPointerException("Cannot trigger a null event");
    }
    this.eventName = eventName;
  }

  @Override
  public void perform(Program program, Map<String, Object> context) {
    program.trigger(eventName);
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
    return new TriggerEventAction(node.getChildNamed("Event").getChildNamed("Identifier").getTokenOrValue());
  }

  @Override
  public String getActionNodeName() {
    return "Trigger";
  }

}
