package net.momirealms.craftengine.bukkit.compatibility.miniplaceholders;

import io.github.miniplaceholders.api.Expansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.font.Image;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * The {@code image} expansion: {@code <image_mm:namespace:path:row:col>} and
 * {@code <image_raw:…>}.
 *
 * <p>Replaces {@code %image_mm_namespace:path:row:col%}. Under PlaceholderAPI the {@code mm} form
 * returned MiniMessage markup <em>as a string</em>, which the caller then had to parse again; it now
 * returns a parsed tag directly.</p>
 *
 * <p>The former {@code md} (MineDown) key is gone. A MineDown string cannot be carried by a
 * MiniMessage tag: the result is a Component, so the markup would reach the player as literal
 * text. MineDown consumers cannot use MiniPlaceholders tags at all.</p>
 */
public final class ImageExpansion {
    private ImageExpansion() {}

    public static Expansion create(BukkitCraftEngine plugin) {
        return Expansion.builder("image")
                .author("XiaoMoMi")
                .version("1.0")
                .globalPlaceholder("mm", (queue, ctx) -> {
                    Image image = resolveImage(plugin, queue);
                    if (image == null) return null;
                    return position(queue, image, true);
                })
                .globalPlaceholder("raw", (queue, ctx) -> {
                    Image image = resolveImage(plugin, queue);
                    if (image == null) return null;
                    return position(queue, image, false);
                })
                .build();
    }

    /**
     * Pops the image identifier from the queue: either a bare id value, or a
     * {@code namespace} + {@code path} pair.
     */
    @Nullable
    private static Image resolveImage(BukkitCraftEngine plugin, ArgumentQueue queue) {
        if (!queue.hasNext()) return null;
        String first = queue.pop().value();
        if (!queue.hasNext()) {
            return plugin.fontManager().imageByIdValue(first).orElse(null);
        }
        String second = queue.pop().value();
        Key key;
        try {
            key = Key.of(first, second);
        } catch (IllegalArgumentException e) {
            plugin.logger().warn("Invalid image namespaced key: " + first + ":" + second);
            return null;
        }
        Optional<Image> optional = plugin.fontManager().imageById(key);
        return optional.orElse(null);
    }

    /**
     * Reads the optional row and column, then renders the glyph.
     *
     * @param parsed whether the value carries MiniMessage markup and must be parsed rather than
     *               inserted verbatim
     */
    @Nullable
    private static Tag position(ArgumentQueue queue, Image image, boolean parsed) {
        int row = queue.hasNext() ? intOrZero(queue.pop().value()) : 0;
        int col = queue.hasNext() ? intOrZero(queue.pop().value()) : 0;
        try {
            if (parsed) {
                return Tag.preProcessParsed(image.miniMessageAt(row, col));
            }
            return Tag.selfClosingInserting(Component.text(new String(Character.toChars(image.codepointAt(row, col)))));
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private static int intOrZero(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
