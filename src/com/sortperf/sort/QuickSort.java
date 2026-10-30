package com.sortperf.sort;

/**
 * Quick Sort - Hoare partition scheme with a median-of-three pivot.
 * The smaller partition is processed recursively and the larger one iteratively,
 * so the recursion depth stays O(log n) even on adversarial input.
 */
public class QuickSort implements SortingAlgorithm {

    @Override
    public String getName() {
        return "Quick Sort";
    }

    @Override
    public String getComplexity() {
        return "O(n log n)";
    }

    @Override
    public void sort(double[] a) {
        if (a == null || a.length < 2) {
            return;
        }
        quickSort(a, 0, a.length - 1);
    }

    private void quickSort(double[] a, int low, int high) {
        while (low < high) {
            int split = partition(a, low, high);
            // Recurse into the smaller side, loop on the larger side
            if (split - low < high - split) {
                quickSort(a, low, split);
                low = split + 1;
            } else {
                quickSort(a, split + 1, high);
                high = split;
            }
        }
    }

    /** Hoare partition. Returns j so that a[low..j] <= pivot <= a[j+1..high]. */
    private int partition(double[] a, int low, int high) {
        double pivot = medianOfThree(a[low], a[(low + high) >>> 1], a[high]);
        int i = low - 1;
        int j = high + 1;
        while (true) {
            do {
                i++;
            } while (a[i] < pivot);
            do {
                j--;
            } while (a[j] > pivot);
            if (i >= j) {
                return j;
            }
            double temp = a[i];
            a[i] = a[j];
            a[j] = temp;
        }
    }

    private double medianOfThree(double x, double y, double z) {
        return Math.max(Math.min(x, y), Math.min(Math.max(x, y), z));
    }
}
