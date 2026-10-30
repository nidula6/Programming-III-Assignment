package com.sortperf.sort;

/** Shell Sort - insertion sort over decreasing gaps (Knuth sequence 1, 4, 13, 40, ...). */
public class ShellSort implements SortingAlgorithm {

    @Override
    public String getName() {
        return "Shell Sort";
    }

    @Override
    public String getComplexity() {
        return "O(n^1.5) approx.";
    }

    @Override
    public void sort(double[] a) {
        if (a == null || a.length < 2) {
            return;
        }
        int n = a.length;

        // Largest gap of the Knuth sequence below n/3
        int gap = 1;
        while (gap < n / 3) {
            gap = 3 * gap + 1;
        }

        for (; gap >= 1; gap /= 3) {
            // Gapped insertion sort
            for (int i = gap; i < n; i++) {
                double temp = a[i];
                int j = i;
                while (j >= gap && a[j - gap] > temp) {
                    a[j] = a[j - gap];
                    j -= gap;
                }
                a[j] = temp;
            }
        }
    }
}
