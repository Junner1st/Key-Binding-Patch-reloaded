package com.kbp.client.mixin;

import com.kbp.client.KBPShadowManager;
import com.kbp.client.impl.IKeyBindingImpl;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Adds a small "+"/"-" button on the left side of every key binding entry in the
 * controls screen.
 * <ul>
 *   <li>Normal entry → "+" : creates a shadow key binding for this functionality.</li>
 *   <li>Shadow entry → "-" : removes this shadow key binding.</li>
 * </ul>
 * Both operations register/unregister immediately, persist to the mod config and
 * rebuild the controls screen list while keeping the scroll position.
 */
@Mixin( targets = "net.minecraft.client.gui.screens.options.controls.KeyBindsList$KeyEntry" )
public abstract class KeyEntryMixin
{
    @Shadow( remap = false )
    @Final
    private KeyMapping key;

    @Shadow( remap = false )
    private KeyBindsList this$0;

    @Unique
    private Button kbp_add_btn;

    @Unique
    private static final int BTN_WIDTH = 14;

    @Unique
    private static final int BTN_HEIGHT = 16;

    @Unique
    private boolean __kbpIsShadow()
    {
        return this.key != null && IKeyBindingImpl.isShadowKeyBinding( this.key );
    }

    @Unique
    private void __kbpEnsureAddBtn()
    {
        if ( this.kbp_add_btn == null )
        {
            this.kbp_add_btn = Button.builder(
                Component.literal( this.__kbpIsShadow() ? "-" : "+" ),
                this::__kbpOnClicked
            ).bounds( 0, 0, BTN_WIDTH, BTN_HEIGHT ).build();
        }
    }

    @Unique
    private void __kbpOnClicked( Button btn )
    {
        final KeyMapping kb = this.key;
        if ( kb == null ) {
            return;
        }

        // Record scroll position before rebuilding.
        final net.minecraft.client.gui.screens.options.controls.KeyBindsScreen screen = this.__kbpGetScreen();
        double scroll = 0.0D;
        if ( screen != null )
        {
            scroll = this.__kbpGetListScroll( screen );
        }

        if ( this.__kbpIsShadow() )
        {
            // Remove this shadow binding.
            final Optional< KeyMapping > target = IKeyBindingImpl.getShadowTarget( kb );
            target.ifPresent( KBPShadowManager::removeShadow );
        }
        else
        {
            // Create a new shadow binding for this functionality.
            KBPShadowManager.addShadow( kb );
        }

        // Rebuild the controls screen (reads updated options.keyMappings) and
        // restore the scroll position.
        if ( screen != null )
        {
            this.__kbpRebuildScreen( screen, scroll );
        }
    }

    @Unique
    private net.minecraft.client.gui.screens.options.controls.KeyBindsScreen __kbpGetScreen()
    {
        final KeyBindsList outer = this.this$0;
        if ( outer == null ) {
            return null;
        }
        try
        {
            final java.lang.reflect.Field f = KeyBindsList.class.getDeclaredField( "keyBindsScreen" );
            f.setAccessible( true );
            return ( net.minecraft.client.gui.screens.options.controls.KeyBindsScreen ) f.get( outer );
        }
        catch ( Exception e )
        {
            return null;
        }
    }

    @Unique
    private double __kbpGetListScroll( net.minecraft.client.gui.screens.options.controls.KeyBindsScreen screen )
    {
        try
        {
            final java.lang.reflect.Field f = net.minecraft.client.gui.screens.options.controls.KeyBindsScreen.class
                .getDeclaredField( "keyBindsList" );
            f.setAccessible( true );
            final KeyBindsList list = ( KeyBindsList ) f.get( screen );
            return list != null ? list.getScrollAmount() : 0.0D;
        }
        catch ( Exception e )
        {
            return 0.0D;
        }
    }

