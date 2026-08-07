package com.kbp.client.gui;

import com.kbp.client.KBPModConfig;
import com.kbp.client.impl.IKeyBindingImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@OnlyIn( Dist.CLIENT )
final class ShadowCountList extends AbstractSelectionList< ShadowCountList.Entry >
{
    private static final int WHITE = 0xFFFFFF;

    private final Button save_all_btn;
    private final int max_label_width;

    private final Map< String, Integer > shadow_count = (
        KBPModConfig.SHADOW_KEY_BINDINGS.get().stream()
        .collect( Collectors.groupingBy( Function.identity(), Collectors.summingInt( o -> 1 ) ) )
    );

    private final HashMap< String, Integer > shadow_change = new HashMap<>();


    ShadowCountList( KBPConfigScreen parent, Button save_all_btn )
    {
        super( parent.getMinecraft(), parent.width, parent.height - 55, 23, 20 );

        this.save_all_btn = save_all_btn;

        final net.minecraft.client.KeyMapping[] kb_arr = (
            Arrays.stream( this.minecraft.options.keyMappings )
            .filter( kb -> !IKeyBindingImpl.isShadowKeyBinding( kb ) )
            .toArray( net.minecraft.client.KeyMapping[]::new )
        );

        final int[] maxLabelW = { 0 };
        final Map< String, List< ShadowCountEntry > > grouped = Arrays.stream( kb_arr ).collect( Collectors.groupingBy(
            net.minecraft.client.KeyMapping::getCategory,
            Collectors.mapping( ShadowCountEntry::new, Collectors.toList() )
        ) );

        Arrays.stream( kb_arr )
            .map( net.minecraft.client.KeyMapping::getCategory )
            .distinct()
            .forEachOrdered( category -> {
                final Component label = Component.translatable( category );
                this.addEntry( new CategoryEntry( label ) );
                final List< ShadowCountEntry > entries = grouped.get( category );
                entries.sort( Comparator.comparing( e -> e.label_text.getString() ) );
                entries.forEach( this::addEntry );
                for ( final ShadowCountEntry e : entries )
                {
                    final int w = this.minecraft.font.width( e.label_text );
                    if ( w > maxLabelW[ 0 ] ) maxLabelW[ 0 ] = w;
                }
            } );

        this.max_label_width = maxLabelW[ 0 ];
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 15 + 20;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 32;
    }

    @Override
    protected void updateWidgetNarration( @Nonnull NarrationElementOutput output ) {
    }

    void _applyChanges()
    {
        assert !this.shadow_change.isEmpty();
        KBPModConfig.SHADOW_KEY_BINDINGS.set(
            this.shadow_count.entrySet().stream()
            .flatMap( e -> {
                final String name = e.getKey();
                final int cnt = e.getValue();
                return Stream.generate( () -> name ).limit( cnt );
            } )
            .collect( Collectors.toList() )
        );
    }


    // Base entry type for this list
    abstract static class Entry extends AbstractSelectionList.Entry< Entry >
    {
    }


    final class CategoryEntry extends Entry
    {
        private final Component label;

        private CategoryEntry( Component label )
        {
            this.label = label;
        }

        @Override
        public void render(
            @Nonnull GuiGraphics graphics,
            int index,
            int top,
            int left,
            int width,
            int height,
            int mouse_x,
            int mouse_y,
            boolean hovered,
            float partial_ticks
        ) {
            final Minecraft mc = ShadowCountList.this.minecraft;
            final Screen parent = Objects.requireNonNull( mc.screen );
            final Font font = mc.font;
            final int label_w = font.width( this.label );
            final float pos_x = ( parent.width - label_w ) * 0.5F;
            final float pos_y = top + height - font.lineHeight - 1;
            graphics.drawString( font, this.label, (int) pos_x, (int) pos_y, WHITE );
        }
    }


