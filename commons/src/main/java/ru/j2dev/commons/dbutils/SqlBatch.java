package ru.j2dev.commons.dbutils;

public class SqlBatch {
    private final String _header;
    private final String _tail;
    private final StringBuilder _result;
    private StringBuilder _sb;
    private long _limit;
    private boolean isEmpty;

    public SqlBatch(final String header, final String tail) {
        _limit = Long.MAX_VALUE;
        isEmpty = true;
        _header = header + "\n";
        _tail = ((tail != null && tail.length() > 0) ? (" " + tail + ";\n") : ";\n");
        _sb = new StringBuilder(_header);
        _result = new StringBuilder();
    }

    public SqlBatch(final String header) {
        this(header, null);
    }

    public void writeStructure(final String str) {
        _result.append(str);
    }

    public void write(final String str) {
        isEmpty = false;
        if (_sb.length() + str.length() < _limit - _tail.length()) {
            _sb.append(str).append(",\n");
        } else {
            _sb.append(str).append(_tail);
            _result.append(_sb);
            _sb = new StringBuilder(_header);
        }
    }

    public void writeBuffer() {
        final String last = _sb.toString();
        if (last.length() > 0) {
            _result.append(last, 0, last.length() - 2).append(_tail);
        }
        _sb = new StringBuilder(_header);
    }

    public String close() {
        if (_sb.length() > _header.length()) {
            writeBuffer();
        }
        return _result.toString();
    }

    public void setLimit(final long l) {
        _limit = l;
    }

    public boolean isEmpty() {
        return isEmpty;
    }
}