    @Unique
    private void __kbpRebuildScreen( net.minecraft.client.gui.screens.options.controls.KeyBindsScreen screen, double scroll )
    {
        final Minecraft mc = Minecraft.getInstance();
        if ( mc == null ) {
            return;
        }

        // Use the ORIGINAL parent (settings screen) for the fresh instance, NOT the
        // current controls screen. Otherwise every rebuild pushes another
        // KeyBindsScreen onto the back-navigation chain, forming a "stack".
        final net.minecraft.client.gui.screens.Screen parent = this.__kbpGetParentScreen( screen );

        // Rebuild the whole screen; the new instance reads the (already updated)
        // options.keyMappings and constructs a fresh KeyBindsList.
        final net.minecraft.client.gui.screens.options.controls.KeyBindsScreen fresh = new net.minecraft.client.gui.screens.options.controls.KeyBindsScreen( parent, mc.options );
        mc.setScreen( fresh );

        // Restore the scroll position on the freshly created list.
        try
        {
            final java.lang.reflect.Field f = net.minecraft.client.gui.screens.options.controls.KeyBindsScreen.class
                .getDeclaredField( "keyBindsList" );
            f.setAccessible( true );
            final KeyBindsList list = ( KeyBindsList ) f.get( fresh );
            if ( list != null )
            {
                final double max = list.getMaxScroll();
                list.setScrollAmount( Math.min( scroll, max ) );
            }
        }
        catch ( Exception e )
        {
            // Scroll restoration is best-effort; ignore failures.
        }
    }

    @Unique
    private net.minecraft.client.gui.screens.Screen __kbpGetParentScreen( net.minecraft.client.gui.screens.options.controls.KeyBindsScreen screen )
    {
        try
        {
            // OptionsSubScreen.lastScreen holds the screen this controls screen was
            // opened from (e.g. the controls/settings screen). It is a protected
            // field of a parent class, so access it via reflection.
            final java.lang.reflect.Field f = net.minecraft.client.gui.screens.options.OptionsSubScreen.class
                .getDeclaredField( "lastScreen" );
            f.setAccessible( true );
            final net.minecraft.client.gui.screens.Screen parent = ( net.minecraft.client.gui.screens.Screen ) f.get( screen );
            return parent != null ? parent : screen;
        }
        catch ( Exception e )
        {
            return screen;
        }
    }

    /**
     * Shift the name label to the right so it does not overlap the "+"/"-" button.
     */
    @ModifyArg(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"
        ),
        index = 2
    )
    private int kbp$onDrawStringX( int original_x )
    {
        return original_x + BTN_WIDTH + 4;
    }

    /**
     * Replace the label of a shadow entry with "*" + translated original name.
     */
    @ModifyArg(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"
        ),
        index = 1
    )
    private Component kbp$onDrawStringText( Component original )
    {
        if ( this.__kbpIsShadow() )
        {
            final Optional< KeyMapping > target = IKeyBindingImpl.getShadowTarget( this.key );
            if ( target.isPresent() )
            {
                return Component.literal( "*" ).append( target.get().getDisplayName() );
            }
        }
        return original;
    }

    /**
     * Render the "+"/"-" button on the far left of the entry. Drawn after the
     * vanilla render so it stays on top of the label.
     */
    @Inject( method = "render", at = @At( "RETURN" ) )
    private void kbp$onRenderReturn(
        GuiGraphics graphics,
        int index,
        int top,
        int left,
        int width,
        int height,
        int mouse_x,
        int mouse_y,
        boolean hovered,
        float partial_ticks,
        CallbackInfo ci
    )
    {
        this.__kbpEnsureAddBtn();
        final Button btn = this.kbp_add_btn;
        btn.setPosition( left + 2, top + ( height - BTN_HEIGHT ) / 2 );
        btn.render( graphics, mouse_x, mouse_y, partial_ticks );
    }

    /**
     * Inject the "+"/"-" button into the entry's children list so that the vanilla
     * event dispatch (via ContainerEventHandler) reaches it and it becomes clickable.
     */
    @ModifyExpressionValue(
        method = "children",
        at = @At(
            value = "INVOKE",
            target = "Lcom/google/common/collect/ImmutableList;of(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;",
            remap = false
        )
    )
    private com.google.common.collect.ImmutableList< GuiEventListener > kbp$onChildren( com.google.common.collect.ImmutableList< GuiEventListener > original )
    {
        this.__kbpEnsureAddBtn();
        final ArrayList< GuiEventListener > out = new ArrayList<>( original );
        out.add( this.kbp_add_btn );
        return com.google.common.collect.ImmutableList.copyOf( out );
    }
}
