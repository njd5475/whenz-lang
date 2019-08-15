package com.anor.roar.whenzint;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.anor.roar.whenzint.parser.WhenzParser;

public class Whenz {

	public static Program program = null;
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
			List<File> files = new LinkedList<>();
			for (String arg : args) {
				File f = new File(arg);
				if (f.isFile() && f.exists()) {
					files.add(f);
				}
			}
			program = loadFromFiles(files.toArray(new File[files.size()]));

			program.trigger("app_starts");

			program.loadJavaProperties();
			
			program.run();
		}
	}

	public static Program loadFromFiles(File... files) {
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
					System.out
							.println("Compiled " + file.getName() + " in " + (System.currentTimeMillis() - start) + "ms");
				}
			} catch (IOException e) {
				System.err.println("Could load '" + file + "' either not a file or does not exist!");
			}

		}
		return program;
	}
}
