package com.sortperf.service;

import com.sortperf.model.SortResult;
import com.sortperf.sort.SortingAlgorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Runs sorting algorithms, measures their execution time and picks the best performer. */
public final class PerformanceEvaluator {

    private static final int HEAD_SIZE = 10;

    /**
     * Sorts a private copy of the data with the given algorithm and times only the sort itself
     * (copying and verification are excluded from the measurement).
     */
    public SortResult run(SortingAlgorithm algorithm, double[] original) {
        double[] working = Arrays.copyOf(original, original.length);

        long start = System.nanoTime();
        algorithm.sort(working);
        long elapsed = System.nanoTime() - start;

        boolean sorted = isSorted(working);
        double[] head = Arrays.copyOf(working, Math.min(HEAD_SIZE, working.length));
        return new SortResult(algorithm.getName(), algorithm.getComplexity(), elapsed, sorted, head);
    }

    /** Verified results first, then fastest first. */
    public List<SortResult> rank(List<SortResult> results) {
        List<SortResult> ranked = new ArrayList<>(results);
        ranked.sort(Comparator
                .comparing((SortResult r) -> !r.isVerified())
                .thenComparingLong(SortResult::getTimeNanos));
        return ranked;
    }

    /** The verified result with the shortest execution time. */
    public Optional<SortResult> findBest(List<SortResult> results) {
        return results.stream()
                .filter(SortResult::isVerified)
                .min(Comparator.comparingLong(SortResult::getTimeNanos));
    }

    public static boolean isSorted(double[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i - 1] > a[i]) {
                return false;
            }
        }
        return true;
    }
}
