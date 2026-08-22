package net.momirealms.craftengine.bukkit.plugin.placeholder;

import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The boundary between CraftEngine and MiniPlaceholders.
 *
 * <p>CraftEngine ships its own copy of Adventure and relocates {@code net.kyori} over the whole
 * plugin jar. A class living inside that jar therefore cannot hand an {@code Audience}, a
 * {@code TagResolver} or a {@code Component} to MiniPlaceholders, nor accept one back: the two type
 * universes are disjoint, and no shadow {@code exclude} can bridge them — a relocator rewrites the
 * <em>referenced</em> name, never the owning class, so no package can be exempted. The
 * implementation consequently lives in a separate, unrelocated jar
 * ({@code miniplaceholders.jarinjar}) loaded into the plugin classloader at runtime.</p>
 *
 * <p>This interface is the contract both sides share. It works only because
 * {@code net.momirealms.craftengine.**} is not relocated, so both sides load the very same class —
 * which holds only as long as no signature here mentions an Adventure type. Rendered text crosses
 * as JSON and each side deserializes it with its own serializer.</p>
 */
public interface MiniPlaceholdersBridge {

    /** Implementation class, resolved reflectively once the nested jar has been loaded. */
    String IMPLEMENTATION = "net.momirealms.craftengine.bukkit.placeholder.miniplaceholders.MiniPlaceholdersBridgeImpl";

    /** Builds and registers CraftEngine's four expansions with MiniPlaceholders. */
    void registerExpansions(BukkitCraftEngine plugin);

    /** Whether some registered expansion answers for the tag {@code name}. */
    boolean isPlaceholderTag(String name);

    /**
     * Resolves one placeholder tag and returns the result as a JSON component.
     *
     * <p>The tag is passed apart from its arguments rather than as ready-made MiniMessage: quoting
     * and escaping belong to the side that owns the MiniMessage instance doing the parsing.</p>
     *
     * <p>A tag claimed by {@link #isPlaceholderTag(String)} but left unresolved — wrong arity, say —
     * comes back as its own literal text, which is exactly what MiniPlaceholders does natively.</p>
     *
     * @param viewer the audience placeholders are read from, or {@code null} when there is none
     */
    String renderToJson(String tag, List<String> arguments, @Nullable Player viewer);
}
