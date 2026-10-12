package com.jerry.mekmm.common.recipe.serializer;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Map;
import java.util.Set;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/** Supplies craftable MoreMachine molds only when the original IE item is absent. */
public final class CompatibleMoldCodec {
    private CompatibleMoldCodec() {}
    private static final Set<String> LEGACY = Set.of("immersiveengineering:mold_gear",
          "immersiveengineering:mold_plate", "immersiveengineering:mold_rod");
    public static final Codec<ItemStackIngredient> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<ItemStackIngredient, T>> decode(DynamicOps<T> ops, T input) {
            var map = ops.getMap(input).result();
            if (map.isPresent()) {
                String field = map.get().get("ingredient") != null ? "ingredient" : "item";
                T value = map.get().get(field);
                if (value != null && map.get().get("tag") == null) {
                    var id = ops.getStringValue(value).result();
                    if (id.isPresent() && LEGACY.contains(id.get()) &&
                          !BuiltInRegistries.ITEM.containsKey(Identifier.parse(id.get()))) {
                        T count = map.get().get("count");
                        input = ops.createMap(Map.of(
                              ops.createString("ingredient"), ops.createString(id.get().replace("immersiveengineering:", "mekmm:")),
                              ops.createString("count"), count == null ? ops.createInt(1) : count));
                    }
                }
            }
            return CompatibleItemIngredientCodec.CODEC.decode(ops, input);
        }
        @Override
        public <T> DataResult<T> encode(ItemStackIngredient input, DynamicOps<T> ops, T prefix) {
            return ItemStackIngredient.CODEC.encode(input, ops, prefix);
        }
    };
}
