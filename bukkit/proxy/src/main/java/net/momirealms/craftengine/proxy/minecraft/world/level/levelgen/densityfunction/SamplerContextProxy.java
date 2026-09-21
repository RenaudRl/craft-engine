package net.momirealms.craftengine.proxy.minecraft.world.level.levelgen.densityfunction;

import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

/**
 * 26.3: density functions sample through a context (caches, buffer arena). A climate sampler is
 * created from one; {@link #simple()} builds the plain cached context vanilla uses for structures.
 */
@ReflectionProxy(name = "net.minecraft.world.level.levelgen.densityfunction.SamplerContext", activeIf = "min_version=26.3")
public interface SamplerContextProxy {
    SamplerContextProxy INSTANCE = ASMProxyFactory.create(SamplerContextProxy.class);

    @MethodInvoker(name = "builder", isStatic = true)
    Object builder();

    static Object simple() {
        Object builder = INSTANCE.builder();
        BuilderProxy.INSTANCE.enableCaches(builder);
        return BuilderProxy.INSTANCE.build(builder);
    }

    @ReflectionProxy(name = "net.minecraft.world.level.levelgen.densityfunction.SamplerContext$Builder", activeIf = "min_version=26.3")
    interface BuilderProxy {
        BuilderProxy INSTANCE = ASMProxyFactory.create(BuilderProxy.class);

        @MethodInvoker(name = "enableCaches")
        Object enableCaches(Object target);

        @MethodInvoker(name = "build")
        Object build(Object target);
    }
}
