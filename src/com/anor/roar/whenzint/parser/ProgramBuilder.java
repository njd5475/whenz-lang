
package com.anor.roar.whenzint.parser;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Condition;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.VariablePath;
import com.anor.roar.whenzint.Whenz;
import com.anor.roar.whenzint.actions.ChainAction;
import com.anor.roar.whenzint.conditions.AndConditionGroup;
import com.anor.roar.whenzint.conditions.BoolCondition;
import com.anor.roar.whenzint.conditions.EventCondition;
import com.anor.roar.whenzint.conditions.OrConditionGroup;
import com.anor.roar.whenzint.conditions.StateCondition;
import com.anor.roar.whenzint.expressions.Expression;
import com.anor.roar.whenzint.mapping.ByteBufferMapping;

public class ProgramBuilder implements NodeVisitor {

	private Node root;
	private Program program;

	private Map<String, VariablePath> paths = new HashMap<>();
	private Map<String, ByteBufferMapping> mappings = new HashMap<>();
	private File currentFile;
	private static Map<String, ActionBuilder> actions = new HashMap<>();

	public ProgramBuilder(Node root, File currentFile) {
		this.root = root;
		this.program = new Program();
		if (currentFile == null) {
			throw new NullPointerException("Programs are built from files, silly");
		}
		this.currentFile = currentFile;
	}

	public ProgramBuilder(Node root2, File currentFile, Program prog) {
		this.root = root2;
		this.program = prog;
		if (currentFile == null) {
			throw new NullPointerException("Programs are built from files, silly");
		}
		this.currentFile = currentFile;
	}

	public File getCurrentFile() {
		return currentFile;
	}

	public File getCurrentDirectory() {
		return currentFile.getParentFile().getAbsoluteFile();
	}

	public Program build() throws WhenzSyntaxTreeError {
		convertTree();
		return program;
	}

	private void convertTree() throws WhenzSyntaxTreeError {
		root.traverse(this);
	}

	public static void registerActionBuilder(ActionBuilder builder) {
		actions.put(builder.getActionNodeName(), builder);
	}

	enum Op {
		AND, OR
	};

	@SuppressWarnings("unchecked")
	@Override
	public void visit(Node node) throws WhenzSyntaxTreeError {
		if ("whenz".equals(node.name())) {
			@SuppressWarnings("rawtypes")
			Stack conds = new Stack();
			Action lastAction = null;
			String defining = null;
			for (Node child : node.children()) {
				if ("and".equals(child.name())) {
					Object last = conds.pop();
					conds.push(Op.AND);
					conds.push(last);
				} else if ("or".equals(child.name())) {
					Object last = conds.pop();
					conds.push(Op.OR);
					conds.push(last);
				} else if ("conditions".equals(child.name())) {
					if (child.children()[0].is("define")) {
						Node defChild = child.children()[0].children()[0];
						defining = defChild.getTokenOrValue();
					} else if (child.children()[0].is("event")) {
						conds.push(new EventCondition(child.children()[0].children()[0].getTokenOrValue()));
					} else if ("Reference".equals(child.children()[0].name())) {
						Node referenceNode = child.children()[0];
						String ref = referenceString(referenceNode.children());
						String op = child.children()[1].children()[0].getTokenOrValue();
						Node rightVal = child.children()[2];
						boolean repeats = !child.hasChildNamed("once");

						Condition cond = null;
						if (rightVal.isNamed("Number")) {
							Node rightValChild = rightVal.children()[0];
							if (rightValChild.getRawToken().isNumber()) {
								int num = Integer.parseInt(rightValChild.name());
								cond = new BoolCondition(op, ref, num, repeats);
							}
						} else if (rightVal.isNamed("Literals")) {
							Node rightValChild = rightVal.children()[0];
							cond = new BoolCondition(op, ref, rightValChild.getTokenOrValue(), repeats);
						}

						if (cond != null) {
							conds.push(cond);
						}

						// program.setListener(ref, cond);
					} else if ("StateCondition".equals(child.children()[0].name())) {
						Node stateNode = child.children()[0];
						VariablePath path = this.getPath(stateNode.getChildNamed("Reference"));
						String stateName = stateNode.getChildNamed("Identifier").getTokenOrValue();
						Condition stateCond = new StateCondition(path.getFullyQualifiedName(), stateName);
						// TODO: state conditions need initialization
						stateCond.check(program);
						conds.push(stateCond);
					} else {
						System.out.println("Unhandled condition: " + child);
					}
				} else if ("action".equals(child.name())) {
					Node actionNode = child.children()[0];
					if ("defined action".equals(actionNode.name())) {
						// TODO: defer processing these to each action class
						Node definedActionNode = actionNode.children()[0];
						ActionBuilder builder = actions.get(definedActionNode.name());
						if (builder == null) {
							System.out.println("NO Action Builder: " + definedActionNode);
						}
						Action a = builder.buildAction(this, definedActionNode);

						if (a != null) {
							if (lastAction == null) {
								lastAction = a;
							} else {
								lastAction = new ChainAction(lastAction, a);
							}
						} else {
							System.out.format("WARNING: Action builder '%s' failed to build action\n",
									String.valueOf(a));
							System.out.format("%s\n", actionNode.toString());
						}
					} else if ("GlobalReference".equals(actionNode.name())) {
						ActionBuilder builder = actions.get(actionNode.name());
						Action a = builder.buildAction(this, actionNode);

						if (a != null) {
							if (lastAction == null) {
								lastAction = a;
							} else {
								lastAction = new ChainAction(lastAction, a);
							}
						}
					} else if ("Class & Method".equals(actionNode.name())) {
						String className = actionNode.children()[0].children()[0].name();
						String methodName = actionNode.children()[1].children()[0].name();
						List<String> params = new LinkedList<String>();
						int first = 0;
						for (Node param : actionNode.children()[1].children()) {
							if (first == 0) {
								++first;
								continue;
							}

							params.add(param.name());
						}
						String parms[] = params.toArray(new String[params.size()]);
						Object objectToSet = instanceObject(className, methodName, parms);
						program.setObject(defining, objectToSet);
					}

					defining = null; // clear what is being defined
				}
			} // done with loop

			// reduce conditions to single group
			if (!conds.empty() && lastAction != null) {
				Collections.reverse(conds);
				Condition c = null;
				Stack<Object> tmp = new Stack<Object>();

				if (conds.size() == 2) {
					System.err.print("Invalid syntax tree only 2 conditionals unexpected");
					throw new WhenzSyntaxTreeError("Invalid syntax tree", node);
				}

				if (conds.size() >= 3) {
					Op operation = null;
					Condition one = null;
					Condition two = null;
					while (!conds.empty() || !tmp.empty()) {
						Object top = conds.pop();
						
						if (operation == null && (top.equals(Op.AND) || top.equals(Op.OR))) {
							operation = (Op) top;
							continue;
						}else if(operation == null && top instanceof Condition) {
							conds.push(top);
							break;
						}
						
						if(one == null && (top instanceof Condition)) {
							one = (Condition) top;
							continue;
						}else if(top instanceof Op) {
							// push to tmp
							tmp.push(operation);
							operation = null;
							
							tmp.push(one);
							one = null;
							
							conds.push(top); // push it back on and start over
							continue;
						}
				
						if(two == null && (top instanceof Condition)) {
							two = (Condition) top;
							// build it
							// push it
							if(operation == Op.AND) {
								conds.push(new AndConditionGroup(one, two));
							}else if(operation == Op.OR) {
								conds.push(new OrConditionGroup(one, two));
							}
							one = null;
							two = null;
							operation = null;
							
							// push all from tmp
							while(!tmp.empty()) {
								conds.push(tmp.pop());
							}
							continue;
						}else if(top instanceof Op) {
							tmp.push(operation);
							operation = null;
							tmp.push(one);
							one = null;
							
							conds.push(top);
							continue;
						}
					}
				}

				if (c == null && conds.size() == 1) {
					c = (Condition) conds.pop();
				}

				if (c != null) {
					c.setAction(lastAction);
					program.add(c);
				}
			}
		}

	}

