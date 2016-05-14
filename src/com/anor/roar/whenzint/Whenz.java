package com.anor.roar.whenzint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
			if("pattern".equals(commandParts[1])) {
				if("as".equals(commandParts[3])) {
					
				}
			}
			break;
		case "set":
			String rest[] = rest(1, commandParts);
			chainAction(new SetCurrentObject(rest[0], resolveObj(concate(rest(1, rest)))));
			break;
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

	private static Object resolveObj(String concate) {
		for(Pattern pattern : patterns) {
			if(pattern.matches(concate)) {
				return pattern.resolve(concate);
			}
		}
		return null;
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
