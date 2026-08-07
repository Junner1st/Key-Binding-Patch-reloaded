package com.kbp.client.mixin;

import com.kbp.client.impl.IKeyBindingImpl;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

/**
 * Mixin for the key binding list in the controls screen.
 * NOTE: In 1.21.1, this class may be {@code KeyBindsList} (inner class of ControlsScreen)
 * or a standalone class at {@code net.minecraft.client.gui.screens.controls.KeyBindsList}.
 * Verify the target class name when compiling.
 */
@Mixin( targets = "net.minecraft.client.gui.screens.options.controls.KeyBindsList" )
public abstract class KeyBindingListMixin
{
    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Arrays;sort([Ljava/lang/Object;)V"
        )
    )
    private void onNew$Invoke( Object[] array )
    {
        Arrays.sort( array, Comparator.comparing( o -> {
            final KeyMapping kb = ( KeyMapping ) o;
            return IKeyBindingImpl.getShadowTarget( kb ).orElse( kb );
        } ) );
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "NEW",
            target = "(Ljava/lang/String;)Lnet/minecraft/network/chat/Component;",
            ordinal = 1
        ),
        require = 0  // Soft-require; may not match in 1.21.1
    )
    private Component onNew$Invoke( String name )
    {
        return (
            IKeyBindingImpl.getShadowTarget( name )
            .map( raw -> ( Component ) Component.literal( "*" ).append( Component.translatable( raw ) ) )
            .orElseGet( () -> Component.translatable( name ) )
        );
    }
}
