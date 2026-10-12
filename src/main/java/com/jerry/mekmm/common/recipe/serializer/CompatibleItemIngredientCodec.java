package com.jerry.mekmm.common.recipe.serializer;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import java.util.Map;

/** Accepts legacy ATM11/KubeJS item and tag fields while writing the modern format. */
public final class CompatibleItemIngredientCodec {
    private CompatibleItemIngredientCodec() {}

    public static final Codec<ItemStackIngredient> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<ItemStackIngredient, T>> decode(DynamicOps<T> ops, T input) {
            var map = ops.getMap(input).result();
            if (map.isPresent() && map.get().get("ingredient") == null) {
                T item = map.get().get("item");
                T tag = map.get().get("tag");
                // Ambiguous or unsupported inputs retain the normal codec's validation.
                if ((item == null) != (tag == null)) {
                    T value = item != null ? item : tag;
                    var id = ops.getStringValue(value).result();
                    if (id.isPresent()) {
                        T count = map.get().get("count");
                        input = ops.createMap(Map.of(
                              ops.createString("ingredient"), ops.createString((tag != null ? "#" : "") + id.get()),
                              ops.createString("count"), count != null ? count : ops.createInt(1)));
                    }
                }
            }
            return ItemStackIngredient.CODEC.decode(ops, input);
        }

        @Override
        public <T> DataResult<T> encode(ItemStackIngredient input, DynamicOps<T> ops, T prefix) {
            return ItemStackIngredient.CODEC.encode(input, ops, prefix);
        }
    };
}
