package net.momirealms.craftengine.bukkit.compatibility.miniplaceholders;

import io.github.miniplaceholders.api.Expansion;
import io.github.miniplaceholders.api.MiniPlaceholders;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.plugin.compatibility.TagResolverProvider;
import net.momirealms.craftengine.core.plugin.context.Context;

import java.util.List;

/**
 * CraftEngine's entry point to MiniPlaceholders, replacing the PlaceholderAPI hook.
 *
 * <p>Two directions, and the migration simplifies both:</p>
 * <ul>
 *   <li><b>Producing</b> — the four expansions ({@code ce}, {@code image}, {@code shift},
 *       {@code checkceitem}) are registered here instead of through PlaceholderAPI.</li>
 *   <li><b>Consuming</b> — the bespoke {@code <papi:…>}, {@code <rel_papi:…>} and
 *       {@code <viewer_papi:…>} tags are gone. MiniPlaceholders tags resolve natively during the
 *       MiniMessage parse, so a bridge tag has nothing left to do.</li>
 * </ul>
 */
public final class MiniPlaceholdersUtils {
    private MiniPlaceholdersUtils() {}

    private static List<Expansion> expansions = List.of();

    public static void registerExpansions(BukkitCraftEngine plugin) {
        unregisterExpansions();
        List<Expansion> built = List.of(
                CraftEngineExpansion.create(),
                ImageExpansion.create(plugin),
                ShiftExpansion.create(plugin),
                CheckItemExpansion.create()
        );
        built.forEach(Expansion::register);
        expansions = built;
    }

    public static void unregisterExpansions() {
        expansions.stream().filter(Expansion::registered).forEach(Expansion::unregister);
        expansions = List.of();
    }

    /**
     * A resolver answering for every MiniPlaceholders tag currently registered.
     *
     * <p>Used to tell CraftEngine which tags are viewer-dependent. Names cannot be enumerated up
     * front — each installed expansion contributes its own — so the resolver's own
     * {@code has(name)} is the authority.</p>
     */
    public static TagResolver resolvers() {
        return MiniPlaceholders.audienceGlobalPlaceholders();
    }

    /** Whether {@code name} is a tag some registered MiniPlaceholders expansion can resolve. */
    public static boolean isPlaceholderTag(String name) {
        return MiniPlaceholders.audienceGlobalPlaceholders().has(name);
    }

    /**
     * Feeds every registered MiniPlaceholders tag into CraftEngine's own resolver chain.
     *
     * <p>Audience-scoped placeholders read the target bound at {@code deserialize} time, which is
     * why the render path passes the viewing player along — see {@code Context#audience()}.</p>
     */
    public static final class Provider implements TagResolverProvider {

        @Override
        public String name() {
            return "miniplaceholders";
        }

        @Override
        public TagResolver getTagResolver(Context context) {
            return resolvers();
        }
    }
}
