package com.kbp.client.impl;

import com.google.common.collect.ImmutableSet;
import com.kbp.client.api.IPatchedKeyBinding;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

/**
 * Only use {@link IPatchedKeyBinding} unless you know what you are doing. This
 * interface is not guarantee to be stable between versions.
 */
@OnlyIn( Dist.CLIENT )
public interface IKeyBindingImpl
{
    default void initDefaultCmbKeys( ImmutableSet< Key > cmb_keys ) {
        throw new UnsupportedOperationException();
    }

    Object getDelegate();


    ImmutableSet< Key > CMB_CTRL = ImmutableSet.of( Type.KEYSYM.getOrCreate( GLFW.GLFW_KEY_LEFT_CONTROL ) );
    ImmutableSet< Key > CMB_SUPER = ImmutableSet.of( Type.KEYSYM.getOrCreate( GLFW.GLFW_KEY_LEFT_SUPER ) );
    ImmutableSet< Key > CMB_SHIFT = ImmutableSet.of( Type.KEYSYM.getOrCreate( GLFW.GLFW_KEY_LEFT_SHIFT ) );
    ImmutableSet< Key > CMB_ALT = ImmutableSet.of( Type.KEYSYM.getOrCreate( GLFW.GLFW_KEY_LEFT_ALT ) );
    static ImmutableSet< Key > toCmbKeySet( KeyModifier modifier )
    {
        switch ( modifier )
        {
        case CONTROL:
            return Minecraft.ON_OSX ? CMB_SUPER : CMB_CTRL;
        case SHIFT:
            return CMB_SHIFT;
        case ALT:
            return CMB_ALT;
        default:
            return ImmutableSet.of();
        }
    }

    static KeyModifier toModifier( ImmutableSet< Key > cmb_keys )
    {
        if ( cmb_keys.isEmpty() ) {
            return KeyModifier.NONE;
        }

        final Type type = Type.KEYSYM;
        if ( Minecraft.ON_OSX )
        {
            final Key lsuper = type.getOrCreate( GLFW.GLFW_KEY_LEFT_SUPER );
            final Key rsuper = type.getOrCreate( GLFW.GLFW_KEY_RIGHT_SUPER );
            if ( cmb_keys.contains( lsuper ) || cmb_keys.contains( rsuper ) ) {
                return KeyModifier.CONTROL;
            }
        }
        else
        {
            final Key lctrl = type.getOrCreate( GLFW.GLFW_KEY_LEFT_CONTROL );
            final Key rctrl = type.getOrCreate( GLFW.GLFW_KEY_RIGHT_CONTROL );
            if ( cmb_keys.contains( lctrl ) || cmb_keys.contains( rctrl ) ) {
                return KeyModifier.CONTROL;
            }
        }

        final Key lshift = type.getOrCreate( GLFW.GLFW_KEY_LEFT_SHIFT );
        final Key rshift = type.getOrCreate( GLFW.GLFW_KEY_RIGHT_SHIFT );
        if ( cmb_keys.contains( lshift ) || cmb_keys.contains( rshift ) ) {
            return KeyModifier.SHIFT;
        }

        final Key lalt = type.getOrCreate( GLFW.GLFW_KEY_LEFT_ALT );
        final Key ralt = type.getOrCreate( GLFW.GLFW_KEY_RIGHT_ALT );
        if ( cmb_keys.contains( lalt ) || cmb_keys.contains( ralt ) ) {
            return KeyModifier.ALT;
        }

        return KeyModifier.NONE;
    }

    static boolean isShadowKeyBinding( KeyMapping kb ) {
        return kb instanceof ShadowKeyBinding || kb instanceof ShadowToggleableKeyBinding;
    }

    static Optional< KeyMapping > getShadowTarget( KeyMapping kb )
    {
        if ( kb instanceof ShadowKeyBinding )
        {
            final ShadowKeyBinding skb = ( ShadowKeyBinding ) kb;
            return Optional.of( skb.target );
        }
        else if ( kb instanceof ShadowToggleableKeyBinding )
        {
            final ShadowToggleableKeyBinding skb = ( ShadowToggleableKeyBinding ) kb;
            return Optional.of( skb.target );
        }
        else {
            return Optional.empty();
        }
    }

    static Optional< String > getShadowTarget( String name )
    {
        if( name.startsWith( "shadow#" ) )
        {
            final int suffix = name.indexOf( '@' );
            return Optional.of( name.substring( 7, suffix ) );
        }
        else {
            return Optional.empty();
        }
    }
}
