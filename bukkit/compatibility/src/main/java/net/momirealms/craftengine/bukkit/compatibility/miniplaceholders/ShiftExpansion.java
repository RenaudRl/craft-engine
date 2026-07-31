package net.momirealms.craftengine.bukkit.compatibility.miniplaceholders;

import io.github.miniplaceholders.api.Expansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import org.jetbrains.annotations.Nullable;

/**
 * The {@code shift} expansion: {@code <shift_mm:10>} and {@code <shift_raw:10>}.
 *
 * <p>Replaces {@code %shift_mm_10%}. As with {@link ImageExpansion}, the {@code mm} form used to
 * hand back MiniMessage markup as a string; it now yields a parsed tag.</p>
 *
 * <p>Two forms are gone. The {@code md} (MineDown) key, for the reason given in
 * {@link ImageExpansion}; and the bare {@code %shift_10%}, which has no equivalent because a
 * MiniPlaceholders tag is always {@code expansion_key}. Use {@code <shift_mm:10>}, which is what
 * the bare form resolved to anyway.</p>
 */
public final class ShiftExpansion {
    private ShiftExpansion() {}

    public static Expansion create(CraftEngine plugin) {
        return Expansion.builder("shift")
                .author("XiaoMoMi")
                .version("1.0")
                .globalPlaceholder("mm", (queue, ctx) -> {
                    Integer amount = popAmount(queue);
                    if (amount == null) return null;
                    return Tag.preProcessParsed(plugin.fontManager().createMiniMessageOffsets(amount));
                })
                .globalPlaceholder("raw", (queue, ctx) -> {
                    Integer amount = popAmount(queue);
                    if (amount == null) return null;
                    return Tag.selfClosingInserting(Component.text(plugin.fontManager().createRawOffsets(amount)));
                })
                .build();
    }

    @Nullable
    private static Integer popAmount(ArgumentQueue queue) {
        if (!queue.hasNext()) return null;
        try {
            return Integer.parseInt(queue.pop().value());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
