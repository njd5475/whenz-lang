package com.anor.roar.whenzint;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TerminalUtils {
	private static final int DEFAULT_WIDTH = 80;
	private static final int DEFAULT_HEIGHT = 24;
	private static TerminalSizer sizer;
	private static boolean initialized = false;

	interface TerminalSizer {
		public Dimension getSize();
	}

	public static Dimension getTerminalSize() {
		if (!initialized) {
			sizer = () -> {
				try {
					String[] cmd = { "/bin/sh", "-c", "stty size < /dev/tty" };
					Process process = Runtime.getRuntime().exec(cmd);
					process.waitFor();
					BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line = reader.readLine();
					if (line != null) {
						String[] parts = line.split(" ");
						int rows = Integer.parseInt(parts[0]);
						int cols = Integer.parseInt(parts[1]);
						return new Dimension(cols, rows);
					}
				} catch (IOException | NumberFormatException | InterruptedException ex) {
					// Ignore exceptions and fall back to using tput
				}
				return null;
			};
			Dimension size = sizer.getSize();

			if (size == null) {
				sizer = () -> {
					try {
						String[] cmd = { "/bin/sh", "-c", "tput cols; tput lines" };
						Process process = Runtime.getRuntime().exec(cmd);
						process.waitFor();
						BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
						int cols = Integer.parseInt(reader.readLine());
						int rows = Integer.parseInt(reader.readLine());
						return new Dimension(cols, rows);
					} catch (IOException | NumberFormatException | InterruptedException ex) {
						// Ignore exceptions and return null
					}
					return null;
				};

			}
			initialized = true;
		}
		return sizer.getSize();
	}

}
