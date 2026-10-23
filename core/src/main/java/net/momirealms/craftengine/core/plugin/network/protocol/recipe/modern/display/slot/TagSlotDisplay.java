package net.momirealms.craftengine.core.plugin.network.protocol.recipe.modern.display.slot;

import com.mojang.datafixers.util.Either;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;

import java.util.List;

public final class TagSlotDisplay implements SlotDisplay {
    // 26.3+ sends a HolderSet<Item> (named tag or direct item ids); before that a bare tag key.
    private final Either<List<Integer>, Key> tag;

    public TagSlotDisplay(Either<List<Integer>, Key> tag) {
        this.tag = tag;
    }

    public static TagSlotDisplay read(FriendlyByteBuf buf, FriendlyByteBuf.Reader<Item> reader) {
        return new TagSlotDisplay(VersionHelper.isOrAbove26_3 ? buf.readHolderSet() : Either.right(buf.readKey()));
    }

    @Override
    public void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item> writer) {
        buf.writeVarInt(BuiltInRegistries.SLOT_DISPLAY_TYPE.getId(SlotDisplayTypes.TAG));
        if (VersionHelper.isOrAbove26_3) {
            buf.writeHolderSet(this.tag);
        } else {
            buf.writeKey(this.tag.right().orElseThrow(() -> new IllegalStateException("Direct item sets are only sent from 26.3")));
        }
    }

    @Override
    public String toString() {
        return "TagSlotDisplay{" +
                "tag=" + this.tag +
                '}';
    }
}