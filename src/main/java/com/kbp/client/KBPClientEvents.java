package com.kbp.client;

import com.kbp.client.api.IPatchedKeyBinding;
import com.kbp.client.impl.ShadowKeyBinding;
import com.kbp.client.impl.ShadowToggleableKeyBinding;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.ToggleKeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@EventBusSubscriber( modid = "key_binding_patch", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT )
public final class KBPClientEvents
{
    private KBPClientEvents() {
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings( RegisterKeyMappingsEvent event )
    {
        // Register key mappings queued via KeyBindingBuilder#buildAndRegis().
        KBPMod.takePendingRegistrations().forEach( event::register );

        // User may create multiple shadow copies for a single key binding,
        // so we need to count it and assign unique number for their save keys.
        KBPModConfig.SHADOW_KEY_BINDINGS.get().stream()
            .collect( Collectors.groupingBy( Function.identity(), Collectors.counting() ) )
            .entrySet().stream()
            .flatMap( e -> (
                KBPMod.findByName( e.getKey() )
                .map( kbp -> ( KeyMapping ) kbp )
                .map( kb -> {
                    final int cnt = e.getValue().intValue();
                    final IntStream is = IntStream.range( 0, cnt );
                    final IntFunction< KeyMapping > to_shadow = (
                        kb instanceof ToggleKeyMapping
                        ? i -> new ShadowToggleableKeyBinding( kb, i )
                        : i -> new ShadowKeyBinding( kb, i )
                    );
                    return is.mapToObj( to_shadow );
                } )
                .orElseGet( Stream::empty )
            ) )
            .forEachOrdered( event::register );
    }
}
