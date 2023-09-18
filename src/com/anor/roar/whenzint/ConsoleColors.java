package com.anor.roar.whenzint;

public enum ConsoleColors {

	// Define enum values for different colors
	RESET("\u001B[0m"),
	BLACK("\u001B[30m"),
	RED("\u001B[31m"),
	GREEN("\u001B[32m"),
	YELLOW("\u001B[33m"),
	BLUE("\u001B[34m"),
	PURPLE("\u001B[35m"),
	CYAN("\u001B[36m"),
	WHITE("\u001B[37m");

	private final String code;

	ConsoleColors(String code) {
		this.code = code;
	}

	public String getCode() {
		return this.code;
	}

	@Override
	public String toString() {
		return code;
	}

	public static void colorText(String text, ConsoleColors color) {
		System.out.println(color + text + ConsoleColors.RESET);
	}
}
