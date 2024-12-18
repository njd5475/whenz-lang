package com.anor.roar.whenzint.actions;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.parser.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public abstract class AbstractAction extends Action {

    private final int _line;
    private final int _column;
    private final File _file;

    public AbstractAction(File file, int line, int column) {
        this._line = line;
        this._column = column;
        this._file = file;
    }

    public AbstractAction(CodeLocation location) {
        this(location.getFile(), location.getLine(), location.getColumn());
    }

    @Override
    public int getLine() {
        return _line;
    }

    @Override
    public int getColumn() {
        return _column;
    }

    @Override
    public File getFile() {
        return _file;
    }
}