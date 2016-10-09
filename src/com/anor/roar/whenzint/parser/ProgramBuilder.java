package com.anor.roar.whenzint.parser;

import com.anor.roar.whenzint.Condition;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.conditions.EventCondition;

public class ProgramBuilder implements NodeVisitor {

	private Node		root;
	private Program	program;

	public ProgramBuilder(Node root) {
		this.root = root;
		this.program = new Program();
	}

	public Program build() {
		convertTree();
		return program;
	}

	private void convertTree() {
		root.traverse(this);
	}

	@Override
	public void visit(Node node) {
		if ("whenz".equals(node.name())) {
			if ("conditions".equals(node.name())) {
				System.out.println("Visiting conditions");
				if (node.children()[0].is("define")) {
					System.out.println("Found whens define condition");
				} else if (node.children()[0].is("event")) {
					System.out.println("Found whens event");
					Condition condition = new EventCondition(
							node.children()[1].getToken());
					program.add(condition);
				}
			}
		}
	}

}
