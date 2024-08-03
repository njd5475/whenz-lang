package com.anor.roar.whenzint;

import java.awt.Dimension;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ColoredTextBuilder {
	public enum Code {
		RESET("\u001B[0m"),
		BOLD("\u001B[1m"),
		DIM("\u001B[2m"),
		UNDERLINE("\u001B[4m"),
		BLINK("\u001B[5m"),
		REVERSE("\u001B[7m"),
		HIDDEN("\u001B[8m"),
		FG_BLACK("\u001B[30m"),
		FG_RED("\u001B[31m"),
		FG_GREEN("\u001B[32m"),
		FG_YELLOW("\u001B[33m"),
		FG_BLUE("\u001B[34m"),
		FG_MAGENTA("\u001B[35m"),
		FG_CYAN("\u001B[36m"),
		FG_WHITE("\u001B[37m"),
		BG_BLACK("\u001B[40m"),
		BG_RED("\u001B[41m"),
		BG_GREEN("\u001B[42m"),
		BG_YELLOW("\u001B[43m"),
		BG_BLUE("\u001B[44m"),
		BG_MAGENTA("\u001B[45m"),
		BG_CYAN("\u001B[46m"),
		BG_WHITE("\u001B[47m"),
		CURSOR_UP("\u001B[%dA"),
		CURSOR_DOWN("\u001B[%dB"),
		CURSOR_FORWARD("\u001B[%dC"),
		CURSOR_BACKWARD("\u001B[%dD"),
		CURSOR_POSITION("\u001B[%d;%dH"),
		BEGIN_LINE("\033[0G"),
		ERASE_LINE("\u001B[2K"),
		ERASE_SCREEN_AFTER("\033[0J"),
		ERASE_LINE_BEFORE("\033[1K"),
		ERASE_LINE_AFTER("\033[0K"),
		UP("\033[1A");

		private final String code;

		Code(String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}
	}

	public enum Color {
		BLACK("\033[0;30m"),
		RED("\033[0;31m"),
		GREEN("\033[0;32m"),
		YELLOW("\033[0;33m"),
		BLUE("\033[0;34m"),
		MAGENTA("\033[0;35m"),
		CYAN("\033[0;36m"),
		WHITE("\033[0;37m");

		private final String code;

		Color(String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}
	}

	public static class TextSegment {
		private final String text;
		private final List<Code> codes;
		private final List<Color> colors;

		public TextSegment(String text, List<Code> codes, List<Color> colors) {
			this.text = text;
			this.codes = new ArrayList<Code>(codes);
			this.colors = new ArrayList<Color>(colors);
		}

		public TextSegment(String text, Code code, Color color) {
			this(text, List.of(code), List.of(color));
		}

		public void appendTo(StringBuilder sb) {

			if (colors.size() > 1) {
				// make gradient
				int begin = 0, seqSize = (int) Math.floor(text.length() / (double) colors.size());
				int cI = 0;
				for (int i = 0; i < codes.size(); i++) {
					sb.append(codes.get(i).getCode());
				}
				while (begin + seqSize <= text.length() + seqSize) {
					sb.append(colors.get(cI).getCode());

					sb.append(text.substring(begin, Math.min(begin + seqSize, text.length())));
					sb.append(Code.RESET.getCode());
					begin += seqSize;
					cI = Math.min(cI + 1, colors.size() - 1);
				}

			} else {
				boolean needsReset = false;
				if (colors.size() > 0) {
					sb.append(colors.get(0).getCode());
					needsReset = true;
				}
				if (codes.size() > 0) {
					for (int i = 0; i < codes.size(); i++) {
						sb.append(codes.get(i).getCode());
					}
					needsReset = true;
				}
				sb.append(text);
				if (needsReset) {
					sb.append(ColoredTextBuilder.Code.RESET.getCode());
				}
			}
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			appendTo(sb);
			return sb.toString();
		}
	}

	private final List<TextSegment> segments = new ArrayList<>();
	private List<Code> currentCodes = new ArrayList<>();
	private List<Color> currentColors = new ArrayList<>();

	private ColoredTextBuilder withCode(Code code) {
		currentCodes.add(code);
		return this;
	}

	public ColoredTextBuilder bold() {
		return this.withCode(Code.BOLD);
	}

	public ColoredTextBuilder withColor(Color color) {
		currentColors.add(color);
		return this;
	}

	public ColoredTextBuilder withColors(Color... colors) {
		currentColors.addAll(List.of(colors));
		return this;
	}

	public ColoredTextBuilder text(String text) {
		createSegment(text, currentCodes, currentColors);
		return this;
	}

	public TextSegment createSegment(String text, List<Code> codes, List<Color> colors) {
		TextSegment txt;
		segments.add(txt = new TextSegment(text, codes, colors));
		return txt;
	}

	public ColoredTextBuilder begin() {
		createSegment(Code.BEGIN_LINE.getCode(), List.of(), List.of());
		return this;
	}

	public ColoredTextBuilder up(int lines) {
		createSegment(String.format(Code.CURSOR_UP.getCode(), lines), List.of(), List.of());
		return this;
	}

	public ColoredTextBuilder position(int col, int row) {
		createSegment(String.format(Code.CURSOR_POSITION.getCode(), col, row), List.of(), List.of());
		return this;
	}
	
	public ColoredTextBuilder reset() {
		currentCodes.clear();
		currentColors.clear();
		return this;
	}

	public String build() {
		StringBuilder sb = new StringBuilder();
		for (TextSegment segment : segments) {
			segment.appendTo(sb);
		}
		return sb.toString();
	}

	public ColoredTextBuilder printProgressBar(double progress) {
		final int BAR_WIDTH = 50;

		TextSegment textSegment = new TextSegment("", List.of(), List.of()) {
			public void appendTo(StringBuilder builder) {

				// Calculate the number of characters to fill the progress bar
				int progressChars = (int) (progress * BAR_WIDTH);
				String progressBar = "[" + "#".repeat(progressChars) + " ".repeat(BAR_WIDTH - progressChars) + "]";

				// Move the cursor to the beginning of the line
				
				// Print the progress bar and percentage
				builder.append(String.format("%s %.2f%%", progressBar, progress * 100));
				
				builder.append(Code.RESET.getCode());
			}
		};
		segments.add(textSegment);

		return this;
	}
	
	public static String toHex(String arg) {
	    return String.format("%040x", new BigInteger(1, arg.getBytes(Charset.forName("ASCII"))));
	}

	public static void main(String... args) {
//		ColoredTextBuilder text = new ColoredTextBuilder();
//		System.out.println("Start the test");
//		final AtomicInteger percentage = new AtomicInteger();
//		(new Thread() {
//			public void run() {
//				while (true) {
//
//					float per = Float.intBitsToFloat(percentage.get());
//					
//					if(per >= 1) {
//						break;
//					}
//					
//					Dimension d = TerminalUtils.getTerminalSize();
//					if(d != null) {
//						String msg = text.position((int)(d.getHeight()-1), 0).printProgressBar(per).build();
//						System.out.println(msg);
//					}
//
//					percentage.set(Float.floatToIntBits(per + 0.01f));
//
//					try {
//						this.sleep(100);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
//		}).start();
//		Dimension d = TerminalUtils.getTerminalSize();
//		if (d != null) {
//			System.out.println(String.format("w=%.0f h=%.0f\n", d.getWidth(), d.getHeight()));
//		}

		ColoredTextBuilder ctb = new ColoredTextBuilder();
		String out = ctb.text("you").withColor(Color.RED).text("hello").build();
		System.out.println("Bytes: " + toHex(out).replaceAll("(.{4})", "$1 ").trim());
		byte barry[] = out.getBytes();
		System.out.println("Array " + barry);
		System.out.println("Message: " + out);
	}
}
