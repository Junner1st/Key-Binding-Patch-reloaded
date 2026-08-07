package com.kbp.client.api;

import com.google.common.collect.ImmutableSet;
import com.kbp.client.KBPMod;
import com.mojang.blaze3d.platform.InputConstants.Key;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.IKeyMappingExtension;

/**
 * @see KBPMod#getPatched(KeyMapping)
 * @see KBPMod#findByName(String)
 */
@OnlyIn( Dist.CLIENT )
public interface IPatchedKeyBinding extends IKeyMappingExtension
{
    default ImmutableSet< Key > getDefaultCmbKeys() {
        throw new UnsupportedOperationException();
    }

    default ImmutableSet< Key > getCmbKeys() {
        throw new UnsupportedOperationException();
    }

    default void setKeyAndCmbKeys( Key key, ImmutableSet< Key > cmb_keys ) {
        throw new UnsupportedOperationException();
    }

    void addPressCallback( Runnable callback );

    boolean removePressCallback( Runnable callback );

    void addReleaseCallback( Runnable callback );

    boolean removeReleaseCallback( Runnable callback );
}
