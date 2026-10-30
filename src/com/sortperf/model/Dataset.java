package com.sortperf.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/** An in-memory CSV dataset: header names, data rows and which columns are numeric. */
public final class Dataset {

    /** A column is "numeric" when at least this share of its non-blank cells are valid numbers. */
    private static final double NUMERIC_THRESHOLD = 0.8;

    private static final Pattern NUMBER =
            Pattern.compile("[-+]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][-+]?\\d+)?");

    private final String name;
    private final List<String> headers;
    private final List<String[]> rows;
    private final boolean[] numeric;

    public Dataset(String name, List<String> headers, List<String[]> rows) {
        this.name = name;
        this.headers = Collections.unmodifiableList(headers);
        this.rows = Collections.unmodifiableList(rows);
        this.numeric = detectNumericColumns();
    }

    public String getName() {
        return name;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<String[]> getRows() {
        return rows;
    }

    public int getColumnCount() {
        return headers.size();
    }

    public int getRowCount() {
        return rows.size();
    }

    public boolean isNumeric(int column) {
        return numeric[column];
    }

    public boolean hasNumericColumn() {
        for (boolean b : numeric) {
            if (b) {
                return true;
            }
        }
        return false;
    }

    public int getNumericColumnCount() {
        int count = 0;
        for (boolean b : numeric) {
            if (b) {
                count++;
            }
        }
        return count;
    }

    /** Extracts the valid numbers of a column, skipping blank and non-numeric cells. */
    public NumericColumn extractNumericColumn(int column) {
        double[] buffer = new double[rows.size()];
        int count = 0;
        int empty = 0;
        int invalid = 0;
        for (String[] row : rows) {
            String cell = row[column];
            double value = parseNumber(cell);
            if (!Double.isNaN(value)) {
                buffer[count++] = value;
            } else if (cell == null || cell.trim().isEmpty()) {
                empty++;
            } else {
                invalid++;
            }
        }
        return new NumericColumn(Arrays.copyOf(buffer, count), empty, invalid);
    }

    /** Parses a plain decimal number. Returns NaN for blank, malformed, NaN or infinite input. */
    public static double parseNumber(String text) {
        if (text == null) {
            return Double.NaN;
        }
        String s = text.trim();
        if (s.isEmpty() || !NUMBER.matcher(s).matches()) {
            return Double.NaN;
        }
        try {
            double value = Double.parseDouble(s);
            return Double.isInfinite(value) ? Double.NaN : value;
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    private boolean[] detectNumericColumns() {
        boolean[] result = new boolean[headers.size()];
        for (int c = 0; c < result.length; c++) {
            int nonBlank = 0;
            int valid = 0;
            for (String[] row : rows) {
                String cell = row[c];
                if (cell == null || cell.trim().isEmpty()) {
                    continue;
                }
                nonBlank++;
                if (!Double.isNaN(parseNumber(cell))) {
                    valid++;
                }
            }
            result[c] = valid > 0 && valid >= NUMERIC_THRESHOLD * nonBlank;
        }
        return result;
    }
}
