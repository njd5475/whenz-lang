package com.anor.roar.whenzint.parser;

import java.io.File;

public class Token {

	public File getFile() {
		return file;
	}

	public enum TTYPE {
		NUMBER,
		WORD,
		UNDERSCORE,
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
	private File file;

	public Token(char c, File file, int line, int col) {
		if(file == null) {
			throw new NullPointerException("Tokens come from files sometimes");
		}
		token.append(c);
		type = type(c);
		this.line = line;
		this.col = col;
		this.file = file;
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
		}

		if (nextType != type || type == TTYPE.SYMBOL || type == TTYPE.NEWLINE) {
			return new Token(c, file, nLine, nCol);
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
		} else if (c == '_') {
		  return TTYPE.UNDERSCORE;
		} else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
			return TTYPE.WORD;
		} else if (c == ' ' || c == '\t') {
			return TTYPE.WHITESPACE;
		} else if (c == '\n' || c == '\r') {
			return TTYPE.NEWLINE;
		} else if (oneOf(c, '#', '!', '\\', '/', '=', '.', '|', ':', '(', ')', '&', '@', '<', '>')) {
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

	public boolean isWord() {
		return type == TTYPE.WORD;
	}
	
	public boolean isUnderscore() {
	  return type == TTYPE.UNDERSCORE;
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

  public TTYPE getType() {
    return type;
  }

  public void appendTo(StringBuilder builder) {
	builder.append(token);
  }

  public boolean oneOf(String...ops) {
    for(String op : ops) {
      if(token.toString().equals(op)) {
        return true;
      }
    }
    return false;
  }

  public boolean isOperator() {
    return type == TTYPE.OPERATION;
  }

  public boolean isOneOf(String...stopWords) {
    for(String word : stopWords) {
      if(is(word)) {
        return true;
      }
    }
    return false;
  }
}
