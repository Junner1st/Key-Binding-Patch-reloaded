package com.kbp.client;

import com.kbp.client.impl.ShadowKeyBinding;
import com.kbp.client.impl.ShadowToggleableKeyBinding;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.ToggleKeyMapping;
import net.neoforged.fml.util.ObfuscationReflectionHelper;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Runtime manager for shadow key bindings.
 *
 * <p>Unlike the startup registration path (which goes through
 * {@link net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent}), this
 * manager allows shadow key bindings to be created while the game is already
 * running, e.g. from the in-game key binding screen. It mirrors the same
 * bookkeeping as the vanilla {@code KeyMapping} class:
 * <ul>
 *   <li>{@code KeyMapping.ALL} — name → mapping lookup table</li>
 *   <li>{@code KeyMapping.MAP} — NeoForge's {@code KeyMappingLookup}</li>
 *   <li>{@code Options.keyMappings} — the array iterated by the controls screen</li>
 *   <li>{@code KBPModConfig.SHADOW_KEY_BINDINGS} — persisted config list</li>
 * </ul>
 */
public final class KBPShadowManager
{
    private KBPShadowManager() {
    }

    /** Reference to {@code KeyMapping.ALL}. */
    private static final Map< String, KeyMapping > ALL = Objects.requireNonNull(
        ObfuscationReflectionHelper.getPrivateValue( KeyMapping.class, null, "ALL" )
    );

    /** Reference to {@code KeyMapping.MAP} (NeoForge {@code KeyMappingLookup}). */
    private static final Object MAP = Objects.requireNonNull(
        ObfuscationReflectionHelper.getPrivateValue( KeyMapping.class, null, "MAP" )
    );

    /**
     * Count how many shadow bindings already exist for the given key binding
     * (both in the config and among the currently registered ones).
     */
    private static int countShadows( KeyMapping target )
    {
        final String name = target.getName();
        return ( int ) KBPModConfig.SHADOW_KEY_BINDINGS.get().stream()
            .filter( name::equals )
            .count();
    }

    /**
     * Persist the in-memory config value to disk. NeoForge's {@code ConfigValue.set()}
     * only mutates the in-memory value; without explicitly saving, the change is lost
     * on the next game restart.
     */
    private static void saveConfig()
    {
        try
        {
            // Access ConfigTracker.INSTANCE via reflection to stay resilient against
            // minor API signature differences across NeoForge versions.
            final Class< ? > tracker_class = Class.forName( "net.neoforged.fml.config.ConfigTracker" );
            final Object tracker = tracker_class.getField( "INSTANCE" ).get( null );
            final java.lang.reflect.Method get_configs = tracker_class.getMethod( "getConfigsByMod", String.class );
            final Object configs = get_configs.invoke( tracker, "key_binding_patch" );

            if ( configs instanceof Iterable< ? > )
            {
                for ( final Object cfg : ( Iterable< ? > ) configs )
                {
                    if ( cfg != null )
                    {
                        cfg.getClass().getMethod( "save" ).invoke( cfg );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            // Persistence failure is non-fatal; the value stays in memory.
        }
    }

    /**
     * Create a new shadow key binding for {@code target}, register it into the
     * vanilla lookup tables and the {@link Options}, and persist it to the config.
     *
     * @return the newly created shadow mapping, or {@code null} on failure.
     */
    public static KeyMapping addShadow( KeyMapping target )
    {
        if ( target == null ) {
            return null;
        }

        final int index = countShadows( target );

        final KeyMapping shadow;
        if ( target instanceof ToggleKeyMapping )
        {
            shadow = new ShadowToggleableKeyBinding( target, index );
        }
        else
        {
            shadow = new ShadowKeyBinding( target, index );
        }

        // 1) Insert into KeyMapping.ALL (name → mapping).
        ALL.put( shadow.getName(), shadow );

        // 2) Insert into the NeoForge KeyMappingLookup (key → mappings).
        final Object map = MAP;
        if ( map != null )
        {
            try
            {
                map.getClass().getMethod( "put", com.mojang.blaze3d.platform.InputConstants.Key.class, KeyMapping.class )
                    .invoke( map, shadow.getKey(), shadow );
            }
            catch ( Exception e )
            {
                // Ignore; lookup failures are non-fatal here.
            }
        }

        // 3) Append to Options.keyMappings so the controls screen shows it.
        final Options options = Minecraft.getInstance().options;
        if ( options != null )
        {
            final KeyMapping[] old_arr = options.keyMappings;
            final KeyMapping[] new_arr = Arrays.copyOf( old_arr, old_arr.length + 1 );
            new_arr[ old_arr.length ] = shadow;
            options.keyMappings = new_arr;
        }

        // 4) Persist to config (same name repeated N times ⇒ N shadows).
        final java.util.List< String > list = KBPModConfig.SHADOW_KEY_BINDINGS.get().stream()
            .collect( Collectors.toList() );
        list.add( target.getName() );
        KBPModConfig.SHADOW_KEY_BINDINGS.set( list );
        saveConfig();

        return shadow;
    }

    /**
     * Remove one shadow binding of {@code target} (the newest one), unregistering
     * it from the lookup tables and the {@link Options}, and persist the change.
     */
    public static void removeShadow( KeyMapping target )
    {
        if ( target == null ) {
            return;
        }

        final int count = countShadows( target );
        if ( count <= 0 ) {
            return;
        }

        // Find the last shadow registered for this target.
        final String shadow_name = String.format( "shadow#%s@%d", target.getName(), count - 1 );
        final KeyMapping shadow = ALL.get( shadow_name );
        if ( shadow != null )
        {
            ALL.remove( shadow_name );
            final Object map = MAP;
            if ( map != null )
            {
                try
                {
                    map.getClass().getMethod( "remove", KeyMapping.class ).invoke( map, shadow );
                }
                catch ( Exception e )
                {
                    // Ignore.
                }
            }

            final Options options = Minecraft.getInstance().options;
            if ( options != null )
            {
                options.keyMappings = Arrays.stream( options.keyMappings )
                    .filter( kb -> kb != shadow )
                    .toArray( KeyMapping[]::new );
            }
        }

        // Remove one entry from the config list.
        final java.util.List< String > list = KBPModConfig.SHADOW_KEY_BINDINGS.get().stream()
            .collect( Collectors.toList() );
        final int idx = list.lastIndexOf( target.getName() );
        if ( idx >= 0 )
        {
            list.remove( idx );
        }
        KBPModConfig.SHADOW_KEY_BINDINGS.set( list );
        saveConfig();
    }
}
