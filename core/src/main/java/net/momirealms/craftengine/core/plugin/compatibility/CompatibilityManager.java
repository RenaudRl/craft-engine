package net.momirealms.craftengine.core.plugin.compatibility;

import com.google.gson.JsonElement;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.entity.furniture.ExternalModel;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;

import java.util.function.BiConsumer;

public interface CompatibilityManager {

    void onLoad();

    void onEnable();

    void onDelayedEnable();

    void runDelayedSyncTasks();

    void registerTagResolverProvider(TagResolverProvider provider);

    ExternalModel createModel(String id);


    boolean isPluginEnabled(String plugin);

    boolean hasPlugin(String plugin);



    int getViaVersionProtocolVersion(NetWorkUser user);

    TagResolver[] createExternalTagResolvers(Context context);

    boolean isBedrockPlayer(Player player);

    ModelProvider getModelProvider(String id);

    void registerModelProvider(ModelProvider provider);

    ItemSource getItemSource(String id);

    void registerItemSource(ItemSource itemSource);

    LevelerProvider getLevelerProvider(String id);

    void registerLevelerProvider(LevelerProvider provider);

    EntityProvider getEntityProvider(String id);

    void registerEntityProvider(EntityProvider provider);

    boolean hasPermission(NetWorkUser user, String permission);

    int remapEntityId(int entityId);

    void blueMapBlockColors(ImmutableBlockState state, BiConsumer<String, JsonElement> callback);
}
