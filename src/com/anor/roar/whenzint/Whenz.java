package com.anor.roar.whenzint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import com.anor.roar.whenzint.actions.ChainAction;
import com.anor.roar.whenzint.actions.ExitAction;
import com.anor.roar.whenzint.actions.LaunchWindowAction;
import com.anor.roar.whenzint.actions.PrintAction;
import com.anor.roar.whenzint.actions.SetCurrentObject;
import com.anor.roar.whenzint.actions.TriggerEventAction;
import com.anor.roar.whenzint.conditions.EventCondition;
import com.anor.roar.whenzint.patterns.Pattern;

public class Whenz {

	private static List<Pattern> patterns = new LinkedList<Pattern>();
	public static Program program = new Program();

	public static void main(String... args) {
		for (String arg : args) {
			File f = new File(arg);
			if (f.isFile() && f.exists()) {
				try {
					BufferedReader bis = new BufferedReader(new FileReader(f), 4096);

					StringBuilder source = new StringBuilder("");
					char buffer[] = new char[4096];
					int read;
					while ((read = bis.read(buffer)) != -1) {
						source.append(buffer, 0, read);
					}
					String lines[] = source.toString().split("\n");
					for (String line : lines) {
						execute(line);
					}
					program.trigger("app_starts");
					runProgram();
				} catch (IOException e) {
					System.err.println("Could load '" + f + "' either not a file or does not exist!");
				}
			} else {
				System.out.println("Invalid input argument: " + f);
			}
		}
	}

	private static void runProgram() {
		program.run();
	}

	private static Condition lastCondition;
	private static String currentObjectName;

	private static void execute(String line) {
		String[] commandParts = line.trim().split(" +");
		switch (commandParts[0]) {
		case "when":
			Condition c = conditional(rest(1, commandParts));
			program.add(c);
			lastCondition = c;
			break;
		case "print":
			String[] rst = rest(1, commandParts);
			StringBuilder r = new StringBuilder("");
			for (String s : rst) {
				r.append(s);
				if (s != rst[rst.length - 1]) {
					r.append(' ');
				}
			}
			
			chainAction(new PrintAction(r.toString()));
			break;
		case "define":
			evalDefine(rest(1, commandParts));
			break;
		case "set":
			String rest[] = rest(1, commandParts);
			chainAction(new SetCurrentObject(rest[0], currentObjectName, resolveObj(rest(1, rest))));
			break;
		case "for":
			setCurrentObject(concate(rest(1, commandParts)));			
		case "trigger":
			if ("event".equals(commandParts[1])) {
				String cmd[] = rest(2, commandParts);
				chainAction(new TriggerEventAction(cmd[0]));
			}
			break;
		case "launch":
			if("window".equals(commandParts[1])) {
				chainAction(new LaunchWindowAction());
			}
			break;
		case "exit":
			chainAction(new ExitAction());
			break;
		default:
			break;
		}
	}

	private static Define evalDefine(String[] rest) {
		if("pattern".equals(rest[0])) {
			return evalDefine(new Pattern(), rest(1, rest));
		}else{
			String objName = rest[0];
			Object obj = null;
			if("as".equals(rest[1])) {
				obj = evalJavaObject(rest(2, rest));
				program.setObject(objName, obj);
			}
			java.awt.Dimension d;
			return new Define();
		}
	}
	
	private static Object evalJavaObject(String[] rest) {
		String all = concate(rest);
		String className = all.substring(0, all.indexOf('#'));
		String methodName = all.substring(all.indexOf('#')+1, all.indexOf(':'));
		String[] params = all.substring(all.indexOf(':')+1).split(":");
		
		try {
			Class<?> loadClass = Whenz.class.getClassLoader().loadClass(className);
			List<Class<?>> paramTypes = new LinkedList<Class<?>>();
			List<Object> args = new LinkedList<Object>();
			for(String param : params) {
				try {
					args.add(Integer.parseInt(param));
					paramTypes.add(int.class);
				}catch(NumberFormatException nfe) {
					try {
					args.add(Double.parseDouble(param));
					paramTypes.add(double.class);
					}catch(NumberFormatException nfe1) {
						if("true".equals(param) || "false".equals(param)) {
							paramTypes.add(Boolean.class);
							args.add(Boolean.parseBoolean(param));
						}else{
							paramTypes.add(String.class);
							args.add(param);
						}
					}
				}				
			}
			Class<?> types[] = paramTypes.toArray(new Class<?>[paramTypes.size()]);
			if(methodName.equals(loadClass.getSimpleName())) {
				//ctor
				Constructor<?> constructor = loadClass.getConstructor(types);
				return constructor.newInstance(args.toArray());
			}			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
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

	private static Pattern evalDefine(Pattern p, String[] rest) {
		if(rest.length == 0) {
			throw new IllegalStateException("Parse Error: Missing 'as' in define");
		}
		switch(rest[0]) {
		case "number":
			p = p.appendNumber(rest[1]);
			evalDefine(p, rest(2, rest));
			break;
		case "literal":
			p = p.appendLiteral(rest[1]);
			evalDefine(p, rest(1, rest));
			break;
		case "as":
		default:
			return p;
		}
		return null;
	}

	private static void setCurrentObject(String concate) {
		currentObjectName = concate;
	}

	private static Object resolveObj(String parts[]) {
		String concate = concate(parts);
		Object o = program.getObject(concate);
		if(o != null) {
			return o;
		}
		
		return concate(parts);
	}

	private static void chainAction(Action action) {
		Action a = action;
		if (lastCondition.getAction() != null) {
			a = new ChainAction(lastCondition.getAction(), a);
		}
		lastCondition.setAction(a);
	}

	private static Condition conditional(String[] rest) {
		Condition c = null;
		if ("event".equals(rest[0])) {
			c = new EventCondition(rest[1]);
			if ("once".equals(rest[rest.length - 1])) {
				c.once();
			}
		}
		return c;
	}
	
	private static String concate(String[] parts) {
		StringBuilder builder = new StringBuilder();
		for(String p : parts) {
			builder.append(p);
			builder.append(" ");
		}
		return builder.toString().trim();
	}

	private static String[] rest(int i, String[] commandParts) {
		List<String> rest = new LinkedList<String>();
		for (; i < commandParts.length; ++i) {
			rest.add(commandParts[i]);
		}
		return rest.toArray(new String[rest.size()]);
	}
}
