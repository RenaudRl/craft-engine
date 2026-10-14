package net.momirealms.craftengine.bukkit.plugin.network.listener.game;

import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.ByteBufPacketEvent;
import net.momirealms.craftengine.core.plugin.network.listener.ByteBufferPacketListener;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.VersionHelper;

public final class SwingListener implements ByteBufferPacketListener {
    public static final ByteBufferPacketListener INSTANCE = new SwingListener();

    private SwingListener() {}

    @Override
    public void onPacketReceive(NetWorkUser user, ByteBufPacketEvent event) {
        BukkitServerPlayer player = (BukkitServerPlayer) user;
        // 26.3: ServerboundPunchPacket is a unit packet (main hand only) — there is no hand to read.
        if (VersionHelper.isOrAbove26_3) {
            player.onSwingHand();
            return;
        }
        FriendlyByteBuf buf = event.getBuffer();
        int hand = buf.readVarInt();
        if (hand == 0/*main*/) {
            player.onSwingHand();
        }
    }
}
