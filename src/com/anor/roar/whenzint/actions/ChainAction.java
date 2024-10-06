package com.anor.roar.whenzint.actions;

import java.io.IOException;
import java.util.Map;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.parser.*;

public class ChainAction extends AbstractAction {

	private Action action;
	private Action next;

	public ChainAction(Action action, Action next) {
        super(CodeLocation.toLocation(action.getFile(), action.getLine(), action.getColumn()));
		this.action = action;
		this.next = next;
	}

	@Override
	public void perform(Program program, Map<String, Object> context) {
		action.perform(program, context);
		next.perform(program, context);
	}

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens)
      throws WhenzSyntaxError, IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Action buildAction(ProgramBuilder builder, Node node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getActionNodeName() {
    // TODO Auto-generated method stub
    return null;
  }

}
