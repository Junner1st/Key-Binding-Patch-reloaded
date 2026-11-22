package com.kbp.client.mixin;

import com.google.common.collect.ImmutableSet;
import com.kbp.client.api.IPatchedKeyBinding;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;

@Mixin( Options.class )
public abstract class OptionsMixin
{
    @WrapOperation(
        method = "processOptionsForge",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Options$FieldAccess;process(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"
        )
    )
    private String kbp$processKeyBinding(
        Options.FieldAccess access,
        String name,
        String current_value,
        Operation< String > original,
        @Local KeyMapping key_mapping
    )
    {
        final String saved_value = original.call( access, name, current_value );
        if ( current_value.equals( saved_value ) ) {
            return saved_value;
        }

        // Vanilla and NeoForge bindings have at most two fields. Let NeoForge
        // load those as before; KBP adds a third field containing combo keys.
        final String[] fields = saved_value.split( ":", -1 );
        if ( fields.length < 3 ) {
            return saved_value;
        }

        final Key key = InputConstants.getKey( fields[ 0 ] );
        final ImmutableSet< Key > cmb_keys = Arrays.stream( fields[ 2 ].split( "\\+" ) )
            .filter( name_part -> !name_part.isEmpty() )
            .map( InputConstants::getKey )
            .collect( ImmutableSet.toImmutableSet() );
        ( ( IPatchedKeyBinding ) key_mapping ).setKeyAndCmbKeys( key, cmb_keys );

        // NeoForge only reads the first two fields and would overwrite the
        // restored combo keys. Returning the previous value skips that setter.
        // Any modifier NeoForge appended after the combo field is redundant.
        return current_value;
    }
}
