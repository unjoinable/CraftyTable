package io.github.unjoinable.craftytable.utils;

import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * High-performance square grid for crafting recipes with efficient pattern matching.
 * Uses 1D array storage and cached hashing for optimal memory layout and speed.
 * Now leverages GridOrientationGenerator for orientation generation.
 */
public class CraftingGrid {
    private static final int AIR_HASH = 0;

    private final Material[] data;
    private final int size;
    private long cachedHash = 0;
    private boolean hashValid = false;

    /**
     * Creates a new square crafting grid with the specified size.
     * @param size the dimensions of the square grid (size x size)
     */
    public CraftingGrid(int size) {
        this.size = size;
        this.data = new Material[size * size];
    }

    /**
     * Creates a new crafting grid from an existing Material array.
     * @param size the dimensions of the square grid (size x size)
     * @param data the material data array (must be size * size length)
     */
    public CraftingGrid(int size, Material[] data) {
        if (data.length != size * size) {
            throw new IllegalArgumentException("Data array length must equal size * size");
        }
        this.size = size;
        this.data = data.clone();
    }

    /**
     * Sets a material at the specified grid position and invalidates the cached hash.
     * @param row the row index (0-based)
     * @param col the column index (0-based)
     * @param value the material to place, or null for empty slot
     */
    public void set(int row, int col, @Nullable Material value) {
        data[row * size + col] = value;
        hashValid = false;
    }

    /**
     * Gets the material at the specified grid position.
     * @param row the row index (0-based)
     * @param col the column index (0-based)
     * @return the material at the position, or null if empty
     */
    public @Nullable Material get(int row, int col) {
        return data[row * size + col];
    }

    public Material[] getData() {
        return data;
    }

    public int getSize() {
        return size;
    }

    /**
     * Generates all possible orientations of the current pattern within the grid bounds.
     * This finds the minimal bounding box of non-null materials and translates it to
     * every valid position in the grid, creating all possible recipe variations.
     * Uses the static GridOrientationGenerator for efficient processing.
     *
     * @return list of all valid orientations, or single-element list containing this grid if empty
     */
    public List<CraftingGrid> getAllOrientations() {
        List<Material[]> orientationArrays = GridOrientationGenerator.getAllOrientations(data, size, Material.class);
        return orientationArrays.stream()
                .map(array -> new CraftingGrid(size, array))
                .toList();
    }

    /**
     * Returns a cached hash of the grid contents, calculating it only when needed.
     * Uses the same hash algorithm as the original encodeRecipe method.
     * @return 64-bit hash of the grid contents
     */
    public long hash() {
        if (!hashValid) {
            cachedHash = calculateHash();
            hashValid = true;
        }
        return cachedHash;
    }

    /**
     * Calculates hash using polynomial rolling hash with prime multiplier.
     * @return 64-bit hash value
     */
    private long calculateHash() {
        long hash = 1;
        for (Material element : data) {
            hash = hash * 31 + (element == null ? AIR_HASH : element.key().hashCode());
        }
        return hash;
    }

    /**
     * Calculates a normalized hash for shapeless recipes by sorting materials by ID.
     * This produces the same hash regardless of material placement in the grid.
     * @return 64-bit normalized hash value
     */
    public long normalizedHash() {
        // Extract and sort material IDs
        int[] ids = new int[size * size];
        int count = 0;
        for (Material mat : data) {
            if (mat != null) ids[count++] = mat.id();
        }

        // Insertion sort for small arrays
        for (int i = 1; i < count; i++) {
            int key = ids[i];
            int j = i - 1;
            while (j >= 0 && ids[j] > key) ids[j + 1] = ids[j--];
            ids[j + 1] = key;
        }

        // Calculate hash from sorted IDs
        long hash = 1;
        for (int i = 0; i < count; i++) {
            hash = hash * 31 + ids[i];
        }
        return hash;
    }

    /**
     * Compares grids for equality using hash-based comparison for performance.
     * @param o the object to compare with
     * @return true if grids have identical contents
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CraftingGrid that = (CraftingGrid) o;
        return this.hash() == that.hash();
    }

    @Override
    public String toString() {
        return "CraftingGrid{" +
                "data=" + Arrays.toString(data) +
                ", size=" + size +
                ", cachedHash=" + cachedHash +
                ", hashValid=" + hashValid +
                '}';
    }
}