	public Expression buildExpression(Node node) {
		return null;
	}

	public VariablePath getPath(Node node) throws WhenzSyntaxTreeError {
		if (node == null) {
			return null;
		}
		String ref = referenceString(node.children());
		if (ref != null) {
			VariablePath path = paths.get(ref);
			if (("Reference".equals(node.name()) || "GlobalPath".equals(node.name())) && path == null) {
				paths.put(ref, path = VariablePath.create(ref));
			}
			return path;
		}

		throw new WhenzSyntaxTreeError("Node is missing children expected 'Reference' node got: ", node);
	}

	public String referenceString(Node[] children) {
		StringBuilder quickRef = new StringBuilder();
		Node parts[] = children;
		for (Node n : children) {
			quickRef.append(n.getTokenOrValue());
			if (n != parts[parts.length - 1]) {
				quickRef.append(".");
			}
		}
		return quickRef.toString();
	}

	public Object instanceObject(String className, String methodName, String params[]) {
		try {
			Class<?> loadClass = Whenz.class.getClassLoader().loadClass(className);
			List<Class<?>> paramTypes = new LinkedList<Class<?>>();
			List<Object> args = new LinkedList<Object>();
			for (String param : params) {
				try {
					args.add(Integer.parseInt(param));
					paramTypes.add(int.class);
				} catch (NumberFormatException nfe) {
					try {
						args.add(Double.parseDouble(param));
						paramTypes.add(double.class);
					} catch (NumberFormatException nfe1) {
						if ("true".equals(param) || "false".equals(param)) {
							paramTypes.add(Boolean.class);
							args.add(Boolean.parseBoolean(param));
						} else {
							paramTypes.add(String.class);
							args.add(param);
						}
					}
				}
			}
			Class<?> types[] = paramTypes.toArray(new Class<?>[paramTypes.size()]);
			if (methodName.equals(loadClass.getSimpleName())) {
				// ctor
				Constructor<?> constructor = loadClass.getConstructor(types);
				return constructor.newInstance(args.toArray());
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;
	}

	public Object getObject(String token) {
		return program.getObject(token);
	}

	public void setObject(String name, Object obj) {
		program.setObject(name, obj);
	}

	public void registerMapping(ByteBufferMapping bmm, VariablePath path) {
		this.mappings.put(path.getFullyQualifiedName(), bmm);
	}

	public ByteBufferMapping getMapping(String path) {
		return this.mappings.get(path);
	}

	public double buildDecimal(Node expression) {
		int signBit = 1;
		if (expression.hasChildNamed("Sign")) {
			if ("-".equals(expression.getChildNamed("Sign").getTokenOrValue())) {
				signBit = -1;
			}
		}
		int leftSide = Integer.parseInt(expression.children()[0].children()[0].getTokenOrValue());
		int rightSide = Integer.parseInt(expression.children()[1].children()[0].getTokenOrValue());
		int rightDigits = expression.children()[1].children()[0].getTokenOrValue().length();
		double decimals = signBit * leftSide + (rightSide / (Math.pow(10, rightDigits)));
		return decimals;
	}
}
