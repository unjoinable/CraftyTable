package io.github.unjoinable.craftytable.utils;

import net.minestom.server.item.Material;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.registry.TagKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
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
     * This implementation uses a BitSet for O(1) lookup performance, making it ideal for
     * recipes or systems that frequently check material membership in groups of related
     * materials (e.g., all wood types, stone variants, or metal ingots).
     * <p>
     * If the tag is invalid, empty, or contains no valid materials, the matcher will
     * return {@code false} for all materials.
     *
     * @param tagKey the tag key representing a group of materials
     * @return a fast matcher that returns {@code true} for any material included in the tag
     */
    static Matcher<Material> tag(@NotNull TagKey<Material> tagKey) {
        var tag = Material.staticRegistry().getTag(tagKey);
        var bitset = new BitSet();
        var materials = StreamSupport
                .stream(tag.spliterator(), false)
                .map(RegistryKey::key)
                .map(Material::fromKey)
                .filter(Objects::nonNull)
                .toList();

        materials.forEach(material -> bitset.set(material.id()));

        return new TaggedMatcher(bitset);
    }

    /**
     * Matcher implementation for exact material matching using reference comparison.
     * <p>
     * This implementation performs constant-time O(1) lookups by comparing object references
     * directly, taking advantage of Material's singleton nature.
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
     * High-performance matcher implementation for tag-based material matching using BitSet.
     * <p>
     * This implementation provides O(1) lookup performance for tag membership tests by
     * using a BitSet where each bit position corresponds to a material ID. This makes it
     * extremely efficient for frequently-checked tag queries, especially when dealing with
     * small to medium-sized tag groups (typical range: 5-10 materials per tag).
     * <p>
     * The BitSet is sized based on the maximum material ID in the range (0-2000), consuming
     * approximately 250 bytes of memory regardless of the actual number of materials in the tag.
     *
     * @param bitSet the BitSet containing set bits for each material ID that belongs to the tag
     */
    record TaggedMatcher(@NotNull BitSet bitSet) implements Matcher<Material> {

        @Override
        public boolean matches(@Nullable Material value) {
            return value != null &&
                    value.id() >= 0 &&
                    bitSet().get(value.id());
        }
    }
}