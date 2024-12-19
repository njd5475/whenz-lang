package com.anor.roar.whenzint.actions;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.parser.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WithAction extends AbstractAction {

    private final Action[] actions;

    public WithAction() {
        super(CodeLocation.fake);
        actions = null;
    }

    public WithAction(CodeLocation location, Action[] actions) {
        super(location);
        this.actions = actions;
    }

    public Action[] getActions() {
        return this.actions;
    }

    @Override
    public void perform(Program program, Map<String, Object> context) {
        Map<String, Object> withContext = new HashMap<>();
        for(Action a : actions) {
            a.perform(program, withContext);
        }
    }

    @Override
    public Action buildAction(ProgramBuilder builder, Node node) throws WhenzSyntaxTreeError {
        LinkedList<Action> actions = new LinkedList<>();
        if(node.hasChildNamed("WithBlock")) {
            Node withNode = node.getChildNamed("WithBlock");
            SetToLiteral literals = new SetToLiteral();
            for(Node child : withNode.children()) {
                if(child.isNamed("Assignment")) {
                    Action action = literals.buildAction(builder, child);
                    actions.add(action);
                }
            }
        }
        return new WithAction(CodeLocation.toLocation(node), actions.toArray(new Action[actions.size()]));
    }

    @Override
    public String getActionNodeName() {
        return "WithBlock";
    }

    @Override
    public Node buildNode(WhenzParser parser, TokenBuffer tokens) throws WhenzSyntaxError, IOException {
        return parser.withBlock(tokens, new Node("WithBlock"));
    }
}
