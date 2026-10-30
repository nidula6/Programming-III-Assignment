package com.sortperf.sort;

/**
 * Common contract for every sorting algorithm used in the application.
 * Each implementation sorts the given array in place, in ascending order.
 */
public interface SortingAlgorithm {

    /** Human readable name, e.g. "Merge Sort". */
    String getName();

    /** Average-case time complexity, shown in the results table. */
    String getComplexity();

    /** Sorts the array in place (ascending). */
    void sort(double[] data);
}
