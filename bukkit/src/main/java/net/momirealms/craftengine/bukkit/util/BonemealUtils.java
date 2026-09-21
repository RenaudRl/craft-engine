package net.momirealms.craftengine.bukkit.util;

import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.BonemealSourceProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.BonemealableBlockProxy;
import net.momirealms.sparrow.reflection.clazz.SparrowClass;

/**
 * One place that knows which {@code BonemealableBlock} signature the running server has.
 *
 * <p>Minecraft 26.3 appended a {@code BonemealSource} parameter to {@code isValidBonemealTarget},
 * {@code isBonemealSuccess} and {@code performBonemeal}; 1.20.2 had dropped the trailing
 * {@code isClient} boolean. Every call CraftEngine makes on our own behalf is an interaction, so
 * {@link #INTERACTION} is the only source ever passed here.
 */
public final class BonemealUtils {
    public static final Class<?> clazz$BonemealSource = VersionHelper.isOrAbove26_3
            ? SparrowClass.find("net.minecraft.world.level.block.BonemealSource")
            : null;
    // Declared first in the enum: INTERACTION, then MOB.
    public static final Object INTERACTION = VersionHelper.isOrAbove26_3
            ? BonemealSourceProxy.INSTANCE.values()[0]
            : null;

    private BonemealUtils() {
    }

    public static boolean isValidBonemealTarget(Object block, Object levelReader, Object pos, Object state) {
        if (VersionHelper.isOrAbove26_3) {
            return BonemealableBlockProxy.INSTANCE.isValidBonemealTarget(block, levelReader, pos, state, INTERACTION);
        }
        if (VersionHelper.isOrAbove1_20_2) {
            return BonemealableBlockProxy.INSTANCE.isValidBonemealTarget(block, levelReader, pos, state);
        }
        return BonemealableBlockProxy.INSTANCE.isValidBonemealTarget(block, levelReader, pos, state, true);
    }

    public static void performBonemeal(Object block, Object serverLevel, Object random, Object pos, Object state) {
        if (VersionHelper.isOrAbove26_3) {
            BonemealableBlockProxy.INSTANCE.performBonemeal(block, serverLevel, random, pos, state, INTERACTION);
        } else {
            BonemealableBlockProxy.INSTANCE.performBonemeal(block, serverLevel, random, pos, state);
        }
    }
}
