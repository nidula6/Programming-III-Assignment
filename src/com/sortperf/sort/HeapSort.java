package com.sortperf.sort;

/** Heap Sort - builds a max-heap, then repeatedly moves the largest element to the end. */
public class HeapSort implements SortingAlgorithm {

    @Override
    public String getName() {
        return "Heap Sort";
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
        int n = a.length;

        // Phase 1: build max-heap (bottom-up)
        for (int i = n / 2 - 1; i >= 0; i--) {
            siftDown(a, i, n);
        }

        // Phase 2: extract the maximum one by one
        for (int end = n - 1; end > 0; end--) {
            double temp = a[0];
            a[0] = a[end];
            a[end] = temp;
            siftDown(a, 0, end);
        }
    }

    /** Restores the max-heap property for the subtree rooted at index i. */
    private void siftDown(double[] a, int i, int size) {
        while (true) {
            int left = 2 * i + 1;
            if (left >= size) {
                break;
            }
            int largest = left;
            int right = left + 1;
            if (right < size && a[right] > a[left]) {
                largest = right;
            }
            if (a[largest] <= a[i]) {
                break;
            }
            double temp = a[i];
            a[i] = a[largest];
            a[largest] = temp;
            i = largest;
        }
    }
}
