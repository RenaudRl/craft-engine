package net.momirealms.craftengine.bukkit.block.behavior;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MBuiltInRegistries;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Abstract base behavior for blocks that react to redstone signals and have a
 * facing direction.
 * This serves as the foundation for blocks like pickaxe blocks and placement
 * blocks that
 * perform actions in a specific direction when triggered by redstone.
 */
public abstract class FacingTriggerableBlockBehavior extends BukkitBlockBehavior {

    /**
     * Default blacklist of blocks that cannot be affected by triggerable behaviors.
     * Includes bedrock, command blocks, portals, and other indestructible/special
     * blocks.
     */
    protected static final Set<Key> DEFAULT_BLACKLIST_BLOCKS = ObjectOpenHashSet.of(
            Key.of("minecraft:bedrock"),
            Key.of("minecraft:end_portal_frame"),
            Key.of("minecraft:end_portal"),
            Key.of("minecraft:nether_portal"),
            Key.of("minecraft:barrier"),
            Key.of("minecraft:command_block"),
            Key.of("minecraft:chain_command_block"),
            Key.of("minecraft:repeating_command_block"),
            Key.of("minecraft:structure_block"),
            Key.of("minecraft:end_gateway"),
            Key.of("minecraft:jigsaw"),
            Key.of("minecraft:structure_void"),
            Key.of("minecraft:moving_piston"),
            Key.of("minecraft:light"));

    protected final Property<Direction> facingProperty;
    protected final Property<Boolean> triggeredProperty;
    protected final Set<Key> blocks;
    protected final boolean whitelistMode;

    public FacingTriggerableBlockBehavior(CustomBlock customBlock, Property<Direction> facing,
            Property<Boolean> triggered, Set<Key> blocks, boolean whitelistMode) {
        super(customBlock);
        this.facingProperty = facing;
        this.triggeredProperty = triggered;
        this.blocks = blocks;
        this.whitelistMode = whitelistMode;
    }

    @Override
    public void neighborChanged(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        Object state = args[0];
        Object level = args[1];
        Object pos = args[2];
        boolean hasNeighborSignal = FastNMS.INSTANCE.method$SignalGetter$hasNeighborSignal(level, pos);
        ImmutableBlockState blockState = BukkitBlockManager.instance()
                .getImmutableBlockState(BlockStateUtils.blockStateToId(state));
        if (blockState == null || blockState.isEmpty())
            return;
        boolean triggeredValue = blockState.get(this.triggeredProperty);
        if (hasNeighborSignal && !triggeredValue) {
            Object tickState = blockState.with(this.triggeredProperty, true).customBlockState().literalObject();
            FastNMS.INSTANCE.method$LevelWriter$setBlock(level, pos, tickState, 2);
            FastNMS.INSTANCE.method$ScheduledTickAccess$scheduleBlockTick(level, pos,
                    BlockStateUtils.getBlockOwner(tickState), 1);
        } else if (!hasNeighborSignal && triggeredValue) {
            FastNMS.INSTANCE.method$LevelWriter$setBlock(level, pos,
                    blockState.with(this.triggeredProperty, false).customBlockState().literalObject(), 2);
        }
    }

    @Override
    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        Direction direction = DirectionUtils.fromNMSDirection(
                FastNMS.INSTANCE.method$Direction$getOpposite(
                        DirectionUtils.toNMSDirection(context.getNearestLookingDirection())));
        return state.owner().value().defaultState().with(this.facingProperty, direction);
    }

    /**
     * Check if a block state passes the whitelist/blacklist filter.
     * 
     * @param blockState The NMS block state to check
     * @return true if the block is allowed to be affected
     */
    protected boolean blockCheckByBlockState(Object blockState) {
        if (blockState == null || FastNMS.INSTANCE.method$BlockStateBase$isAir(blockState)) {
            return false;
        }
        Key blockId = Optional
                .ofNullable(BukkitBlockManager.instance()
                        .getImmutableBlockState(BlockStateUtils.blockStateToId(blockState)))
                .filter(state -> !state.isEmpty())
                .map(state -> state.owner().value().id())
                .orElseGet(() -> KeyUtils.resourceLocationToKey(FastNMS.INSTANCE.method$Registry$getKey(
                        MBuiltInRegistries.BLOCK, FastNMS.INSTANCE.method$BlockState$getBlock(blockState))));
        return blockCheckByKey(blockId);
    }

    /**
     * Check if a block key passes the whitelist/blacklist filter.
     * 
     * @param blockId The block key to check
     * @return true if the block is allowed to be affected
     */
    protected boolean blockCheckByKey(Key blockId) {
        return this.blocks.contains(blockId) == this.whitelistMode;
    }

    /**
     * Called when the block should perform its action.
     * 
     * @param state The current block state
     * @param level The world/level
     * @param pos   The block position
     */
    protected abstract void tick(Object state, Object level, Object pos);
}
