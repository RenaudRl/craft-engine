package net.momirealms.craftengine.proxy.minecraft.world.entity;

import net.momirealms.craftengine.proxy.minecraft.world.phys.Vec3Proxy;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.ConstructorInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;
import net.momirealms.sparrow.reflection.proxy.annotation.Type;

/**
 * 26.3: entity position sync packets carry a {@code PositionPath} (a linear jump, or a stepped
 * path) instead of a {@code PositionMoveRotation}. CraftEngine only ever needs the linear form.
 */
@ReflectionProxy(name = "net.minecraft.world.entity.PositionPath", activeIf = "min_version=26.3")
public interface PositionPathProxy {

    @ReflectionProxy(name = "net.minecraft.world.entity.PositionPath$Linear", activeIf = "min_version=26.3")
    interface LinearProxy {
        LinearProxy INSTANCE = ASMProxyFactory.create(LinearProxy.class);

        @ConstructorInvoker
        Object newInstance(@Type(clazz = Vec3Proxy.class) Object endPosition);
    }
}
