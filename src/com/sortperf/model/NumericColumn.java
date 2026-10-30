package com.sortperf.model;

/** The numeric values extracted from one CSV column, plus how many cells were skipped. */
public final class NumericColumn {

    private final double[] values;
    private final int emptyCount;
    private final int invalidCount;

    public NumericColumn(double[] values, int emptyCount, int invalidCount) {
        this.values = values;
        this.emptyCount = emptyCount;
        this.invalidCount = invalidCount;
    }

    public double[] getValues() {
        return values;
    }

    public int size() {
        return values.length;
    }

    /** Blank cells that were ignored. */
    public int getEmptyCount() {
        return emptyCount;
    }

    /** Non-blank cells that were not valid numbers and were ignored. */
    public int getInvalidCount() {
        return invalidCount;
    }
}
