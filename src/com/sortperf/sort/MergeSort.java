package com.sortperf.sort;

/** Merge Sort - top-down divide and conquer using one reusable temporary buffer. */
public class MergeSort implements SortingAlgorithm {

    @Override
    public String getName() {
        return "Merge Sort";
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
        double[] buffer = new double[a.length];
        mergeSort(a, buffer, 0, a.length - 1);
    }

    private void mergeSort(double[] a, double[] buffer, int low, int high) {
        if (low >= high) {
            return;
        }
        int mid = (low + high) >>> 1;
        mergeSort(a, buffer, low, mid);
        mergeSort(a, buffer, mid + 1, high);
        merge(a, buffer, low, mid, high);
    }

    /** Merges the sorted ranges a[low..mid] and a[mid+1..high]. */
    private void merge(double[] a, double[] buffer, int low, int mid, int high) {
        System.arraycopy(a, low, buffer, low, high - low + 1);

        int left = low;
        int right = mid + 1;
        for (int k = low; k <= high; k++) {
            if (left > mid) {
                a[k] = buffer[right++];
            } else if (right > high) {
                a[k] = buffer[left++];
            } else if (buffer[right] < buffer[left]) {
                a[k] = buffer[right++];
            } else {
                a[k] = buffer[left++]; // '<=' keeps the sort stable
            }
        }
    }
}
