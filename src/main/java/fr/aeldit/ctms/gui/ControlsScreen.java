/*
 * Copyright (c) 2023  -  Made by Aeldit
 *
 *              GNU LESSER GENERAL PUBLIC LICENSE
 *                  Version 3, 29 June 2007
 *
 *  Copyright (C) 2007 Free Software Foundation, Inc. <https://fsf.org/>
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 *
 *
 * This version of the GNU Lesser General Public License incorporates
 * the terms and conditions of version 3 of the GNU General Public
 * License, supplemented by the additional permissions listed in the LICENSE.txt file
 * in the repo of this mod (https://github.com/Aeldit/Cyan)
 */

package fr.aeldit.ctms.gui;

import fr.aeldit.ctms.gui.widgets.ControlsPackWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class ControlsScreen extends Screen
{
    private final Screen parent;
    private ControlsPackWidget controlsPackWidget;
    private PackListWidget packListWidget;

    public ControlsScreen(Screen parent)
    {
        super(Text.translatable("ctms.screen.controls.title"));
        this.parent = parent;
    }

    @Override
    public void close()
    {
        Objects.requireNonNull(client).setScreen(parent);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta)
    {
        renderBackgroundTexture(drawContext);
        controlsPackWidget.render(drawContext, mouseX, mouseY, delta);
        drawContext.drawCenteredTextWithShadow(textRenderer, title, width / 2, 5, 0xffffff);
        super.render(drawContext, mouseX, mouseY, delta);
    }

    @Override
    protected void init()
    {
        controlsPackWidget = new ControlsPackWidget(client, width, height, 32, height - 32, 25);
        controlsPackWidget.addAll(Collections.singletonList(new ControlsPackWidget.ControlsPackEntry(MinecraftClient.getInstance(), controlsPackWidget)));
        addSelectableChild(controlsPackWidget);
    }
}
