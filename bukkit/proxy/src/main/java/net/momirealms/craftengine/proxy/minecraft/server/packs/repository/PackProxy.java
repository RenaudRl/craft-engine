package net.momirealms.craftengine.proxy.minecraft.server.packs.repository;

import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

import java.util.List;
import java.util.stream.Stream;

@ReflectionProxy(name = "net.minecraft.server.packs.repository.Pack")
public interface PackProxy {
    PackProxy INSTANCE = ASMProxyFactory.create(PackProxy.class);

    /** Up to 26.2 a single {@code PackResources}; since 26.3 a {@code Stream<PackResources>} (overlays). */
    @MethodInvoker(name = "open")
    Object open(Object target);

    /** Opens the pack and appends every resulting {@code PackResources} to {@code into}, whatever the version. */
    static void openInto(Object pack, List<Object> into) {
        Object opened = INSTANCE.open(pack);
        if (opened instanceof Stream<?> stream) {
            stream.forEach(into::add);
        } else {
            into.add(opened);
        }
    }
}
