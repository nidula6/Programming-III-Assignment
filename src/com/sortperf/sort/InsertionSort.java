package com.sortperf.sort;

/** Insertion Sort - builds the sorted portion one element at a time. */
public class InsertionSort implements SortingAlgorithm {

    @Override
    public String getName() {
        return "Insertion Sort";
    }

    @Override
    public String getComplexity() {
        return "O(n^2)";
    }

    @Override
    public void sort(double[] a) {
        if (a == null || a.length < 2) {
            return;
        }
        for (int i = 1; i < a.length; i++) {
            double key = a[i];
            int j = i - 1;
            // Shift larger elements one position to the right
            while (j >= 0 && a[j] > key) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }
}
