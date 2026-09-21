package net.momirealms.craftengine.proxy.minecraft.world.item.equipment.trim;

import net.momirealms.craftengine.proxy.minecraft.core.HolderProxy;
import net.momirealms.craftengine.proxy.minecraft.network.chat.ComponentProxy;
import net.momirealms.craftengine.proxy.minecraft.resources.IdentifierProxy;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.ConstructorInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;
import net.momirealms.sparrow.reflection.proxy.annotation.Type;

import java.util.Map;

@ReflectionProxy(name = {"net.minecraft.world.item.equipment.trim.TrimMaterial", "net.minecraft.world.item.armortrim.TrimMaterial"})
public interface TrimMaterialProxy {
    TrimMaterialProxy INSTANCE = ASMProxyFactory.create(TrimMaterialProxy.class);

    // 26.3: the material is a palette identifier (minecraft:trim/<name>) plus a description.
    @ConstructorInvoker(activeIf = "min_version=26.3")
    Object newInstance$26_3(@Type(clazz = IdentifierProxy.class) Object paletteId,
                       @Type(clazz = ComponentProxy.class) Object description);

    @ConstructorInvoker(activeIf = "min_version=1.21.5 && max_version=26.2")
    Object newInstance(@Type(clazz = MaterialAssetGroupProxy.class) Object assets,
                       @Type(clazz = ComponentProxy.class) Object description);

    @ConstructorInvoker(activeIf = "version=1.21.4")
    Object newInstance(String assetName,
                       @Type(clazz = HolderProxy.class) Object ingredient,
                       Map<Object, String> overrideArmorAssets,
                       @Type(clazz = ComponentProxy.class) Object description);

    @ConstructorInvoker(activeIf = "max_version=1.21.3")
    Object newInstance(String assetName,
                       @Type(clazz = HolderProxy.class) Object ingredient,
                       float itemModelIndex,
                       Map<Object, String> overrideArmorAssets,
                       @Type(clazz = ComponentProxy.class) Object description);
}
