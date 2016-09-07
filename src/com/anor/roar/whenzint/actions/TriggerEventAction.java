package com.anor.roar.whenzint.actions;
import java.io.IOException;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Event;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.patterns.Node;
import com.anor.roar.whenzint.patterns.TokenBuffer;
import com.anor.roar.whenzint.patterns.WhenzParser;
import com.anor.roar.whenzint.patterns.WhenzSyntaxError;

public class TriggerEventAction extends Action {

	private String eventName;

	public TriggerEventAction(String eventName) {
		this.eventName = eventName;
	}

	@Override
	public void perform(Program program) {
		program.trigger(eventName);
	}

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens)
      throws WhenzSyntaxError, IOException {
    Node triggerEvent = new Node("Trigger");
    if(tokens.peek().is("trigger")) {
      tokens.take();
      parser.consumeWhitespace(tokens);
      if(tokens.peek().is("event")) {
        tokens.take();
        parser.consumeWhitespace(tokens);
        if(tokens.peek().isIdentifier()) {
          Node eventName = new Node("Event", tokens.take());
          triggerEvent.add(eventName);
          parser.consumeWhitespace(tokens);
          if(tokens.peek().isNewline()) {
            tokens.take();
          }else{
            parser.unexpectedToken(tokens.peek());
          }
        }else{
          parser.unexpectedToken(tokens.peek());
        }
      }else{
        parser.unexpectedToken(tokens.peek());
      }
    }else{
      parser.unexpectedToken(tokens.peek());
    }
    return triggerEvent;
  }

}
