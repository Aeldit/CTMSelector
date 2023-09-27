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

package fr.aeldit.ctms.gui.widgets;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class ControlsPackWidget extends AlwaysSelectedEntryListWidget<ControlsPackWidget.ControlsPackEntry>
{
    public ControlsPackWidget(MinecraftClient client, int width, int height, int k, int l, int m)
    {
        super(client, width, height, k, l, m);
        this.centerListVertically = false;
        Objects.requireNonNull(client.textRenderer);
        this.setRenderHeader(true, (int) (9.0F * 1.5F));
    }

    public void addAll(@NotNull List<ControlsPackEntry> entries)
    {
        entries.forEach(super::addEntry);
    }

    public int getRowWidth()
    {
        return this.width;
    }

    protected int getScrollbarPositionX()
    {
        return this.right - 6;
    }

    @Environment(EnvType.CLIENT)
    public static class ControlsPackEntry extends AlwaysSelectedEntryListWidget.Entry<ControlsPackEntry>
    {
        private final ControlsPackWidget widget;
        protected final MinecraftClient client;

        public ControlsPackEntry(MinecraftClient client, ControlsPackWidget widget)
        {
            this.client = client;
            this.widget = widget;
        }

        public void render(@NotNull DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta)
        {
            context.drawTextWithShadow(this.client.textRenderer, "Test", x + 32 + 2, y + 1, 16777215);
        }

        @Override
        public Text getNarration()
        {
            return null;
        }
    }
}