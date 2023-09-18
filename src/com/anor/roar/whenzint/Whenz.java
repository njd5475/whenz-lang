package com.anor.roar.whenzint;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.anor.roar.whenzint.parser.WhenzParser;
import com.anor.roar.whenzint.parser.WhenzSyntaxError;
import com.anor.roar.whenzint.parser.WhenzSyntaxTreeError;

public class Whenz {

  // Last number of version are days since epoch in UTC
  private static String                       VERSION      = "v0.0.2";
  public static Program                       program      = null;
  private static boolean                      pauseOnStart = false;

  private static Map<String, ArgumentHandler> commands     = new HashMap<>();
  static {
    commands.put("version", (ArgumentHandler) (arg) -> {
      System.out.println("Whenz Parser version " + VERSION);
      return true;
    });
    commands.put("help", (ArgumentHandler) (arg) -> {
      System.out.println("Help\n\nwhenz [options] [files]");
      return true;
    });
  }

  public static void main(String... args) {
    if (pauseOnStart) {
      try {
        System.in.read();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }

    Pattern m = Pattern.compile("--(.+)");
    Set<Entry<String, ArgumentHandler>> entrySet = commands.entrySet();
    for (String arg : args) {
      if (arg.startsWith("-")) {
        Matcher matcher = m.matcher(arg);
        if (matcher.find()) {
          String matchArg = matcher.group(1);
          ArgumentHandler found = null;
          for (Entry<String, ArgumentHandler> c : entrySet) {
            if (c.getKey().startsWith(matchArg)) {
              found = c.getValue();
            }
          }
          found.handleArgument(matchArg);
        }
      }
    }

    if (args.length == 0) {
      System.out.println("No input files to run!");
    } else {
      List<File> files = new LinkedList<>();
      for (String arg : args) {
        if (!arg.startsWith("-")) {
          File f = new File(arg);
          if (f.isFile() && f.exists()) {
            files.add(f);
          }
        }
      }
      try {
        program = loadFromFiles(files.toArray(new File[files.size()]));

        program.trigger("app_starts");

        program.setObject("whenz.version", VERSION);

        program.loadJavaProperties();

        program.run();
      } catch (WhenzSyntaxError e) {
        System.err.format("%s\n", e.getDefaultFormattedMessage());
      }

    }
  }

  public static Program loadFromFiles(File... files) throws WhenzSyntaxError {
    Program program = new Program();
    for (File file : files) {
      try {
        long start = System.currentTimeMillis();
        if (program == null) {
          program = WhenzParser.compileProgram(file.getAbsolutePath());
        } else {
          WhenzParser.compileToProgram(file.getAbsolutePath(), program);
        }
        if (System.getenv().get("WHENZ_PARSER_VERBOSE") != null) {
          System.out.println("Compiled " + file.getName() + " in " + (System.currentTimeMillis() - start) + "ms");
        }
      } catch (IOException e) {
        System.err.println("Could load '" + file + "' either not a file or does not exist!");
      } catch (WhenzSyntaxTreeError e) {
		e.printStackTrace();
	}

    }
    return program;
  }
}
