package com.anor.roar.whenzint;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import com.anor.roar.whenzint.actions.ChainAction;
import com.anor.roar.whenzint.actions.ExitAction;
import com.anor.roar.whenzint.actions.LaunchWindowAction;
import com.anor.roar.whenzint.actions.PrintAction;
import com.anor.roar.whenzint.actions.SetCurrentObject;
import com.anor.roar.whenzint.actions.TriggerEventAction;
import com.anor.roar.whenzint.conditions.EventCondition;
import com.anor.roar.whenzint.parser.WhenzParser;
import com.anor.roar.whenzint.patterns.Pattern;

public class Whenz {

  public static Program        program  = null;

  public static void main(String... args) {
    for (String arg : args) {
      File f = new File(arg);
      if (f.isFile() && f.exists()) {
        try {
          program = WhenzParser.compileProgram(f.getAbsolutePath());
          program.trigger("app_starts");
          runProgram();
        } catch (IOException e) {
          System.err.println(
              "Could load '" + f + "' either not a file or does not exist!");
        }
      } else {
        System.out.println("Invalid input argument: " + f);
      }
    }
  }

  private static void runProgram() {
    program.run();
  }

}
