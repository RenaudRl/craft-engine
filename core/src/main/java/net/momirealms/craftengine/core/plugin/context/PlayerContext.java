package net.momirealms.craftengine.core.plugin.context;

import net.kyori.adventure.pointer.Pointered;
import net.momirealms.craftengine.core.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface PlayerContext extends Context {

    Player player();

    /**
     * The platform player, when it is an Adventure audience.
     *
     * <p>Checked by {@code instanceof} rather than cast: {@code platformPlayer()} is deliberately
     * untyped so core stays platform-agnostic, and only the Bukkit implementation happens to be
     * {@link Pointered}.</p>
     */
    @Override
    @Nullable
    default Pointered audience() {
        Player player = player();
        if (player == null) {
            return null;
        }
        return player.platformPlayer() instanceof Pointered pointered ? pointered : null;
    }
}
