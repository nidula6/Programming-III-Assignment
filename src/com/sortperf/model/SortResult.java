package com.sortperf.model;

/** Immutable outcome of running one sorting algorithm. */
public final class SortResult {

    private final String algorithmName;
    private final String complexity;
    private final long timeNanos;
    private final boolean verified;
    private final double[] sortedHead;

    public SortResult(String algorithmName, String complexity, long timeNanos,
                      boolean verified, double[] sortedHead) {
        this.algorithmName = algorithmName;
        this.complexity = complexity;
        this.timeNanos = timeNanos;
        this.verified = verified;
        this.sortedHead = sortedHead;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public String getComplexity() {
        return complexity;
    }

    public long getTimeNanos() {
        return timeNanos;
    }

    public double getTimeMillis() {
        return timeNanos / 1_000_000.0;
    }

    /** True when the output array was checked and is really in ascending order. */
    public boolean isVerified() {
        return verified;
    }

    /** The first few values of the sorted output (for display). */
    public double[] getSortedHead() {
        return sortedHead;
    }
}
