package com.anor.roar.whenzint.parser;

import java.io.File;

public class CodeLocation {

    public static CodeLocation fake = new CodeLocation();
    private final File file;
    private final int line;
    private final int column;

    private CodeLocation() {
        file = new File("./");
        line = 0;
        column = 0;
    }

    public CodeLocation(File file, int line, int column) {
        this.file = file;
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public File getFile() {
        return file;
    }

    public static CodeLocation toLocation(File file, int line, int column) {
        return new CodeLocation(file, line, column);
    }

    public static CodeLocation toLocation(Token token) {
        return toLocation(token.getFile(), token.getLine(), token.getChar());
    }

    public static CodeLocation toLocation(Node node) {
        Token token = node.getFirstTokenNode();
        return toLocation(token);
    }
}
