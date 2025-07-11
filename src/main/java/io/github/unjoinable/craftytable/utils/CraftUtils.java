package io.github.unjoinable.craftytable.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.unjoinable.craftytable.recipe.RecipeResult;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.registry.TagKey;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class providing helper methods for crafting operations.
 * This class contains static methods for material handling and validation.
 */
public final class CraftUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * @throws UnsupportedOperationException always, as this class should not be instantiated
     */
    private CraftUtils() {
        throw new UnsupportedOperationException("CraftUtils is a utility class and cannot be instantiated");
    }

    /**
     * Returns true if the recipe is a shaped crafting recipe.
     *
     * @param recipe the JsonObject representing the recipe
     * @return true if type is "minecraft:crafting_shaped", false otherwise
     */
    public static boolean isShaped(JsonObject recipe) {
        return "minecraft:crafting_shaped".equals(recipe.get("type").getAsString());
    }

    /**
     * Returns true if the recipe is a shapeless crafting recipe.
     *
     * @param recipe the JsonObject representing the recipe
     * @return true if type is "minecraft:crafting_shapeless", false otherwise
     */
    public static boolean isShapeless(JsonObject recipe) {
        return "minecraft:crafting_shapeless".equals(recipe.get("type").getAsString());
    }

    /**
     * Checks if any ingredient in the recipe uses a tag (starts with '#').
     *
     * @param recipe the recipe JSON object
     * @return true if any ingredient starts with '#', false otherwise
     */
    public static boolean hasTaggedShapeless(JsonObject recipe) {
        JsonArray ingredients = recipe.getAsJsonArray("ingredients");
        return ingredients.asList().stream()
                .filter(element -> element.isJsonPrimitive() && element.getAsJsonPrimitive().isString())
                .map(JsonElement::getAsString)
                .anyMatch(s -> s.startsWith("#"));
    }


    /**
     * Checks if any value in the "key" map of the recipe uses a tag (starts with '#').
     *
     * @param recipe the recipe JSON object
     * @return true if any key value starts with '#', false otherwise
     */
    public static boolean hasTaggedShaped(JsonObject recipe) {
        JsonObject key = recipe.getAsJsonObject("key");
        return key.asMap().values().stream()
                .filter(element -> element.isJsonPrimitive() && element.getAsJsonPrimitive().isString())
                .map(JsonElement::getAsString)
                .anyMatch(s -> s.startsWith("#"));
    }

    /**
     * Parses a crafting key JsonObject into a map of character to Material.
     *
     * @param keyObj the JsonObject representing the key mapping
     * @return a map from character symbols to Material objects
     */
    public static Map<Character, Material> extractKeyToMaterialMap(JsonObject keyObj) {
        return keyObj.getAsJsonObject("key").entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().charAt(0),
                        e -> Material.fromKey(e.getValue().getAsString())
                ));
    }

    /**
     * Extracts a map from pattern characters to Matcher objects.
     * Supports both material and tag-based keys (tagged values start with '#').
     *
     * @param keyObj the JsonObject representing the "key" section of a shaped recipe
     * @return a map from character keys to Matcher instances
     */
    public static Map<Character, Matcher<Material>> extractKeyToMatcherMap(JsonObject keyObj) {
        return keyObj.getAsJsonObject("key").entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().charAt(0),
                        e -> {
                            String value = e.getValue().getAsString();
                            if (value.startsWith("#")) {
                                return Matcher.tag(TagKey.ofHash(value));
                            } else {
                                return Matcher.material(Material.fromKey(value));
                            }
                        }
                ));
    }

    /**
     * Extracts materials from a shapeless recipe and places them into a 3x3 CraftingGrid
     * in row-major order. Remaining slots are left null.
     *
     * @param recipe the JsonObject representing the shapeless recipe
     * @return a 3x3 CraftingGrid filled with the materials
     */
    public static CraftingGrid applyShapelessToGrid(JsonObject recipe) {
        JsonArray ingredients = recipe.getAsJsonArray("ingredients");
        CraftingGrid grid = new CraftingGrid(3); // 3x3 grid

        int index = 0;
        for (JsonElement element : ingredients) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) continue;

            String key = element.getAsString();
            Material material = Material.fromKey(key);

            int row = index / 3;
            int col = index % 3;

            if (row < 3) {
                grid.set(row, col, material);
            }

            index++;
        }

        return grid;
    }

    /**
     * Parses the "pattern" field from a recipe JsonObject into a square 2D char array.
     * Shorter rows are padded with spaces (' ') to make a square matrix.
     *
     * @param recipe the full recipe JsonObject
     * @return a square 2D char array representing the crafting pattern
     */
    public static char[][] parsePattern(JsonObject recipe) {
        JsonArray patternArray = recipe.getAsJsonArray("pattern");
        List<String> rows = patternArray.asList().stream()
                .map(JsonElement::getAsString)
                .toList();

        int size = Math.max(rows.size(), rows.stream().mapToInt(String::length).max().orElse(0));
        char[][] pattern = new char[size][size];

        for (char[] row : pattern) {
            Arrays.fill(row, ' '); // fill entire grid with space characters
        }

        for (int i = 0; i < rows.size(); i++) {
            char[] line = rows.get(i).toCharArray();
            System.arraycopy(line, 0, pattern[i], 0, line.length);
        }

        return pattern;
    }

    /**
     * Applies a character-to-material mapping to a crafting pattern to produce a CraftingGrid.
     * Unmapped characters or spaces are set to null.
     *
     * @param map     the mapping of pattern characters to Material instances
     * @param pattern the 2D char array representing the crafting pattern
     * @return a CraftingGrid populated with materials based on the pattern and map
     */
    public static CraftingGrid applyMapToPattern(Map<Character, Material> map, char[][] pattern) {
        int size = pattern.length;
        CraftingGrid grid = new CraftingGrid(size); // Assuming square grid

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < pattern[row].length; col++) {
                char symbol = pattern[row][col];
                Material material = symbol == ' ' ? null : map.get(symbol); // null if missing or space
                grid.set(row, col, material);
            }
        }

        return grid;
    }

    /**
     * Applies a character-to-Matcher map to a 2D crafting pattern and flattens the result into a 1D array.
     * Spaces and unmapped characters are set to null.
     *
     * @param map     the mapping of characters to Matcher<Material>
     * @param pattern the 2D char array representing the crafting pattern
     * @return a flattened 1D Matcher<Material>[] array
     */
    public static Matcher<Material>[] applyMatcherMapToPattern(Map<Character, Matcher<Material>> map, char[][] pattern) {
        int rows = pattern.length;
        int cols = pattern[0].length;

        @SuppressWarnings("unchecked")
        Matcher<Material>[] flat = new Matcher[rows * cols];

        int index = 0;
        for (char[] chars : pattern) {
            for (int col = 0; col < cols; col++) {
                char symbol = chars[col];
                Matcher<Material> matcher = symbol == ' ' ? null : map.get(symbol);
                flat[index++] = matcher;
            }
        }

        return flat;
    }

    /**
     * Extracts the recipe result from the given recipe JsonObject.
     * Converts the "result" object into a RecipeResult containing an ItemStack
     * with the specified material and quantity.
     * <p>
     * Defaults to a count of 1 if the "count" field is not present.
     *
     * @param recipe the JsonObject representing the full recipe
     * @return a RecipeResult wrapping the resulting ItemStack
     */
    public static RecipeResult parseResult(JsonObject recipe) {
        JsonObject result = recipe.getAsJsonObject("result");
        String id = result.get("id").getAsString();
        int count = result.has("count") ? result.get("count").getAsInt() : 1;
        Material material = Material.fromKey(id);
        return new RecipeResult(ItemStack.of(material, count));
    }
}