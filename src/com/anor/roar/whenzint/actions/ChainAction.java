package com.anor.roar.whenzint.actions;

import java.io.IOException;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.patterns.Node;
import com.anor.roar.whenzint.patterns.TokenBuffer;
import com.anor.roar.whenzint.patterns.WhenzParser;
import com.anor.roar.whenzint.patterns.WhenzSyntaxError;

public class ChainAction extends Action {

	private Action action;
	private Action next;

	public ChainAction(Action action, Action next) {
		this.action = action;
		this.next = next;
	}

	@Override
	public void perform(Program program) {
		action.perform(program);
		next.perform(program);
	}

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens)
      throws WhenzSyntaxError, IOException {
    // TODO Auto-generated method stub
    return null;
  }

}
