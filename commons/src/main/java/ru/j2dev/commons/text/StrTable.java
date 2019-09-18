package ru.j2dev.commons.text;

import java.util.*;

public class StrTable {
    private final Map<Integer, Map<String, String>> rows;
    private final Map<String, Integer> columns;
    private final List<String> titles;

    public StrTable(final String title) {
        rows = new HashMap<>();
        columns = new LinkedHashMap<>();
        titles = new ArrayList<>();
        if (title != null) {
            titles.add(title);
        }
    }

    public StrTable() {
        this(null);
    }

    private static StringBuilder right(final StringBuilder result, final String s, int sz) {
        result.append(s);
        if ((sz -= s.length()) > 0) {
            for (int i = 0; i < sz; ++i) {
                result.append(" ");
            }
        }
        return result;
    }

    private static StringBuilder center(final StringBuilder result, final String s, final int sz) {
        final int offset = result.length();
        result.append(s);
        int i;
        while ((i = sz - (result.length() - offset)) > 0) {
            result.append(" ");
            if (i > 1) {
                result.insert(offset, " ");
            }
        }
        return result;
    }

    private static StringBuilder repeat(final StringBuilder result, final String s, final int sz) {
        for (int i = 0; i < sz; ++i) {
            result.append(s);
        }
        return result;
    }

    public StrTable set(final int rowIndex, final String colName, final boolean val) {
        return set(rowIndex, colName, Boolean.toString(val));
    }

    public StrTable set(final int rowIndex, final String colName, final byte val) {
        return set(rowIndex, colName, Byte.toString(val));
    }

    public StrTable set(final int rowIndex, final String colName, final char val) {
        return set(rowIndex, colName, String.valueOf(val));
    }

    public StrTable set(final int rowIndex, final String colName, final short val) {
        return set(rowIndex, colName, Short.toString(val));
    }

    public StrTable set(final int rowIndex, final String colName, final int val) {
        return set(rowIndex, colName, Integer.toString(val));
    }

    public StrTable set(final int rowIndex, final String colName, final long val) {
        return set(rowIndex, colName, Long.toString(val));
    }

    public StrTable set(final int rowIndex, final String colName, final float val) {
        return set(rowIndex, colName, Float.toString(val));
    }

    public StrTable set(final int rowIndex, final String colName, final double val) {
        return set(rowIndex, colName, Double.toString(val));
    }

    public StrTable set(final int rowIndex, final String colName, final Object val) {
        return set(rowIndex, colName, String.valueOf(val));
    }

    public StrTable set(final int rowIndex, final String colName, final String val) {
        Map<String, String> row;
        if (rows.containsKey(rowIndex)) {
            row = rows.get(rowIndex);
        } else {
            row = new HashMap<>();
            rows.put(rowIndex, row);
        }
        row.put(colName, val);
        int columnSize;
        if (!columns.containsKey(colName)) {
            columnSize = Math.max(colName.length(), val.length());
        } else if (columns.get(colName) >= (columnSize = val.length())) {
            return this;
        }
        columns.put(colName, columnSize);
        return this;
    }

    public StrTable addTitle(final String s) {
        titles.add(s);
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        if (columns.isEmpty()) {
            return result.toString();
        }
        final StringBuilder header = new StringBuilder("|");
        final StringBuilder line = new StringBuilder("|");
        for (final String c : columns.keySet()) {
            center(header, c, columns.get(c) + 2).append("|");
            repeat(line, "-", columns.get(c) + 2).append("|");
        }
        if (!titles.isEmpty()) {
            result.append(" ");
            repeat(result, "-", header.length() - 2).append(" ").append("\n");
            for (final String title : titles) {
                result.append("| ");
                right(result, title, header.length() - 3).append("|").append("\n");
            }
        }
        result.append(" ");
        repeat(result, "-", header.length() - 2).append(" ").append("\n");
        result.append(header).append("\n");
        result.append(line).append("\n");
        for (final Map<String, String> row : rows.values()) {
            result.append("|");
            for (final String c2 : columns.keySet()) {
                center(result, row.getOrDefault(c2, "-"), columns.get(c2) + 2).append("|");
            }
            result.append("\n");
        }
        result.append(" ");
        repeat(result, "-", header.length() - 2).append(" ").append("\n");
        return result.toString();
    }
}
