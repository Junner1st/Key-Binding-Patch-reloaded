package com.kbp.client.mixin;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.kbp.client.api.IPatchedKeyBinding;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyMappingLookup;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Mixin( KeyMappingLookup.class )
public abstract class KeyBindingMapMixin
{
    @Shadow( remap = false )
    private static @Final EnumMap< KeyModifier, Map< Key, Collection< KeyMapping > > > map;

    /**
     * @author Giant_Salted_Fish
     * @reason Prioritize key bindings with their combo keys.
     */
    @Overwrite( remap = false )
    public void put( Key key, KeyMapping kb )
    {
        if ( key == InputConstants.UNKNOWN ) {
            return;
        }

        final KeyModifier modifier = kb.getKeyModifier();
        final Map< Key, Collection< KeyMapping > > mapper = map.get( modifier );
        mapper.compute( key, ( k, v ) -> {
            if ( v == null )
            {
                final ArrayList< KeyMapping > lst = new ArrayList<>();
                lst.add( kb );
                return lst;
            }
            else
            {
                final List< KeyMapping > update_lst = ( List< KeyMapping > ) v;
                final List< Integer > prio_lst = Lists.transform( update_lst, o -> {
                    final IPatchedKeyBinding ikb = ( IPatchedKeyBinding ) o;
                    return ikb.getCmbKeys().size();
                } );

                final IPatchedKeyBinding ikb = ( IPatchedKeyBinding ) kb;
                final int priority = MoreObjects.firstNonNull( ikb.getCmbKeys(), ImmutableSet.of() ).size();
                final int result = Collections.binarySearch( Lists.reverse( prio_lst ), priority );
                final int index = result < 0 ? -result - 1 : result;
                update_lst.add( update_lst.size() - index, kb );
                return update_lst;
            }
        } );
    }
}
