package com.kbp.client.mixin;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.kbp.client.api.IPatchedKeyBinding;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.LinkedList;

@Mixin( KeyBindsScreen.class )
public abstract class ControlsScreenMixin extends Screen
{
    @Shadow
    public KeyMapping selectedKey;

    @Shadow
    public long lastKeySelection;

    @Shadow
    private KeyBindsList keyBindsList;


    /**
     * It turns out that NeoForge may automatically set {@link #selectedKey} to
     * {@code null} in {@link KeyboardHandler#keyPress(long, int, int, int, int)}
     * under certain circumstances when keyboard key is released, so we have to
     * manually copy the reference to use.
     */
    @Unique
    private KeyMapping shadow_selected_key;

    @Unique
    private final LinkedList< Key > active_inputs = new LinkedList<>();


    public ControlsScreenMixin( Screen parent, Options settings, Component title ) {
        super( title );
    }

    @Override
    public boolean keyPressed( int key, int scan_code, int modifier )
    {
        if ( this.selectedKey == null ) {
            return super.keyPressed( key, scan_code, modifier );
        }

        // Copy reference so that we can use it on key release.
        this.shadow_selected_key = this.selectedKey;

        if ( key == GLFW.GLFW_KEY_ESCAPE )
        {
            this.active_inputs.clear();
            this.__updateSelectedKeyBinding();
        }
        else
        {
            final Key input = InputConstants.getKey( key, scan_code );
            this.active_inputs.addFirst( input );
        }

        this.lastKeySelection = Util.getMillis();
        return true;
    }

    @Override
    public boolean keyReleased( int key, int scan_code, int modifier )
    {
        if ( this.shadow_selected_key == null ) {
            return super.keyReleased( key, scan_code, modifier );
        }

        this.__updateSelectedKeyBinding();
        this.active_inputs.clear();
        return true;
    }

    @Override
    public boolean mouseClicked( double x, double y, int button )
    {
        if ( this.selectedKey == null ) {
            return super.mouseClicked( x, y, button );
        }

        this.shadow_selected_key = this.selectedKey;
        final Key input = Type.MOUSE.getOrCreate( button );
        this.active_inputs.addFirst( input );
        return true;
    }

    @Override
    public boolean mouseReleased( double x, double y, int button )
    {
        final boolean is_select_click_release = this.active_inputs.isEmpty();
        if ( this.shadow_selected_key == null || is_select_click_release ) {
            return super.mouseReleased( x, y, button );
        }

        this.__updateSelectedKeyBinding();
        this.active_inputs.clear();
        return true;
    }

    @Unique
    private void __updateSelectedKeyBinding()
    {
        final IPatchedKeyBinding kb = ( IPatchedKeyBinding ) this.shadow_selected_key;
        final Key key = MoreObjects.firstNonNull( this.active_inputs.peekFirst(), InputConstants.UNKNOWN );
        final ImmutableSet< Key > cmb_keys = this.active_inputs.stream().skip( 1 ).collect( ImmutableSet.toImmutableSet() );
        kb.setKeyAndCmbKeys( key, cmb_keys );

        // NOTE: Options.setKey(KeyMapping, InputConstants.Key) - verify this method exists in 1.21.1
        final KeyMapping km = ( KeyMapping ) kb;
        km.setKey( key );

        this.shadow_selected_key = null;
        this.selectedKey = null;
        KeyMapping.resetMapping();

        // Refresh the key binding list so the change is visible immediately.
        if ( this.keyBindsList != null ) {
            this.keyBindsList.resetMappingAndUpdateButtons();
        }
    }
}