    final class ShadowCountEntry extends Entry
    {
        private final String kb_name;
        private final Component label_text;
        private final Button reduce_count_btn;
        private final Button increase_count_btn;
        private final Button count_field;

        private ShadowCountEntry( net.minecraft.client.KeyMapping kb )
        {
            final String name = kb.getName();
            this.kb_name = name;
            this.label_text = Component.translatable( name );

            final int count = this.__getShadowCount();
            final Component text = Component.literal( Integer.toString( count ) );
            final Button count_field = Button.builder( text, btn -> { } ).bounds( 0, 0, 20, 20 ).build();
            count_field.active = false;
            this.count_field = count_field;

            final Button rdc_btn = Button.builder(
                Component.literal( "-" ),
                this::__handleReduceBtnClick
            ).bounds( 0, 0, 20, 20 ).build();
            rdc_btn.active = count > 0;
            this.reduce_count_btn = rdc_btn;

            final Button icr_btn = Button.builder(
                Component.literal( "+" ),
                this::__handleIncreaseBtnClick
            ).bounds( 0, 0, 20, 20 ).build();
            icr_btn.active = count < 5;
            this.increase_count_btn = icr_btn;
        }

        private void __handleReduceBtnClick( Button btn )
        {
            final int cnt = this.__shiftShadowCount( -1 );
            btn.active = cnt > 0;
            this.increase_count_btn.active = true;
        }

        private void __handleIncreaseBtnClick( Button btn )
        {
            final int cnt = this.__shiftShadowCount( 1 );
            btn.active = cnt < 5;
            this.reduce_count_btn.active = true;
        }

        @Override
        public void render(
            @Nonnull GuiGraphics graphics,
            int index,
            int top,
            int left,
            int width,
            int height,
            int mouse_x,
            int mouse_y,
            boolean hovered,
            float partial_ticks
        ) {
            final Font font = ShadowCountList.this.minecraft.font;
            final int pos_x = left + 90 - ShadowCountList.this.max_label_width;
            final int pos_y = top + ( height - font.lineHeight ) / 2;
            graphics.drawString( font, this.label_text, pos_x, pos_y, WHITE );

            final Button rcb = this.reduce_count_btn;
            rcb.setPosition( left + 105, top );
            rcb.render( graphics, mouse_x, mouse_y, partial_ticks );

            final Button cf = this.count_field;
            cf.setPosition( left + 127, top );
            cf.render( graphics, mouse_x, mouse_y, partial_ticks );

            final Button icb = this.increase_count_btn;
            icb.setPosition( left + 149, top );
            icb.render( graphics, mouse_x, mouse_y, partial_ticks );
        }

        @Override
        public boolean mouseClicked( double mx, double my, int button )
        {
            return (
                this.reduce_count_btn.mouseClicked( mx, my, button )
                || this.increase_count_btn.mouseClicked( mx, my, button )
            );
        }

        @Override
        public boolean mouseReleased( double mx, double my, int button )
        {
            return (
                this.reduce_count_btn.mouseReleased( mx, my, button )
                || this.increase_count_btn.mouseReleased( mx, my, button )
            );
        }

        private int __getShadowCount() {
            return ShadowCountList.this.shadow_count.getOrDefault( this.kb_name, 0 );
        }

        private int __shiftShadowCount( int delta )
        {
            final int count = this.__getShadowCount() + delta;
            final Component text = Component.literal( Integer.toString( count ) );
            this.count_field.setMessage( text );

            final String kb = this.kb_name;
            ShadowCountList.this.shadow_count.compute( kb, ( k, v ) -> count != 0 ? count : null );
            final HashMap< String, Integer > shadow_change = ShadowCountList.this.shadow_change;
            shadow_change.compute( kb, ( k, v ) -> {
                final int prev_delta = v != null ? v : 0;
                final int new_delta = prev_delta + delta;
                return new_delta != 0 ? new_delta : null;
            } );
            ShadowCountList.this.save_all_btn.active = !shadow_change.isEmpty();
            return count;
        }
    }
}
