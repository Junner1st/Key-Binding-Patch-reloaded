package com.kbp.client.impl;

import com.google.common.collect.ImmutableSet;
import com.kbp.client.KBPMod;
import com.mojang.blaze3d.platform.InputConstants.Key;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;

/**
 * Use {@link KBPMod#newBuilder(String)} if possible as this implementation is
 * not guaranteed to present in all version.
 */
@OnlyIn( Dist.CLIENT )
public class PatchedKeyBinding extends KeyMapping
{
    public PatchedKeyBinding(
        String description,
        IKeyConflictContext conflict_context,
        Key key,
        ImmutableSet< Key > cmb_keys,
        String category
    ) {
        super( description, conflict_context, key, category );

        final IKeyBindingImpl kb = ( IKeyBindingImpl ) this;
        kb.initDefaultCmbKeys( cmb_keys );
    }
}
