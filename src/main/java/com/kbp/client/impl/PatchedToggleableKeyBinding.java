package com.kbp.client.impl;

import com.google.common.collect.ImmutableSet;
import com.kbp.client.KBPMod;
import com.mojang.blaze3d.platform.InputConstants.Key;
import net.minecraft.client.ToggleKeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.BooleanSupplier;

/**
 * Use {@link KBPMod#newToggleableBuilder(String, BooleanSupplier)} if possible
 * as this implementation is not guaranteed to present in all version.
 */
@OnlyIn( Dist.CLIENT )
public class PatchedToggleableKeyBinding extends ToggleKeyMapping
{
    public PatchedToggleableKeyBinding(
        String description,
        int key_code,
        ImmutableSet< Key > cmb_keys,
        String category,
        BooleanSupplier toggle_controller
    ) {
        super( description, key_code, category, toggle_controller );

        final IKeyBindingImpl kb = ( IKeyBindingImpl ) this;
        kb.initDefaultCmbKeys( cmb_keys );
    }
}
