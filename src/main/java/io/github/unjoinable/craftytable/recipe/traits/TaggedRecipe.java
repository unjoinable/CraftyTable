package io.github.unjoinable.craftytable.recipe.traits;

import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public interface TaggedRecipe extends Recipe {

    boolean matches(@NotNull Material[] inputMaterials);

}
