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

import fr.aeldit.ctms.gui.CTMSScreen;
import fr.aeldit.ctms.util.CTMResourcePack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Made using a part of ModMenu's ModListWidget and Minecraft's EntryListWidget (rearranged to match the purpose of this mod)
 */
public class ResourcePacksListWidget extends AlwaysSelectedEntryListWidget<ResourcePacksListWidget.ResourcePackListEntry> implements AutoCloseable
{
    private boolean scrolling;
    private final CTMSScreen parent;

    public ResourcePacksListWidget(MinecraftClient minecraftClient, int i, int j, int k, int l, int m, CTMSScreen parent)
    {
        super(minecraftClient, i, j, k, l, m);
        this.parent = parent;
    }

    @Override
    protected void renderList(DrawContext context, int mouseX, int mouseY, float delta)
    {
        int j = this.getRowWidth();
        int k = this.itemHeight - 4;
        int l = this.getEntryCount();

        for (int m = 0; m < l; ++m)
        {
            int n = this.getRowTop(m);
            int o = this.getRowBottom(m);
            if (o < this.top || n > this.bottom)
            {
                continue;
            }
            this.renderEntry(context, mouseX, mouseY, delta, m, 6, 2 + n + 11 * m, j, k);
        }
    }

    @Override
    public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
        renderList(context, mouseX, mouseY, delta);
        context.drawText(client.textRenderer, Text.translatable("ctms.screen.ctmPacks"), 16, 52, 0xFFFFFF, false);
    }

    @Override
    protected int getScrollbarPositionX()
    {
        return width / 2 + 97;
    }

    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1)
    {
        this.updateScrollingState(double_1, double_2, int_1);

        if (!this.isMouseOver(double_1, double_2))
        {
            return false;
        }
        else
        {
            ResourcePackListEntry entry = getEntryAtPosition(double_1, double_2);

            if (entry != null)
            {
                if (entry.mouseClicked(double_1, double_2, int_1))
                {
                    this.setFocused(entry);
                    this.setDragging(true);
                    return true;
                }
            }
            else if (int_1 == 0)
            {
                this.clickedHeader((int) (double_1 - (double) (this.left + this.width / 2 - this.getRowWidth() / 2)), (int) (double_2 - (double) this.top) + (int) this.getScrollAmount() - 4);
                return true;
            }
            return scrolling;
        }
    }

    @Override
    protected int addEntry(ResourcePackListEntry entry)
    {
        return super.addEntry(entry);
    }

    public void addAll(@NotNull ArrayList<CTMResourcePack> ctmResourcePacks)
    {
        ctmResourcePacks.forEach(ctmResourcePack -> addEntry(ctmResourcePack.asResourcePackListEntry()));
    }

    @Override
    public void close()
    {
    }

    public static class ResourcePackListEntry extends AlwaysSelectedEntryListWidget.Entry<ResourcePackListEntry>
    {
        private final CTMResourcePack ctmResourcePack;
        private final MinecraftClient client;
        protected Identifier iconPath;

        public ResourcePackListEntry(CTMResourcePack ctmResourcePack)
        {
            this.ctmResourcePack = ctmResourcePack;
            this.client = MinecraftClient.getInstance();
        }

        @Override
        public void render(@NotNull DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta)
        {
            context.drawTexture(getIconTexture(), x, y, 0.0f, 0.0f, 32, 32, 32, 32);
            context.drawText(client.textRenderer, ctmResourcePack.getName(), x + 32 + 3, y + 1, 0xFFFFFF, false);
        }

        @Override
        public Text getNarration()
        {
            return Text.literal("resourcePackName");
        }

        public Identifier getIconTexture()
        {
            if (this.iconPath == null)
            {
                this.iconPath = new Identifier("textures/misc/unknown_pack.png"); // ctmResourcePack.getIconPath().toString().replace("\\", "/")
            }
            return iconPath;
        }
    }
}
