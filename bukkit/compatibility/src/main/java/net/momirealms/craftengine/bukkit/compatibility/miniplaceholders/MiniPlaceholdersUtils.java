package net.momirealms.craftengine.bukkit.compatibility.miniplaceholders;

import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.placeholder.MiniPlaceholdersBridge;
import net.momirealms.craftengine.core.plugin.compatibility.TagResolverProvider;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.PlayerContext;
import net.momirealms.craftengine.core.plugin.dependency.Dependencies;
import net.momirealms.craftengine.core.util.AdventureHelper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * CraftEngine's entry point to MiniPlaceholders, replacing the PlaceholderAPI hook.
 *
 * <p>This class sits on the relocated side, so it never touches the MiniPlaceholders API itself:
 * everything goes through {@link MiniPlaceholdersBridge}, whose implementation is loaded from a
 * nested, unrelocated jar. See that interface for why the split is unavoidable.</p>
 *
 * <p>Two directions, both simpler than under PlaceholderAPI:</p>
 * <ul>
 *   <li><b>Producing</b> — the four expansions ({@code ce}, {@code image}, {@code shift},
 *       {@code checkceitem}) are registered by the bridge instead of through PlaceholderAPI.</li>
 *   <li><b>Consuming</b> — the bespoke {@code <papi:…>}, {@code <rel_papi:…>} and
 *       {@code <viewer_papi:…>} tags are gone. Every MiniPlaceholders tag is answered by the
 *       resolvers below, which delegate the actual parse across the boundary and rebuild the
 *       resulting component on this side from JSON.</li>
 * </ul>
 */
public final class MiniPlaceholdersUtils {
    private MiniPlaceholdersUtils() {}

    private static volatile MiniPlaceholdersBridge bridge;

    /**
     * Extracts the bridge jar into the plugin classloader, instantiates it and registers the
     * expansions.
     *
     * <p>The classloader matters: the jar is added to the CraftEngine plugin's own loader, which is
     * where both the server's Adventure and — thanks to the {@code MiniPlaceholders} entry in
     * {@code paper-plugin.yml} — the MiniPlaceholders API are visible.</p>
     */
    public static void registerExpansions(BukkitCraftEngine plugin) throws ReflectiveOperationException {
        plugin.dependencyManager().loadDependencies(List.of(Dependencies.CRAFT_ENGINE_MINIPLACEHOLDERS));
        MiniPlaceholdersBridge loaded = (MiniPlaceholdersBridge) Class
                .forName(MiniPlaceholdersBridge.IMPLEMENTATION, true, MiniPlaceholdersUtils.class.getClassLoader())
                .getDeclaredConstructor()
                .newInstance();
        loaded.registerExpansions(plugin);
        bridge = loaded;
    }

    /**
     * A resolver answering for every MiniPlaceholders tag currently registered, with no viewer.
     *
     * <p>Used to tell CraftEngine which lines carry dynamic tags. Names cannot be enumerated up
     * front — each installed expansion contributes its own — so the resolver's {@code has(name)} is
     * the authority.</p>
     */
    public static TagResolver resolvers() {
        return bridgedResolver(null);
    }

    /** Whether {@code name} is a tag some registered MiniPlaceholders expansion can resolve. */
    public static boolean isPlaceholderTag(String name) {
        MiniPlaceholdersBridge current = bridge;
        return current != null && current.isPlaceholderTag(name);
    }

    /**
     * A resolver that hands each claimed tag to the bridge and deserializes the JSON it gets back.
     *
     * <p>The component is inserted rather than pre-processed: it crosses the boundary already
     * parsed, so it carries its own style instead of inheriting the surrounding one. That is the
     * one visible difference from a native MiniPlaceholders parse.</p>
     */
    private static TagResolver bridgedResolver(@Nullable Player viewer) {
        return new TagResolver() {

            @Override
            public boolean has(@NotNull String name) {
                return isPlaceholderTag(name);
            }

            @Override
            public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments,
                                         @NotNull net.kyori.adventure.text.minimessage.Context ctx) throws ParsingException {
                MiniPlaceholdersBridge current = bridge;
                if (current == null) return null;
                List<String> values = new ArrayList<>();
                while (arguments.hasNext()) {
                    values.add(arguments.pop().value());
                }
                return Tag.selfClosingInserting(AdventureHelper.jsonToComponent(current.renderToJson(name, values, viewer)));
            }
        };
    }

    /**
     * Feeds every registered MiniPlaceholders tag into CraftEngine's own resolver chain.
     *
     * <p>Audience-scoped placeholders read the viewer bound at parse time, which is why the viewer
     * is pulled out of the context here. {@code Context#audience()} is of no use across the
     * boundary — it yields a <em>relocated</em> {@code Pointered} — so the platform player is taken
     * from {@link PlayerContext#player()} instead.</p>
     */
    public static final class Provider implements TagResolverProvider {

        @Override
        public String name() {
            return "miniplaceholders";
        }

        @Override
        public TagResolver getTagResolver(Context context) {
            return bridgedResolver(viewerOf(context));
        }

        @Nullable
        private static Player viewerOf(Context context) {
            if (!(context instanceof PlayerContext playerContext)) return null;
            net.momirealms.craftengine.core.entity.player.Player player = playerContext.player();
            if (player == null) return null;
            return player.platformPlayer() instanceof Player bukkitPlayer ? bukkitPlayer : null;
        }
    }
}
