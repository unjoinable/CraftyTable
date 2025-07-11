package io.github.unjoinable.craftytable.utils;

import net.minestom.server.item.Material;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.registry.TagKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

/**
 * A generic interface for evaluating whether a value satisfies certain criteria.
 * <p>
 * {@code Matcher<T>} provides a flexible and reusable mechanism for pattern matching,
 * commonly used in recipe validation, filtering systems, or tag-based logic. It supports
 * both exact value checks and broader tag-based inclusion tests.
 *
 * @param <T> the type of value this matcher operates on
 */
public sealed interface Matcher<T> {

    /**
     * Tests whether the provided value matches this matcher's condition.
     *
     * @param value the value to evaluate, may be {@code null}
     * @return {@code true} if the value satisfies the match condition; otherwise {@code false}
     */
    boolean matches(@Nullable T value);

    /**
     * Creates a matcher that checks for exact equality with a given {@link Material}.
     * <p>
     * This matcher performs a reference equality check ({@code ==}), which is suitable
     * since {@code Material} instances are effectively singletons.
     *
     * @param material the target material to match against
     * @return a matcher that matches only the specified material
     */
    static Matcher<Material> material(@NotNull Material material) {
        return new MaterialMatcher(material);
    }

    /**
     * Creates a matcher that checks whether a {@link Material} is part of a specified tag.
     * <p>
     * This is ideal for recipes or systems that accept a group of related materials
     * (e.g., all wood types). If the tag is invalid or empty, the matcher will behave accordingly.
     *
     * @param tagKey the tag key representing a group of materials
     * @return a matcher that returns {@code true} for any material included in the tag
     */
    static Matcher<Material> tag(@NotNull TagKey<Material> tagKey) {
        var tag = Material.staticRegistry().getTag(tagKey);
        var materials = StreamSupport
                .stream(tag.spliterator(), false)
                .map(RegistryKey::key)
                .map(Material::fromKey)
                .filter(Objects::nonNull)
                .toList();
        return new TaggedMatcher(materials);
    }

    /**
     * Matcher implementation for exact material matching using reference comparison.
     *
     * @param material the target material for exact matching
     */
    record MaterialMatcher(@NotNull Material material) implements Matcher<Material> {

        @Override
        public boolean matches(@Nullable Material value) {
            return material == value;
        }
    }

    /**
     * Matcher implementation for tag-based material matching.
     * <p>
     * Matches any material that exists within the given list of tagged materials.
     *
     * @param materials list of materials included in the tag, must not be null
     */
    record TaggedMatcher(@NotNull List<Material> materials) implements Matcher<Material> {

        @Override
        public boolean matches(@Nullable Material value) {
            return value != null && materials.contains(value);
        }
    }
}