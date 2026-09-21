package net.momirealms.craftengine.proxy.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.FieldGetter;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

/**
 * 26.3: placement modifiers no longer expose a {@code PlacementModifierType}; they answer
 * {@code codec()} instead, and the registry holds the map codecs. The generated biome filter
 * reports vanilla's biome filter codec so it dispatches like the vanilla one.
 */
@ReflectionProxy(name = "net.minecraft.world.level.levelgen.placement.BiomeFilter", activeIf = "min_version=26.3")
public interface BiomeFilterProxy {
    BiomeFilterProxy INSTANCE = ASMProxyFactory.create(BiomeFilterProxy.class);

    @FieldGetter(name = "CODEC", isStatic = true)
    MapCodec<Object> getCodec();
}
