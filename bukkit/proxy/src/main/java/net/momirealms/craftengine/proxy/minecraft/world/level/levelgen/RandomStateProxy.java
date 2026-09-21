package net.momirealms.craftengine.proxy.minecraft.world.level.levelgen;

import net.momirealms.craftengine.proxy.minecraft.world.level.levelgen.densityfunction.SamplerContextProxy;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;
import net.momirealms.sparrow.reflection.proxy.annotation.Type;

@ReflectionProxy(name = "net.minecraft.world.level.levelgen.RandomState")
public interface RandomStateProxy {
    RandomStateProxy INSTANCE = ASMProxyFactory.create(RandomStateProxy.class);

    // 26.3: the climate sampler is built per use from a sampler context (Structure#generate needs one).
    @MethodInvoker(name = "createClimateSampler", activeIf = "min_version=26.3")
    Object createClimateSampler(Object target, @Type(clazz = SamplerContextProxy.class) Object context);
}
