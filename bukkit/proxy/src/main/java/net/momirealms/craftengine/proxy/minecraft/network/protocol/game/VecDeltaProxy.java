package net.momirealms.craftengine.proxy.minecraft.network.protocol.game;

import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.ConstructorInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

/**
 * 26.3: relative move packets carry a {@code VecDelta} (linear, or stepped over several ticks)
 * instead of three raw shorts. The linear form is the pre-26.3 triple, 1/4096 of a block each.
 */
@ReflectionProxy(name = "net.minecraft.network.protocol.game.VecDelta", activeIf = "min_version=26.3")
public interface VecDeltaProxy {

    @ReflectionProxy(name = "net.minecraft.network.protocol.game.VecDelta$Linear", activeIf = "min_version=26.3")
    interface LinearProxy {
        LinearProxy INSTANCE = ASMProxyFactory.create(LinearProxy.class);

        @ConstructorInvoker
        Object newInstance(short xa, short ya, short za);
    }
}
