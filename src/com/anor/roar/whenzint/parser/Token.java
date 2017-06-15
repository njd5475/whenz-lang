package com.anor.roar.whenzint.parser;

public class Token {

	public enum TTYPE {
		NUMBER,
		IDENTIFIER,
		OPERATION,
		QUOTE,
		WHITESPACE,
		NEWLINE,
		SYMBOL,
		UNKNOWN,
		EOF
	};

	private TTYPE					type;
	private StringBuilder	token	= new StringBuilder("");
	private int						line	= 1;
	private int						col		= 0;

	public Token(char c, int line, int col) {
		token.append(c);
		type = type(c);
		this.line = line;
		this.col = col;
	}

	private Token(TTYPE type, int line, int col) {
		this.line = line;
		this.col = col;
		this.type = type;
	}

	public Token next(char c) {
		TTYPE nextType = type(c);
		if (type == null) {
			type = nextType;
		}

		int nLine = line;
		int nCol = col + token.length();
		if (nextType == TTYPE.NEWLINE) {
			++nLine;
			nCol = 0;
			System.out.println("Line: " + nLine);
		}

		if (nextType != type || type == TTYPE.NEWLINE) {
			return new Token(c, nLine, nCol);
		} else {
			this.token.append(c);
		}

		return this;
	}

	public TTYPE type(char c) {
		if (c >= '0' && c <= '9') {
			return TTYPE.NUMBER;
		} else if (c == '+' || c == '-' || c == '/' || c == '*') {
			return TTYPE.OPERATION;
		} else if (c == '"' || c == '\'') {
			return TTYPE.QUOTE;
		} else if (c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
			return TTYPE.IDENTIFIER;
		} else if (c == ' ' || c == '\t') {
			return TTYPE.WHITESPACE;
		} else if (c == '\n') {
			return TTYPE.NEWLINE;
		} else if (oneOf(c, '=', '.', '|', ':', '(', ')', '&', '@')) {
			return TTYPE.SYMBOL;
		}
		return TTYPE.UNKNOWN;
	}

	private boolean oneOf(char test, char... chars) {
		for (char ch : chars) {
			if (ch == test) {
				return true;
			}
		}
		return false;
	}

	private boolean not(TTYPE t) {
		return type != null && (type != t);
	}

	public String toString() {
		return String.format("T[tok='%s',type=%s,line=%d,col=%d",
				token.toString().replaceAll("[\n\r]", "<NL>"), type.name(), line, col);
	}

	public String asString() {
		return token.toString();
	}

	public boolean isWhitespace() {
		return type == TTYPE.WHITESPACE;
	}

	public boolean isNewline() {
		return type == TTYPE.NEWLINE || type == TTYPE.EOF;
	}

	public boolean isIdentifier() {
		return type == TTYPE.IDENTIFIER;
	}

	public boolean isNumber() {
		return type == TTYPE.NUMBER;
	}

	public boolean isNot(String term) {
		return !token.toString().equals(term);
	}

	public boolean isSymbol() {
		return type == TTYPE.SYMBOL;
	}

	public boolean is(String term) {
		return token.toString().equals(term);
	}

	public int getChar() {
		return col;
	}

	public int getLine() {
		return line;
	}

	public int asNumber() {
		return Integer.parseInt(token.toString());
	}

	public static Token eof(int line, int col) {
		return new Token(TTYPE.EOF, line, col);
	}

	public boolean isSymbol(String symbol) {
		return isSymbol() && is(symbol);
	}
}
