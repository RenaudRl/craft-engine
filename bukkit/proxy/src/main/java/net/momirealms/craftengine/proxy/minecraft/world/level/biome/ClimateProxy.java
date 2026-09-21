package net.momirealms.craftengine.proxy.minecraft.world.level.biome;

import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

public interface ClimateProxy {

    /** 26.3: passed explicitly to {@code Structure#generate}; obtained from {@code RandomState}. */
    @ReflectionProxy(name = "net.minecraft.world.level.biome.Climate$Sampler", activeIf = "min_version=26.3")
    interface SamplerProxy {
    }
}
