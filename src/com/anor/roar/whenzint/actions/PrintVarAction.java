package com.anor.roar.whenzint.actions;

import java.io.IOException;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.parser.Node;
import com.anor.roar.whenzint.parser.TokenBuffer;
import com.anor.roar.whenzint.parser.WhenzParser;
import com.anor.roar.whenzint.parser.WhenzSyntaxError;

public class PrintVarAction extends Action {

	private String varName;

	public PrintVarAction(String varName) {
		this.varName= varName;
	}

	@Override
	public void perform(Program program) {
		program.getObject(varName);
	}
	
	@Override
	public Node buildNode(WhenzParser parser, TokenBuffer tokens)
			throws WhenzSyntaxError, IOException {
		
		Node printVarAction = new Node("PrintVarAction");
    if (tokens.peek().is("printvar")) {
      tokens.take();
      parser.consumeWhitespace(tokens);
      if(tokens.peek().isSymbol("@")) {
      	tokens.take();
      	Node globalRef = new Node("GlobalVariable");
      	while(tokens.peek().isIdentifier()) {
      		globalRef.add(new Node("part",tokens.take()));
      		if(tokens.peek().isSymbol(".")) {
      			tokens.take();
      		}else if(tokens.peek().isNewline()) {
      			break;
      		}else{
      			parser.unexpectedToken(tokens.peek());
      		}
      	}
      	printVarAction.add(globalRef);
      }
      
      if (tokens.peek().isNewline()) {
        tokens.take(); // consume the newline token
      } else {
        parser.unexpectedToken(tokens.peek());
      }
    } else {
      parser.unexpectedToken(tokens.peek());
    }
    return printVarAction;
	}

}
