package io.github.unjoinable.craftytable;

import java.util.*;
import java.lang.reflect.Array;

public class GridArrangements {

    public static <T> long[] getOrientationHashes(T[] gridData, int size, Class<T> clazz) {
        T[][] grid = arrayTo2D(gridData, size, size, clazz);
        Set<Long> hashes = new HashSet<>();

        // Generate all translations (unlimited shifts with wrapping)
        addTranslations(grid, hashes);

        return hashes.stream().mapToLong(Long::longValue).toArray();
    }

    private static <T> void addTranslations(T[][] grid, Set<Long> hashes) {
        int size = grid.length;

        // Try all possible shifts with wrapping
        for (int dx = 0; dx < size; dx++) {
            for (int dy = 0; dy < size; dy++) {
                T[][] shifted = shift(grid, dx, dy);
                long hash = computeHash(shifted);
                hashes.add(hash);
            }
        }
    }

    public static <T> List<T[]> getAllOrientations(T[] gridData, int size, Class<T> clazz) {
        T[][] grid = arrayTo2D(gridData, size, size, clazz);
        List<T[]> orientations = new ArrayList<>();
        Set<Long> seen = new HashSet<>();

        // Generate all translations (unlimited shifts with wrapping)
        addAllTranslations(grid, orientations, seen, clazz);

        return orientations;
    }

    private static <T> void addAllTranslations(T[][] grid, List<T[]> orientations, Set<Long> seen, Class<T> clazz) {
        int size = grid.length;

        // Try all possible shifts with wrapping
        for (int dx = 0; dx < size; dx++) {
            for (int dy = 0; dy < size; dy++) {
                T[][] shifted = shift(grid, dx, dy);
                long hash = computeHash(shifted);

                // Only add if we haven't seen this orientation before
                if (!seen.contains(hash)) {
                    seen.add(hash);
                    orientations.add(arrayTo1D(shifted, clazz));
                }
            }
        }
    }

    private static <T> T[][] shift(T[][] grid, int dx, int dy) {
        int n = grid.length;
        @SuppressWarnings("unchecked")
        T[][] shifted = (T[][]) Array.newInstance(grid[0][0].getClass(), n, n);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                // Wrap around using modulo - allows unlimited movement
                int srcI = (i - dx + n) % n;
                int srcJ = (j - dy + n) % n;
                shifted[i][j] = grid[srcI][srcJ];
            }
        }
        return shifted;
    }

    private static <T> long computeHash(T[][] grid) {
        long hash = 1;
        for (T[] row : grid) {
            for (T element : row) {
                hash = hash * 31 + (element != null ? element.hashCode() : 0);
            }
        }
        return hash;
    }

    public static <T> List<T[][]> getArrangements(T[][] array, int size, Class<T> clazz) {
        T[] elements = arrayTo1D(array, clazz);

        if (elements.length < size * size) {
            return Collections.emptyList();
        }

        List<T[][]> result = new ArrayList<>();
        int[] indices = new int[size * size];
        for (int i = 0; i < size * size; i++) {
            indices[i] = i;
        }

        // Use iterative approach instead of recursive
        generatePermutationsIterative(elements, indices, size, clazz, result);
        return result;
    }

    private static <T> void generatePermutationsIterative(T[] elements, int[] indices,
                                                          int size, Class<T> clazz,
                                                          List<T[][]> result) {
        // Heap's algorithm for permutations
        int n = indices.length;
        int[] c = new int[n];

        result.add(indicesToGrid(elements, indices, size, clazz));

        int i = 0;
        while (i < n) {
            if (c[i] < i) {
                if (i % 2 == 0) {
                    swap(indices, 0, i);
                } else {
                    swap(indices, c[i], i);
                }
                result.add(indicesToGrid(elements, indices, size, clazz));
                c[i]++;
                i = 0;
            } else {
                c[i] = 0;
                i++;
            }
        }
    }

    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    private static <T> T[][] indicesToGrid(T[] elements, int[] indices, int size, Class<T> clazz) {
        @SuppressWarnings("unchecked")
        T[][] grid = (T[][]) Array.newInstance(clazz, size, size);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = elements[indices[i * size + j]];
            }
        }
        return grid;
    }

    public static <T> T[] arrayTo1D(T[][] array, Class<T> clazz) {
        int totalElements = 0;
        for (T[] row : array) {
            totalElements += row.length;
        }

        @SuppressWarnings("unchecked")
        T[] result = (T[]) Array.newInstance(clazz, totalElements);
        int index = 0;

        for (T[] row : array) {
            System.arraycopy(row, 0, result, index, row.length);
            index += row.length;
        }
        return result;
    }

    public static <T> T[][] arrayTo2D(T[] array, int rows, int cols, Class<T> clazz) {
        @SuppressWarnings("unchecked")
        T[][] result = (T[][]) Array.newInstance(clazz, rows, cols);
        int index = 0;

        for (int i = 0; i < rows && index < array.length; i++) {
            int copyLength = Math.min(cols, array.length - index);
            System.arraycopy(array, index, result[i], 0, copyLength);
            index += copyLength;
        }
        return result;
    }
}