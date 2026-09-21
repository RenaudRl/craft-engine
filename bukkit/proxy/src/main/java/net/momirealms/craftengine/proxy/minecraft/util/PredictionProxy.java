package net.momirealms.craftengine.proxy.minecraft.util;

import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.FieldGetter;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

/**
 * 26.3: whether the client already predicted an action (a hand drop, a swing) or the server does
 * it alone. Replaces the {@code traceItem} boolean of {@code Player#drop}. Server-driven drops are
 * {@link #SERVER_ONLY}.
 */
@ReflectionProxy(name = "net.minecraft.util.Prediction", activeIf = "min_version=26.3")
public interface PredictionProxy {
    PredictionProxy INSTANCE = ASMProxyFactory.create(PredictionProxy.class);
    Object PREDICTED = INSTANCE.getPredicted();
    Object SERVER_ONLY = INSTANCE.getServerOnly();

    @FieldGetter(name = "PREDICTED", isStatic = true)
    Object getPredicted();

    @FieldGetter(name = "SERVER_ONLY", isStatic = true)
    Object getServerOnly();
}
