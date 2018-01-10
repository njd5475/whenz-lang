package com.anor.roar.whenzint;

import java.io.File;
import java.io.IOException;

import com.anor.roar.whenzint.actions.NewByteBuffer;
import com.anor.roar.whenzint.parser.WhenzParser;

public class Whenz {

  public static Program  program      = null;
  private static boolean pauseOnStart = false;

  public static void main(String... args) {
    if (pauseOnStart) {
      try {
        System.in.read();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }
    if (args.length == 0) {
      System.out.println("No input files to run!");
    } else {
      for (String arg : args) {
        File f = new File(arg);
        if (f.isFile() && f.exists()) {
          try {
            long start = System.currentTimeMillis();
            if (program == null) {
              program = WhenzParser.compileProgram(f.getAbsolutePath());
            } else {
              WhenzParser.compileToProgram(f.getAbsolutePath(), program);
            }
            System.out.println("Compiled " + f.getName() + " in "
                + (System.currentTimeMillis() - start) + "ms");

          } catch (IOException e) {
            System.err.println(
                "Could load '" + f + "' either not a file or does not exist!");
          }
        } else {
          System.out.println("Invalid input argument: " + f);
        }
      }

      program.trigger("app_starts");
      runProgram();
    }
  }

  private static void runProgram() {
    program.run();
  }

}
