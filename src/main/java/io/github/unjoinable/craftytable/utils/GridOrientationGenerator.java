package io.github.unjoinable.craftytable.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * High-performance static utility for generating all possible orientations of patterns
 * within square grids. Works with any type of array elements.
 */
public final class GridOrientationGenerator {

    private GridOrientationGenerator() {
        throw new UnsupportedOperationException("Cannot instance utility class");
    }

    /**
     * Generates all possible orientations of a pattern within a square grid.
     * Finds the minimal bounding box of non-null elements and translates it to
     * every valid position in the grid.
     *
     * @param <T> the type of elements in the grid
     * @param grid the input grid as a 1D array (row-major order)
     * @param size the dimensions of the square grid (size x size)
     * @param type the class of the element type
     * @return list of all valid orientations as 1D arrays
     */
    public static <T> List<T[]> getAllOrientations(T[] grid, int size, Class<T> type) {
        if (grid.length != size * size) {
            throw new IllegalArgumentException("Grid array length must equal size * size");
        }

        PatternData<T> pattern = extractPattern(grid, size, type);

        if (pattern.isEmpty()) {
            List<T[]> orientations = new ArrayList<>(1);
            T[] clone = (T[]) Array.newInstance(type, grid.length);
            System.arraycopy(grid, 0, clone, 0, grid.length);
            orientations.add(clone);
            return orientations;
        }

        return generateAllValidPlacements(pattern, size, type);
    }

    /**
     * Generates all possible orientations of a pattern within a square grid.
     * Alternative method that works with 2D arrays.
     *
     * @param <T> the type of elements in the grid
     * @param grid the input grid as a 2D array
     * @param type the class of the element type
     * @return list of all valid orientations as 2D arrays
     */
    public static <T> List<T[][]> getAllOrientations(T[][] grid, Class<T> type) {
        int size = grid.length;

        for (T[] row : grid) {
            if (row.length != size) {
                throw new IllegalArgumentException("Grid must be square");
            }
        }

        @SuppressWarnings("unchecked")
        T[] flatGrid = (T[]) Array.newInstance(type, size * size);
        for (int i = 0; i < size; i++) {
            System.arraycopy(grid[i], 0, flatGrid, i * size, size);
        }

        List<T[]> flatOrientations = getAllOrientations(flatGrid, size, type);

        List<T[][]> result = new ArrayList<>(flatOrientations.size());
        for (T[] flat : flatOrientations) {
            result.add(convertTo2D(flat, size, type));
        }

        return result;
    }

    private static <T> PatternData<T> extractPattern(T[] grid, int size, Class<T> type) {
        PatternBounds bounds = findPatternBounds(grid, size);
        return bounds.isEmpty() ? PatternData.empty(type) : createPatternFromBounds(grid, size, bounds, type);
    }

    private static <T> PatternBounds findPatternBounds(T[] grid, int size) {
        int minRow = size, maxRow = -1, minCol = size, maxCol = -1;
        int count = 0;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i * size + j] != null) {
                    count++;
                    minRow = Math.min(minRow, i);
                    maxRow = Math.max(maxRow, i);
                    minCol = Math.min(minCol, j);
                    maxCol = Math.max(maxCol, j);
                }
            }
        }

        return new PatternBounds(minRow, maxRow, minCol, maxCol, count);
    }

    private static <T> PatternData<T> createPatternFromBounds(T[] grid, int size, PatternBounds bounds, Class<T> type) {
        int[] relRows = new int[bounds.count];
        int[] relCols = new int[bounds.count];
        @SuppressWarnings("unchecked")
        T[] values = (T[]) Array.newInstance(type, bounds.count);

        int idx = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                T val = grid[i * size + j];
                if (val != null) {
                    relRows[idx] = i - bounds.minRow;
                    relCols[idx] = j - bounds.minCol;
                    values[idx] = val;
                    idx++;
                }
            }
        }

        return new PatternData<>(relRows, relCols, values, bounds.getHeight(), bounds.getWidth());
    }

    private static <T> List<T[]> generateAllValidPlacements(PatternData<T> pattern, int size, Class<T> type) {
        int maxRow = size - pattern.height;
        int maxCol = size - pattern.width;

        List<T[]> results = new ArrayList<>((maxRow + 1) * (maxCol + 1));
        for (int r = 0; r <= maxRow; r++) {
            for (int c = 0; c <= maxCol; c++) {
                results.add(createOrientationAt(pattern, r, c, size, type));
            }
        }

        return results;
    }

    private static <T> T[] createOrientationAt(PatternData<T> pattern, int startRow, int startCol, int size, Class<T> type) {
        @SuppressWarnings("unchecked")
        T[] grid = (T[]) Array.newInstance(type, size * size);
        for (int i = 0; i < pattern.count; i++) {
            int row = startRow + pattern.relativeRows[i];
            int col = startCol + pattern.relativeCols[i];
            grid[row * size + col] = pattern.values[i];
        }
        return grid;
    }

    private static <T> T[][] convertTo2D(T[] flat, int size, Class<T> type) {
        @SuppressWarnings("unchecked")
        T[][] result = (T[][]) Array.newInstance(type, size, size);
        for (int i = 0; i < size; i++) {
            System.arraycopy(flat, i * size, result[i], 0, size);
        }
        return result;
    }

    private record PatternBounds(int minRow, int maxRow, int minCol, int maxCol, int count) {
        boolean isEmpty() {
            return count == 0;
        }

        int getHeight() {
            return maxRow - minRow + 1;
        }

        int getWidth() {
            return maxCol - minCol + 1;
        }
    }

    private record PatternData<T>(int[] relativeRows, int[] relativeCols, T[] values, int height, int width, int count) {
        PatternData(int[] relativeRows, int[] relativeCols, T[] values, int height, int width) {
            this(relativeRows, relativeCols, values, height, width, values.length);
        }

        static <T> PatternData<T> empty(Class<T> type) {
            @SuppressWarnings("unchecked")
            T[] emptyArray = (T[]) Array.newInstance(type, 0);
            return new PatternData<>(new int[0], new int[0], emptyArray, 0, 0, 0);
        }

        boolean isEmpty() {
            return count == 0;
        }
    }
}