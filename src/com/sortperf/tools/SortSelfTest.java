package com.sortperf.tools;

import com.sortperf.sort.HeapSort;
import com.sortperf.sort.InsertionSort;
import com.sortperf.sort.MergeSort;
import com.sortperf.sort.QuickSort;
import com.sortperf.sort.ShellSort;
import com.sortperf.sort.SortingAlgorithm;

import java.util.Arrays;
import java.util.Random;

/**
 * Optional console check: compares every algorithm against java.util.Arrays.sort on many inputs.
 * Run this class to prove the algorithms are correct (useful for the "Correctness" criterion).
 */
public final class SortSelfTest {

    private SortSelfTest() {
    }

    public static void main(String[] args) {
        SortingAlgorithm[] algorithms = {
                new InsertionSort(), new ShellSort(), new MergeSort(), new QuickSort(), new HeapSort()};
        Random random = new Random(42);
        int failures = 0;

        for (SortingAlgorithm algorithm : algorithms) {
            int cases = 0;
            int failed = 0;
            for (double[] input : buildInputs(random)) {
                double[] expected = input.clone();
                Arrays.sort(expected);
                double[] actual = input.clone();
                algorithm.sort(actual);
                cases++;
                if (!Arrays.equals(expected, actual)) {
                    failed++;
                }
            }
            failures += failed;
            System.out.printf("%-15s %d cases, %d failed -> %s%n",
                    algorithm.getName(), cases, failed, failed == 0 ? "PASS" : "FAIL");
        }
        System.out.println(failures == 0 ? "\nAll algorithms are correct." : "\nSOME TESTS FAILED.");
    }

    private static double[][] buildInputs(Random random) {
        int[] sizes = {0, 1, 2, 3, 5, 10, 100, 1000, 5000};
        double[][] inputs = new double[sizes.length * 6][];
        int k = 0;
        for (int n : sizes) {
            double[] randomData = new double[n];
            double[] sortedData = new double[n];
            double[] reversed = new double[n];
            double[] duplicates = new double[n];
            double[] allSame = new double[n];
            double[] negatives = new double[n];
            for (int i = 0; i < n; i++) {
                randomData[i] = random.nextDouble() * 1000;
                sortedData[i] = i;
                reversed[i] = n - i;
                duplicates[i] = random.nextInt(5);
                allSame[i] = 7.5;
                negatives[i] = random.nextInt(2001) - 1000;
            }
            inputs[k++] = randomData;
            inputs[k++] = sortedData;
            inputs[k++] = reversed;
            inputs[k++] = duplicates;
            inputs[k++] = allSame;
            inputs[k++] = negatives;
        }
        return inputs;
    }
}
