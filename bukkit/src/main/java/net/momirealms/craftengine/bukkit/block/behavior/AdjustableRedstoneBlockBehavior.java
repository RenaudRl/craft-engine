package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.UpdateOption;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.IntegerProperty;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.item.context.UseOnContext;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * A block behavior that allows a block to emit an adjustable redstone signal.
 * Players can click on the block to increase the signal level (or shift+click
 * to decrease).
 * The block requires a "power" IntegerProperty to store the current signal
 * level (0-15).
 * 
 * Configuration example:
 * 
 * <pre>
 * behaviors: craftengine: adjustable_redstone_block: {
 * }
 * </pre>
 */
public class AdjustableRedstoneBlockBehavior extends BukkitBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final IntegerProperty powerProperty;

    public AdjustableRedstoneBlockBehavior(CustomBlock customBlock, IntegerProperty power) {
        super(customBlock);
        this.powerProperty = power;
    }

    @Override
    public InteractionResult useWithoutItem(UseOnContext context, ImmutableBlockState state) {
        int power = state.get(this.powerProperty);
        if (context.isSecondaryUseActive()) {
            // Shift+click decreases power
            if (power - 1 < this.powerProperty.min) {
                power = this.powerProperty.max;
            } else {
                power--;
            }
        } else {
            // Normal click increases power
            if (power + 1 > this.powerProperty.max) {
                power = this.powerProperty.min;
            } else {
                power++;
            }
        }
        FastNMS.INSTANCE.method$LevelWriter$setBlock(
                context.getLevel().serverWorld(),
                LocationUtils.toBlockPos(context.getClickedPos()),
                state.with(this.powerProperty, power).customBlockState().literalObject(),
                UpdateOption.UPDATE_ALL.flags());
        return InteractionResult.SUCCESS_AND_CANCEL;
    }

    @Override
    public boolean isSignalSource(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        return true;
    }

    public int getSignal(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        ImmutableBlockState state = BukkitBlockManager.instance()
                .getImmutableBlockState(BlockStateUtils.blockStateToId(args[0]));
        if (state == null || state.isEmpty()) {
            return 0;
        }
        return state.get(this.powerProperty);
    }

    public int getDirectSignal(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        ImmutableBlockState state = BukkitBlockManager.instance()
                .getImmutableBlockState(BlockStateUtils.blockStateToId(args[0]));
        if (state == null || state.isEmpty()) {
            return 0;
        }
        return state.get(this.powerProperty);
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            IntegerProperty power = (IntegerProperty) block.getProperty("power");
            if (power == null) {
                throw new IllegalArgumentException("Block '" + block.id()
                        + "' with 'craftengine:adjustable_redstone_block' behavior is missing the required 'power' property");
            }
            return new AdjustableRedstoneBlockBehavior(block, power);
        }
    }
}
