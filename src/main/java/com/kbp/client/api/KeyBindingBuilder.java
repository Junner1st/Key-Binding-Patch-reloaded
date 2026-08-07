package com.kbp.client.api;

import com.google.common.collect.ImmutableSet;
import com.kbp.client.KBPMod;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

import java.util.Arrays;
import java.util.function.BooleanSupplier;

/**
 * @see KBPMod#newBuilder(String)
 * @see KBPMod#newToggleableBuilder(String, BooleanSupplier)
 */
@OnlyIn( Dist.CLIENT )
public abstract class KeyBindingBuilder
{
    protected String category = "key.categories.gameplay";
    protected Key key = InputConstants.UNKNOWN;
    protected ImmutableSet< Key > cmb_keys = ImmutableSet.of();
    protected IKeyConflictContext conflict_context = KeyConflictContext.IN_GAME;

    /**
     * @see KBPMod#newBuilder(String)
     * @see KBPMod#newToggleableBuilder(String, BooleanSupplier)
     */
    protected KeyBindingBuilder() {
    }

    public KeyBindingBuilder withCategory( String category )
    {
        this.category = category;
        return this;
    }

    public KeyBindingBuilder withKey( Key key )
    {
        this.key = key;
        return this;
    }

    public KeyBindingBuilder withKeyboardKey( int key_code )
    {
        this.key = Type.KEYSYM.getOrCreate( key_code );
        return this;
    }

    public KeyBindingBuilder withMouseButton( int button )
    {
        this.key = Type.MOUSE.getOrCreate( button );
        return this;
    }

    public KeyBindingBuilder withCmbKeys( Key... cmb_keys )
    {
        this.cmb_keys = ImmutableSet.copyOf( cmb_keys );
        return this;
    }

    public KeyBindingBuilder withKeyboardCmbKeys( int... cmb_keys )
    {
        this.cmb_keys = (
            Arrays.stream( cmb_keys )
            .mapToObj( Type.KEYSYM::getOrCreate )
            .collect( ImmutableSet.toImmutableSet() )
        );
        return this;
    }

    public KeyBindingBuilder withConflictContext( IKeyConflictContext context )
    {
        this.conflict_context = context;
        return this;
    }

    public abstract IPatchedKeyBinding build();

    public IPatchedKeyBinding buildAndRegis()
    {
        final IPatchedKeyBinding kb = this.build();
        KBPMod.registerKeyMapping( ( net.minecraft.client.KeyMapping ) kb );
        return kb;
    }
}
