package net.momirealms.craftengine.bukkit.block.behavior;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * A block behavior that breaks the block in its facing direction when powered
 * by redstone.
 * Useful for creating automated mining/breaking systems with custom blocks.
 * 
 * Configuration example:
 * 
 * <pre>
 * behaviors:
 *   craftengine:pickaxe_block:
 *     whitelist: false
 *     blocks:
 *       - minecraft:bedrock
 *       - minecraft:obsidian
 * </pre>
 */
public class PickaxeBlockBehavior extends FacingTriggerableBlockBehavior {
    public static final Factory FACTORY = new Factory();

    public PickaxeBlockBehavior(CustomBlock customBlock, Property<Direction> facing, Property<Boolean> triggered,
            Set<Key> blocks, boolean whitelistMode) {
        super(customBlock, facing, triggered, blocks, whitelistMode);
    }

    @Override
    public void tick(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        Object state = args[0];
        Object level = args[1];
        Object pos = args[2];
        tick(state, level, pos);
    }

    @Override
    public void tick(Object state, Object level, Object pos) {
        ImmutableBlockState blockState = BukkitBlockManager.instance()
                .getImmutableBlockState(BlockStateUtils.blockStateToId(state));
        if (blockState == null || blockState.isEmpty())
            return;
        Object breakPos = FastNMS.INSTANCE.method$BlockPos$relative(pos,
                DirectionUtils.toNMSDirection(blockState.get(this.facingProperty)));
        Object breakState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(level, breakPos);
        if (blockCheckByBlockState(breakState)) {
            FastNMS.INSTANCE.method$LevelWriter$destroyBlock(level, breakPos, true);
        }
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        @SuppressWarnings({ "unchecked" })
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            Property<Direction> facing = (Property<Direction>) block.getProperty("facing");
            if (facing == null) {
                throw new IllegalArgumentException("Block '" + block.id()
                        + "' with 'craftengine:pickaxe_block' behavior is missing the required 'facing' property");
            }
            Property<Boolean> triggered = (Property<Boolean>) block.getProperty("triggered");
            if (triggered == null) {
                throw new IllegalArgumentException("Block '" + block.id()
                        + "' with 'craftengine:pickaxe_block' behavior is missing the required 'triggered' property");
            }
            boolean whitelistMode = (boolean) arguments.getOrDefault("whitelist", false);
            Set<Key> blocks = MiscUtils.getAsStringList(arguments.get("blocks")).stream().map(Key::of)
                    .collect(Collectors.toCollection(ObjectOpenHashSet::new));
            if (blocks.isEmpty() && !whitelistMode) {
                blocks = FacingTriggerableBlockBehavior.DEFAULT_BLACKLIST_BLOCKS;
            }
            return new PickaxeBlockBehavior(block, facing, triggered, blocks, whitelistMode);
        }
    }
}
