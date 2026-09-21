package net.momirealms.craftengine.proxy.minecraft.world.level.block;

import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

/**
 * Minecraft 26.3 added a {@code BonemealSource} enum ({@code INTERACTION}, {@code MOB}) as the
 * trailing parameter of every {@code BonemealableBlock} method. The class does not exist below
 * 26.3, hence the version gate: on older servers the proxy is inert and must not be touched.
 */
@ReflectionProxy(name = "net.minecraft.world.level.block.BonemealSource", activeIf = "min_version=26.3")
public interface BonemealSourceProxy {
    BonemealSourceProxy INSTANCE = ASMProxyFactory.create(BonemealSourceProxy.class);

    @MethodInvoker(name = "values", isStatic = true)
    Enum<?>[] values();
}
