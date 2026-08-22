package net.momirealms.craftengine.bukkit.placeholder.miniplaceholders;

import io.github.miniplaceholders.api.Expansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * The {@code checkceitem} expansion, replacing {@code %checkceitem_…%}:
 *
 * <ul>
 *   <li>{@code <checkceitem_count:namespace:path>}</li>
 *   <li>{@code <checkceitem_has:namespace:path:amount>} — {@code amount} defaults to 1</li>
 *   <li>{@code <checkceitem_id:main_hand|off_hand|slot>} — defaults to the main hand</li>
 *   <li>{@code <checkceitem_iscustom:main_hand|off_hand|slot>}</li>
 * </ul>
 */
public final class CheckItemExpansion {
    private CheckItemExpansion() {}

    public static Expansion create() {
        return Expansion.builder("checkceitem")
                .author("jhqwqmc")
                .version("1.0")
                .audiencePlaceholder(Player.class, "count", (player, queue, ctx) -> {
                    BukkitServerPlayer adapted = adapt(player);
                    Key key = popKey(queue);
                    if (adapted == null || key == null) return null;
                    return text(String.valueOf(count(adapted, key)));
                })
                .audiencePlaceholder(Player.class, "has", (player, queue, ctx) -> {
                    BukkitServerPlayer adapted = adapt(player);
                    Key key = popKey(queue);
                    if (adapted == null || key == null) return null;
                    int required = 1;
                    if (queue.hasNext()) {
                        try {
                            required = Integer.parseInt(queue.pop().value());
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    }
                    if (required < 1) return text("true");
                    return text(String.valueOf(count(adapted, key) >= required));
                })
                .audiencePlaceholder(Player.class, "id", (player, queue, ctx) -> {
                    Item item = itemOf(adapt(player), queue);
                    return item == null ? null : text(item.id().asString());
                })
                .audiencePlaceholder(Player.class, "iscustom", (player, queue, ctx) -> {
                    Item item = itemOf(adapt(player), queue);
                    return item == null ? null : text(String.valueOf(item.isCustomItem()));
                })
                .build();
    }

    private static Tag text(String value) {
        return Tag.selfClosingInserting(Component.text(value));
    }

    @Nullable
    private static BukkitServerPlayer adapt(Player player) {
        return player == null ? null : BukkitAdaptor.adapt(player);
    }

    /** Pops the {@code namespace} and {@code path} pair identifying a CraftEngine item. */
    @Nullable
    private static Key popKey(ArgumentQueue queue) {
        if (!queue.hasNext()) return null;
        String namespace = queue.pop().value();
        if (!queue.hasNext()) return null;
        String path = queue.pop().value();
        try {
            return Key.of(namespace, path);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Resolves the inspected slot, defaulting to the main hand when no argument is given. */
    @Nullable
    private static Item itemOf(BukkitServerPlayer player, ArgumentQueue queue) {
        if (player == null) return null;
        if (!queue.hasNext()) return player.getItemInHand(InteractionHand.MAIN_HAND);
        String slot = queue.pop().value();
        return switch (slot) {
            case "", "main_hand" -> player.getItemInHand(InteractionHand.MAIN_HAND);
            case "off_hand" -> player.getItemInHand(InteractionHand.OFF_HAND);
            default -> {
                try {
                    yield player.getItemBySlot(Math.max(Integer.parseInt(slot), 0));
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
        };
    }

    private static int count(BukkitServerPlayer player, Key key) {
        return player.clearOrCountMatchingInventoryItems(key, 0);
    }
}
