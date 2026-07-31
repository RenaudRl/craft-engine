package net.momirealms.craftengine.core.plugin.text.minimessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.momirealms.craftengine.core.util.AdventureHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface FormattedLine {
    TagResolver[] CUSTOM_RESOLVERS = new TagResolver[]{
            createDummyResolvers("expr"),
            createDummyResolvers("image"),
            createDummyResolvers("arg"),
            createDummyResolvers("shift"),
            createDummyResolvers("i18n"),
            createDummyResolvers("l10n"),
            createDummyResolvers("global")
    };

    class Companion {
        public static TagResolver[] LATEST_RESOLVERS = CUSTOM_RESOLVERS;

        /**
         * Resolvers recognised by name is not enough for every source of dynamic tags.
         *
         * <p>The {@code papi} and {@code rel_papi} entries above used to cover every
         * PlaceholderAPI placeholder under two fixed names. MiniPlaceholders has no such funnel:
         * each expansion contributes its own {@code expansion_key} tags, and the set depends on
         * which plugins are installed. So a platform hook contributes a whole resolver, which
         * answers {@code has(name)} for anything it can resolve.</p>
         */
        private static final List<TagResolver> DYNAMIC_RESOLVERS = new ArrayList<>();

        public static void registerDynamicResolver(TagResolver resolver) {
            DYNAMIC_RESOLVERS.add(resolver);
            rebuild(LAST_CUSTOM_NAMES);
        }

        private static List<String> LAST_CUSTOM_NAMES = List.of();

        public static void resetWithCustomResolvers(List<String> customResolvers) {
            LAST_CUSTOM_NAMES = List.copyOf(customResolvers);
            rebuild(LAST_CUSTOM_NAMES);
        }

        private static void rebuild(List<String> customResolvers) {
            List<TagResolver> resolvers = new ArrayList<>(Arrays.asList(CUSTOM_RESOLVERS));
            for (String name : customResolvers) {
                resolvers.add(createDummyResolvers(name));
            }
            resolvers.addAll(DYNAMIC_RESOLVERS);
            LATEST_RESOLVERS = resolvers.toArray(new TagResolver[0]);
        }
    }

    Component parse(net.momirealms.craftengine.core.plugin.context.Context context);

    private static TagResolver createDummyResolvers(String tag) {
        return new TagResolver() {
            @Override
            public boolean has(@NotNull String name) {
                return tag.equals(name);
            }

            @Override
            public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
                return null;
            }
        };
    }

    static FormattedLine create(String line) {
        if (line.equals(AdventureHelper.customMiniMessage().stripTags(line, Companion.LATEST_RESOLVERS))) {
            return new PreParsedLine(AdventureHelper.miniMessage().deserialize(line));
        } else {
            return new DynamicLine(line);
        }
    }

    class PreParsedLine implements FormattedLine {
        private final Component parsed;

        public PreParsedLine(Component parsed) {
            this.parsed = parsed;
        }

        @Override
        public Component parse(net.momirealms.craftengine.core.plugin.context.Context context) {
            return this.parsed;
        }
    }

    class DynamicLine implements FormattedLine {
        private final String content;

        public DynamicLine(String content) {
            this.content = content;
        }

        @Override
        public Component parse(net.momirealms.craftengine.core.plugin.context.Context context) {
            // The audience must reach `deserialize`, not the resolvers: MiniMessage binds
            // audience-scoped placeholders to the target, so passing only the resolvers would
            // leave every player-dependent MiniPlaceholders tag empty.
            net.kyori.adventure.pointer.Pointered audience = context.audience();
            return audience == null
                    ? AdventureHelper.miniMessage().deserialize(this.content, context.tagResolvers())
                    : AdventureHelper.miniMessage().deserialize(this.content, audience, context.tagResolvers());
        }
    }
}

