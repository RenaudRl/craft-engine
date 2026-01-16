package net.momirealms.craftengine.bukkit.block.behavior;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.item.behavior.BlockItemBehavior;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.bukkit.world.BukkitWorldManager;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.world.BlockHitResult;
import net.momirealms.craftengine.core.world.BlockPos;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A block behavior that places items from a container behind it when powered by
 * redstone.
 * This can place both vanilla blocks and custom CraftEngine blocks.
 * Items that cannot be placed as blocks will be dropped as entities in front of
 * the block.
 * 
 * Configuration example:
 * 
 * <pre>
 * behaviors:
 *   craftengine:place_block:
 *     whitelist: false
 *     blocks:
 *       - minecraft:tnt
 * </pre>
 */
public class PlaceBlockBehavior extends FacingTriggerableBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private static final Logger LOGGER = Logger.getLogger(PlaceBlockBehavior.class.getName());

    public PlaceBlockBehavior(CustomBlock customBlock, Property<Direction> facing, Property<Boolean> triggered,
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
    public void tick(Object state, Object level, Object nmsBlockPos) {
        BlockPos pos = LocationUtils.fromBlockPos(nmsBlockPos);
        ImmutableBlockState blockState = BukkitBlockManager.instance()
                .getImmutableBlockState(BlockStateUtils.blockStateToId(state));
        if (blockState == null || blockState.isEmpty())
            return;

        Direction direction = blockState.get(this.facingProperty);
        Direction opposite = direction.opposite();
        BlockPos containerPos = pos.relative(opposite);
        BlockPos targetPos = pos.relative(direction);

        try {
            placeItemFromContainer(level, containerPos, targetPos, opposite);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error in PlaceBlockBehavior tick", e);
        }
    }

    /**
     * Attempts to take an item from a container and place it as a block.
     * Uses Bukkit API for container access.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void placeItemFromContainer(Object level, BlockPos containerPos, BlockPos targetPos,
            Direction placeDirection) {
        // Check if target position is empty
        Object targetBlockState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(level,
                LocationUtils.toBlockPos(targetPos));
        if (!FastNMS.INSTANCE.method$BlockStateBase$isAir(targetBlockState)) {
            return;
        }

        // Get the Bukkit world and container block
        org.bukkit.World bukkitWorld = FastNMS.INSTANCE.method$Level$getCraftWorld(level);
        Block containerBlock = bukkitWorld.getBlockAt(containerPos.x(), containerPos.y(), containerPos.z());
        BlockState blockState = containerBlock.getState();

        if (!(blockState instanceof Container container)) {
            return;
        }

        Inventory inventory = container.getInventory();

        // Find the first non-empty slot
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack itemStack = inventory.getItem(slot);
            if (itemStack == null || itemStack.getType().isAir()) {
                continue;
            }

            Item<ItemStack> item = BukkitItemManager.instance().wrap(itemStack);

            // Try custom item placement
            Optional<CustomItem<ItemStack>> optionalCustomItem = item.getCustomItem();
            if (optionalCustomItem.isPresent()) {
                CustomItem<ItemStack> customItem = optionalCustomItem.get();
                for (ItemBehavior itemBehavior : customItem.behaviors()) {
                    if (itemBehavior instanceof BlockItemBehavior blockItemBehavior) {
                        if (!blockCheckByKey(blockItemBehavior.block()))
                            continue;

                        BlockHitResult hitResult = new BlockHitResult(
                                LocationUtils.toVec3d(targetPos),
                                placeDirection,
                                targetPos,
                                false);

                        BlockPlaceContext context = new BlockPlaceContext(
                                BukkitWorldManager.instance().wrap(bukkitWorld),
                                null,
                                InteractionHand.MAIN_HAND,
                                item,
                                hitResult);

                        InteractionResult result = blockItemBehavior.place(context);
                        if (result.success()) {
                            itemStack.setAmount(itemStack.getAmount() - 1);
                            inventory.setItem(slot, itemStack);
                            return;
                        }
                    }
                }
            }

            // Try vanilla block placement
            if (itemStack.getType().isBlock()) {
                org.bukkit.Material blockType = itemStack.getType();
                Key blockKey = Key.of("minecraft:" + blockType.name().toLowerCase());

                if (blockCheckByKey(blockKey)) {
                    Block targetBlock = bukkitWorld.getBlockAt(targetPos.x(), targetPos.y(), targetPos.z());
                    targetBlock.setType(blockType);

                    itemStack.setAmount(itemStack.getAmount() - 1);
                    inventory.setItem(slot, itemStack);
                    return;
                }
            }
        }
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        @SuppressWarnings({ "unchecked" })
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            Property<Direction> facing = (Property<Direction>) block.getProperty("facing");
            if (facing == null) {
                throw new IllegalArgumentException("Block '" + block.id()
                        + "' with 'craftengine:place_block' behavior is missing the required 'facing' property");
            }
            Property<Boolean> triggered = (Property<Boolean>) block.getProperty("triggered");
            if (triggered == null) {
                throw new IllegalArgumentException("Block '" + block.id()
                        + "' with 'craftengine:place_block' behavior is missing the required 'triggered' property");
            }
            boolean whitelistMode = (boolean) arguments.getOrDefault("whitelist", false);
            Set<Key> blocks = MiscUtils.getAsStringList(arguments.get("blocks")).stream().map(Key::of)
                    .collect(Collectors.toCollection(ObjectOpenHashSet::new));
            if (blocks.isEmpty() && !whitelistMode) {
                blocks = FacingTriggerableBlockBehavior.DEFAULT_BLACKLIST_BLOCKS;
            }
            return new PlaceBlockBehavior(block, facing, triggered, blocks, whitelistMode);
        }
    }
}
