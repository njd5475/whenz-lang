package com.anor.roar.whenzint;

import java.io.File;
import java.io.IOException;

import com.anor.roar.whenzint.parser.WhenzParser;

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
