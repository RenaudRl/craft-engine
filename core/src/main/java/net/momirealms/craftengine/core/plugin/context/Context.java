package net.momirealms.craftengine.core.plugin.context;

import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface Context {

    ContextHolder contexts();

    TagResolver[] tagResolvers();

    /**
     * The audience this text is being rendered for, or {@code null} when there is none.
     *
     * <p>MiniMessage binds audience-scoped placeholders to the target handed to
     * {@code deserialize}, not to the resolvers. Without this, every MiniPlaceholders tag that
     * depends on a player — most of them — would silently resolve to nothing.</p>
     */
    @Nullable
    default Pointered audience() {
        return null;
    }

    <T> Optional<T> getOptionalParameter(ContextKey<T> parameter);

    default <T> T getParameterOrThrow(ContextKey<T> parameter) {
        return getOptionalParameter(parameter).orElseThrow(() -> new RuntimeException("No parameter found for " + parameter));
    }
}
