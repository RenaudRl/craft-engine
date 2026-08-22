package net.momirealms.craftengine.bukkit.placeholder.miniplaceholders;

import io.github.miniplaceholders.api.Expansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.plugin.context.CooldownData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * The {@code ce} expansion: {@code <ce_cooldown:key>}.
 *
 * <p>Replaces the PlaceholderAPI form {@code %ce_cooldown_key%}. The dynamic segment is now a tag
 * argument rather than a suffix split on {@code _}, so cooldown keys containing an underscore
 * resolve correctly — they did not before.</p>
 */
public final class CraftEngineExpansion {
    private CraftEngineExpansion() {}

    public static Expansion create() {
        return Expansion.builder("ce")
                .author("XiaoMoMi")
                .version("1.0")
                .audiencePlaceholder(Player.class, "cooldown", (player, queue, ctx) -> {
                    if (!queue.hasNext()) return null;
                    String remaining = getCooldown(BukkitAdaptor.adapt(player), queue.pop().value());
                    return Tag.selfClosingInserting(Component.text(remaining == null ? "0" : remaining));
                })
                .build();
    }

    @Nullable
    private static String getCooldown(BukkitServerPlayer player, String key) {
        if (player == null) {
            return null;
        }
        CooldownData cooldown = player.cooldown();
        if (cooldown == null) {
            return null;
        }
        Long ms = cooldown.getExpirationTime(key);
        if (ms == null) {
            return null;
        }
        ms -= System.currentTimeMillis();
        if (ms < 0) {
            return null;
        }
        return String.valueOf(ms);
    }
}
