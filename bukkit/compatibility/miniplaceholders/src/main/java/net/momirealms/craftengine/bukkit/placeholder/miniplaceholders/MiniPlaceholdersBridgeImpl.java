package net.momirealms.craftengine.bukkit.placeholder.miniplaceholders;

import io.github.miniplaceholders.api.Expansion;
import io.github.miniplaceholders.api.MiniPlaceholders;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.placeholder.MiniPlaceholdersBridge;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The unrelocated half of {@link MiniPlaceholdersBridge}: everything here runs against the server's
 * real Adventure, so it may freely build expansions, hold audiences and parse MiniMessage.
 *
 * <p>It may call into CraftEngine — those classes are not relocated either — but only through
 * methods whose signatures carry no Adventure type. The expansions in this package stick to
 * {@code String}, {@code int}, {@code Key} and {@code Item}, which is what makes the split
 * workable.</p>
 */
public final class MiniPlaceholdersBridgeImpl implements MiniPlaceholdersBridge {
    private List<Expansion> expansions = List.of();

    @Override
    public void registerExpansions(BukkitCraftEngine plugin) {
        this.expansions.stream().filter(Expansion::registered).forEach(Expansion::unregister);
        List<Expansion> built = List.of(
                CraftEngineExpansion.create(),
                ImageExpansion.create(plugin),
                ShiftExpansion.create(plugin),
                CheckItemExpansion.create()
        );
        built.forEach(Expansion::register);
        this.expansions = built;
    }

    @Override
    public boolean isPlaceholderTag(String name) {
        return MiniPlaceholders.audienceGlobalPlaceholders().has(name);
    }

    @Override
    public String renderToJson(String tag, List<String> arguments, @Nullable Player viewer) {
        // The audience must reach `deserialize`: MiniMessage binds audience-scoped placeholders to
        // the target, not to the resolvers, so passing only the resolver would leave every
        // player-dependent tag empty.
        Component component = MiniMessage.miniMessage().deserialize(
                toMiniMessage(tag, arguments),
                viewer == null ? Audience.empty() : viewer,
                MiniPlaceholders.audienceGlobalPlaceholders()
        );
        return GsonComponentSerializer.gson().serialize(component);
    }

    /**
     * Rebuilds the tag as MiniMessage source, quoting every argument.
     *
     * <p>Arguments are arbitrary values from the caller's own text, so they may well contain
     * {@code :}, {@code >} or a quote; unquoted they would split the tag or end it early.</p>
     */
    private static String toMiniMessage(String tag, List<String> arguments) {
        StringBuilder builder = new StringBuilder("<").append(tag);
        for (String argument : arguments) {
            builder.append(":'")
                    .append(argument.replace("\\", "\\\\").replace("'", "\\'"))
                    .append('\'');
        }
        return builder.append('>').toString();
    }
}
