package net.momirealms.craftengine.proxy.minecraft.world.entity.player;

import net.momirealms.craftengine.proxy.minecraft.world.ContainerProxy;
import net.momirealms.craftengine.proxy.minecraft.world.item.ItemStackProxy;
import net.momirealms.sparrow.reflection.SReflection;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;
import net.momirealms.sparrow.reflection.proxy.annotation.Type;

import java.util.function.Predicate;

@ReflectionProxy(name = "net.minecraft.world.entity.player.Inventory")
public interface InventoryProxy {
    InventoryProxy INSTANCE = ASMProxyFactory.create(InventoryProxy.class);
    boolean IS_26_3 = SReflection.getFilter().test("min_version=26.3");

    /** Version-agnostic entry point: vanilla derives {@code countingOnly} from {@code maxCount == 0}. */
    static int clearOrCount(Object inventory, Predicate<Object> shouldRemove, int maxCount, Object craftingInventory) {
        return IS_26_3
                ? INSTANCE.clearOrCountMatchingItems(inventory, shouldRemove, maxCount == 0, maxCount, craftingInventory)
                : INSTANCE.clearOrCountMatchingItems(inventory, shouldRemove, maxCount, craftingInventory);
    }

    @MethodInvoker(name = "clearOrCountMatchingItems", activeIf = "max_version=26.2")
    int clearOrCountMatchingItems(Object target, Predicate<Object> shouldRemove, int maxCount, @Type(clazz = ContainerProxy.class) Object craftingInventory);

    // 26.3: "count only" is an explicit flag instead of maxCount == 0.
    @MethodInvoker(name = "clearOrCountMatchingItems", activeIf = "min_version=26.3")
    int clearOrCountMatchingItems(Object target, Predicate<Object> shouldRemove, boolean countingOnly, int maxCount, @Type(clazz = ContainerProxy.class) Object craftingInventory);

    @MethodInvoker(name = "add")
    boolean add(Object target, @Type(clazz = ItemStackProxy.class) Object itemStack);
}
