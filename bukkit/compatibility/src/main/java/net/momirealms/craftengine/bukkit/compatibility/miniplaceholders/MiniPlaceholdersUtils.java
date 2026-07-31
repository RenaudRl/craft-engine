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
import net.momirealms.craftengine.core.util.AdventureHelper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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

    /** Nested jar holding the unrelocated half of the hook. */
    private static final String BRIDGE_JAR = "miniplaceholders.jarinjar";

    private static volatile MiniPlaceholdersBridge bridge;
    @SuppressWarnings("unused") // kept alive for as long as the classes it defined are in use
    private static URLClassLoader bridgeClassLoader;

    /** Loads the bridge out of the nested jar, instantiates it and registers the expansions. */
    public static void registerExpansions(BukkitCraftEngine plugin) throws Exception {
        ClassLoader loader = loadBridgeJar(plugin);
        MiniPlaceholdersBridge loaded = (MiniPlaceholdersBridge) Class
                .forName(MiniPlaceholdersBridge.IMPLEMENTATION, true, loader)
                .getDeclaredConstructor()
                .newInstance();
        loaded.registerExpansions(plugin);
        bridge = loaded;
    }

    /**
     * Extracts the nested jar and opens a loader for it whose <b>parent is this plugin's own
     * classloader</b>.
     *
     * <p>That parent is the whole point, and the reason the dependency manager cannot be used here
     * the way {@code proxy.jarinjar} uses it. Its appenders hand jars either to the server
     * classloader or to Paper's library loader, and neither can see plugin classes: defining
     * {@code MiniPlaceholdersBridgeImpl} there fails on its own super-interface, and even without
     * one every call back into CraftEngine would fail to link. The proxy gets away with it because
     * it only ever talks to the server.</p>
     *
     * <p>Parent-first delegation then resolves each side from where it actually lives: the bridge
     * interface and CraftEngine's API from the plugin jar, the MiniPlaceholders API from that
     * plugin (see the {@code MiniPlaceholders} entry in the plugin descriptor), and the server's
     * unrelocated Adventure from the server — the plugin jar holds no {@code net.kyori} class of
     * its own, they are all relocated.</p>
     */
    private static ClassLoader loadBridgeJar(BukkitCraftEngine plugin) throws IOException {
        // Same folder as the downloaded dependencies, which is wiped of loose jars on every boot,
        // so a stale copy can never outlive the plugin jar it was extracted from.
        Path jar = plugin.dataFolderPath().resolve("libs").resolve("miniplaceholders-bridge.jar");
        Files.createDirectories(jar.getParent());
        try (InputStream in = MiniPlaceholdersUtils.class.getClassLoader().getResourceAsStream(BRIDGE_JAR)) {
            if (in == null) {
                throw new FileNotFoundException(BRIDGE_JAR + " is missing from the plugin jar");
            }
            Files.copy(in, jar, StandardCopyOption.REPLACE_EXISTING);
        }
        URLClassLoader loader = new URLClassLoader(
                new URL[]{jar.toUri().toURL()},
                MiniPlaceholdersUtils.class.getClassLoader()
        );
        bridgeClassLoader = loader;
        return loader;
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
