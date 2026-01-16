package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.world.BlockPos;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * A block behavior that force-loads the chunk where the block is placed.
 * Useful for creating chunk loaders that keep areas of the world active.
 * 
 * Note: If the block is destroyed by anything other than a player (explosion,
 * etc.),
 * the chunk will remain force-loaded until server restart.
 * 
 * Configuration example:
 * 
 * <pre>
 * behaviors: craftengine: chunk_loader_block: {
 * }
 * </pre>
 */
public class ChunkLoaderBlockBehavior extends BukkitBlockBehavior {
    public static final Factory FACTORY = new Factory();

    public ChunkLoaderBlockBehavior(CustomBlock customBlock) {
        super(customBlock);
    }

    @Override
    public void placeMultiState(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        if (!CoreReflections.clazz$ServerLevel.isInstance(args[0]))
            return;
        BlockPos pos = LocationUtils.fromBlockPos(args[1]);
        int blockX = pos.x();
        int blockZ = pos.z();
        updateChunkForced(args[0], blockX, blockZ, true);
    }

    @Override
    public Object playerWillDestroy(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object serverLevel = args[0];
        int blockX = FastNMS.INSTANCE.field$Vec3i$x(args[1]);
        int blockZ = FastNMS.INSTANCE.field$Vec3i$z(args[1]);
        updateChunkForced(serverLevel, blockX, blockZ, false);
        return super.playerWillDestroy(thisBlock, args, superMethod);
    }

    private static void updateChunkForced(Object serverLevel, int blockX, int blockZ, boolean add) {
        if (!CoreReflections.clazz$ServerLevel.isInstance(serverLevel))
            return;
        FastNMS.INSTANCE.method$ServerLevel$setChunkForced(serverLevel, blockX >> 4, blockZ >> 4, add);
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            return new ChunkLoaderBlockBehavior(block);
        }
    }
}
