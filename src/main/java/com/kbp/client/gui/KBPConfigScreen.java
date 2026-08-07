package com.kbp.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Objects;

@OnlyIn( Dist.CLIENT )
public final class KBPConfigScreen extends Screen
{
    private static final int WHITE = 0xFFFFFF;

    private final Screen parent_screen;
    private ShadowCountList shadow_count_list;

    public KBPConfigScreen( Screen parent )
    {
        super( Component.translatable( "kbp.gui.config_title" ) );

        this.parent_screen = parent;
    }

    @Override
    protected void init()
    {
        final Button cancel_btn = Button.builder(
            CommonComponents.GUI_CANCEL,
            btn -> Objects.requireNonNull( this.minecraft ).setScreen( this.parent_screen )
        ).bounds( this.width / 2 - 155, this.height - 29, 150, 20 ).build();
        this.addRenderableWidget( cancel_btn );

        final Button save_btn = Button.builder(
            CommonComponents.GUI_DONE,
            btn -> {
                this.shadow_count_list._applyChanges();

                final Minecraft mc = Objects.requireNonNull( this.minecraft );
                final ConfirmScreen confirm_screen = new ConfirmScreen(
                    ok -> {
                        if ( ok ) {
                            mc.setScreen( this.parent_screen );
                        }
                    },
                    Component.translatable( "kbp.gui.alert_title" ),
                    Component.translatable( "kbp.gui.alert_message" )
                );
                mc.setScreen( confirm_screen );
            }
        ).bounds( this.width / 2 - 155 + 160, this.height - 29, 150, 20 ).build();
        save_btn.active = false;
        this.addRenderableWidget( save_btn );

        final ShadowCountList shadow_lst = new ShadowCountList( this, save_btn );
        this.shadow_count_list = shadow_lst;
        this.addWidget( shadow_lst );
    }

    @Override
    public void render( @Nonnull GuiGraphics graphics, int mouse_x, int mouse_y, float partial_ticks )
    {
        this.renderTransparentBackground( graphics );
        this.renderMenuBackground( graphics );
        this.shadow_count_list.render( graphics, mouse_x, mouse_y, partial_ticks );
        graphics.drawCenteredString( this.font, this.title, this.width / 2, 8, WHITE );

        super.render( graphics, mouse_x, mouse_y, partial_ticks );
    }
}
