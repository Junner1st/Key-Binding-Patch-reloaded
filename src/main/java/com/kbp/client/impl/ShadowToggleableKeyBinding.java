package com.kbp.client.impl;

import com.kbp.client.api.IPatchedKeyBinding;
import com.kbp.client.mixin.ToggleableKeyBindingAccess;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.ToggleKeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;

/**
 * Only used by internal shadow key bindings.
 */
@OnlyIn( Dist.CLIENT )
public final class ShadowToggleableKeyBinding
    extends ToggleKeyMapping
    implements IPatchedKeyBinding, IKeyBindingImpl
{
    public final KeyMapping target;

    public ShadowToggleableKeyBinding( KeyMapping target, int index )
    {
        super(
            String.format( "shadow#%s@%d", target.getName(), index ),
            GLFW.GLFW_KEY_UNKNOWN,
            target.getCategory(),
            ( ( ToggleableKeyBindingAccess ) target ).getNeedsToggle()
        );

        this.target = target;
    }

    @Override
    public boolean isDown() {
        return this.target.isDown();
    }

    @Override
    public boolean consumeClick() {
        return this.target.consumeClick();
    }

    @Override
    public void setKeyConflictContext( @Nonnull IKeyConflictContext context ) {
        this.target.setKeyConflictContext( context );
    }

    @Nonnull
    @Override
    public IKeyConflictContext getKeyConflictContext() {
        return this.target.getKeyConflictContext();
    }

    @Override
    public void addPressCallback( Runnable callback )
    {
        final IPatchedKeyBinding ikb = ( IPatchedKeyBinding ) this.target;
        ikb.addPressCallback( callback );
    }

    @Override
    public boolean removePressCallback( Runnable callback )
    {
        final IPatchedKeyBinding ikb = ( IPatchedKeyBinding ) this.target;
        return ikb.removePressCallback( callback );
    }

    @Override
    public void addReleaseCallback( Runnable callback )
    {
        final IPatchedKeyBinding ikb = ( IPatchedKeyBinding ) this.target;
        ikb.addReleaseCallback( callback );
    }

    @Override
    public boolean removeReleaseCallback( Runnable callback )
    {
        final IPatchedKeyBinding ikb = ( IPatchedKeyBinding ) this.target;
        return ikb.removeReleaseCallback( callback );
    }

    @Override
    public Object getDelegate() {
        return this.target;
    }
}
