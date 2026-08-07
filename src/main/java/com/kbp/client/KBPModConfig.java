package com.kbp.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

import java.util.Collections;
import java.util.List;

@OnlyIn( Dist.CLIENT )
public final class KBPModConfig
{
    static final ModConfigSpec CONFIG_SPEC;
    public static final ConfigValue< List< ? extends String > > SHADOW_KEY_BINDINGS;

    static
    {
        final Builder builder = new Builder();
        SHADOW_KEY_BINDINGS = (
            builder.comment(
                "Shadow key bindings are replications of the specified key binding.",
                "This brings the ability to have multiple key setups for a single functionality."
            )
            .defineList( "shadow_key_bindings", Collections.emptyList(), String.class::isInstance )
        );
        CONFIG_SPEC = builder.build();
    }

    private KBPModConfig() {
    }
}
