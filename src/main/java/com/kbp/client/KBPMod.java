package com.kbp.client;

import com.kbp.client.api.IPatchedKeyBinding;
import com.kbp.client.api.KeyBindingBuilder;
import com.kbp.client.gui.KBPConfigScreen;
import com.kbp.client.impl.PatchedKeyBinding;
import com.kbp.client.impl.PatchedToggleableKeyBinding;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import net.minecraft.client.KeyMapping;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;

@Mod( "key_binding_patch" )
public final class KBPMod
{
    /**
     * You should use this to retrieve {@link IPatchedKeyBinding} interface from
     * {@link KeyMapping} instances because there is no guarantee that
     * {@link KeyMapping} will always implement {@link IPatchedKeyBinding} in
     * the future.
     *
     * @see #findByName(String)
     */
    public static IPatchedKeyBinding getPatched( KeyMapping key_binding ) {
        return ( IPatchedKeyBinding ) key_binding;
    }

    /**
     * @see #getPatched(KeyMapping)
     */
    public static Optional< IPatchedKeyBinding > findByName( String name ) {
        return Optional.ofNullable( KeyMapping$ALL.get( name ) ).map( KBPMod::getPatched );
    }

    /**
     * Convenient builder for creating normal key bindings.
     *
     * @see #newToggleableBuilder(String, BooleanSupplier)
     */
    public static KeyBindingBuilder newBuilder( String description )
    {
        return new KeyBindingBuilder() {
            @Override
            @SuppressWarnings( "DataFlowIssue" )
            public IPatchedKeyBinding build()
            {
                return ( IPatchedKeyBinding ) new PatchedKeyBinding(
                    description,
                    this.conflict_context,
                    this.key,
                    this.cmb_keys,
                    this.category
                );
            }
        };
    }

    /**
     * Convenient builder for creating toggleable key bindings.
     *
     * @see #newBuilder(String)
     */
    public static KeyBindingBuilder newToggleableBuilder(
        String description, BooleanSupplier toggle_controller
    ) {
        return new KeyBindingBuilder() {
            @Override
            @SuppressWarnings( "DataFlowIssue" )
            public IPatchedKeyBinding build()
            {
                final Key default_key = this.key;
                final PatchedToggleableKeyBinding tkb = new PatchedToggleableKeyBinding(
                    description,
                    InputConstants.UNKNOWN.getValue(),
                    this.cmb_keys,
                    this.category,
                    toggle_controller
                ) {
                    @Nonnull
                    @Override
                    public Key getDefaultKey() {
                        return default_key;
                    }
                };
                tkb.setKeyConflictContext( this.conflict_context );
                return ( IPatchedKeyBinding ) tkb;
            }
        };
    }


    // Internal implementations that should not be accessed by other mods.
    private static final Map< String, KeyMapping > KeyMapping$ALL = Objects.requireNonNull(
        ObfuscationReflectionHelper.getPrivateValue( KeyMapping.class, null, "ALL" )
    );

    /** Key mappings registered via {@link KeyBindingBuilder#buildAndRegis()}. */
    private static final java.util.List< KeyMapping > PENDING_REGISTRATIONS = new java.util.ArrayList<>();

    /**
     * Queue a key mapping for registration via {@link net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent}.
     */
    public static void registerKeyMapping( KeyMapping kb ) {
        PENDING_REGISTRATIONS.add( kb );
    }

    /**
     * @see #registerKeyMapping(KeyMapping)
     */
    public static java.util.List< KeyMapping > takePendingRegistrations() {
        final java.util.List< KeyMapping > out = new java.util.ArrayList<>( PENDING_REGISTRATIONS );
        PENDING_REGISTRATIONS.clear();
        return out;
    }

    public KBPMod( final net.neoforged.fml.ModContainer mod_container )
    {
        // Setup mod config settings.
        mod_container.registerConfig( net.neoforged.fml.config.ModConfig.Type.CLIENT, KBPModConfig.CONFIG_SPEC );

        // Register config screen factory for NeoForge.
        final java.util.function.Supplier< IConfigScreenFactory > screen_factory = () -> new IConfigScreenFactory()
        {
            @Override
            public net.minecraft.client.gui.screens.Screen createScreen(
                net.neoforged.fml.ModContainer container,
                net.minecraft.client.gui.screens.Screen screen
            ) {
                return new KBPConfigScreen( screen );
            }
        };
        mod_container.registerExtensionPoint( IConfigScreenFactory.class, screen_factory );
    }
}
