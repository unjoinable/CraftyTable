package io.github.unjoinable.craftytable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.unjoinable.craftytable.recipe.impl.ShapedRecipe;
import io.github.unjoinable.craftytable.recipe.impl.ShapedTaggedRecipe;
import io.github.unjoinable.craftytable.recipe.impl.ShapelessRecipe;
import io.github.unjoinable.craftytable.recipe.traits.Recipe;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static io.github.unjoinable.craftytable.utils.CraftUtils.*;

public class RecipeLoader {

    private final File recipeFolder;
    private final Gson gson;

    public RecipeLoader(File recipeFolder) {
        this.recipeFolder = recipeFolder;
        this.gson = new Gson();
    }

    public RecipeLoader() {
        this(new File("recipe"));
    }

    /**
     * Loads all valid recipes from the recipe folder.
     *
     * @return a list of successfully parsed Recipe instances
     */
    public List<Recipe> loadAll() {
        List<Recipe> recipes = new ArrayList<>();

        if (!recipeFolder.exists() || !recipeFolder.isDirectory()) {
            System.err.println("Recipe folder not found: " + recipeFolder.getAbsolutePath());
            return recipes;
        }

        File[] files = recipeFolder.listFiles((dir, name) -> name.endsWith(".json") && !name.contains("from"));
        if (files == null) return recipes;

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                JsonObject recipeJson = gson.fromJson(reader, JsonObject.class);
                Recipe recipe = parseRecipe(recipeJson);
                if (recipe != null) {
                    recipes.add(recipe);
                } else {
                    System.err.println("Skipped invalid or unsupported recipe: " + file.getName());
                }
            } catch (Exception e) {
                System.err.println("Failed to parse recipe: " + file.getName());
            }
        }

        return recipes;
    }

    private Recipe parseRecipe(JsonObject recipe) {
        if (isShaped(recipe)) return parseShaped(recipe);
        if (isShapeless(recipe)) return parseShapeless(recipe);
        return null;
    }

    private Recipe parseShaped(JsonObject recipe) {
        if (hasTaggedShaped(recipe)) {
            var grid = applyMatcherMapToPattern(extractKeyToMatcherMap(recipe), parsePattern(recipe));
            var result = parseResult(recipe);
            return new ShapedTaggedRecipe(grid, result);
        } else {
            var grid = applyMapToPattern(extractKeyToMaterialMap(recipe), parsePattern(recipe));
            var result = parseResult(recipe);
            return new ShapedRecipe(grid, result);
        }
    }

    private Recipe parseShapeless(JsonObject recipe) {
        if (hasTaggedShapeless(recipe)) {
            // You can support this later by implementing a TaggedShapelessRecipe
            System.err.println("Tagged shapeless recipes are currently unsupported.");
            return null;
        }

        var grid = applyShapelessToGrid(recipe);
        var result = parseResult(recipe);
        return new ShapelessRecipe(grid, result);
    }
}