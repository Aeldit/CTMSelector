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

import fr.aeldit.ctms.util.CTMResourcePack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ResourcePacksListWidget extends AlwaysSelectedEntryListWidget<ResourcePacksListWidget.ResourcePackListEntry> implements AutoCloseable
{
    public ResourcePacksListWidget(MinecraftClient minecraftClient, int i, int j, int k, int l, int m)
    {
        super(minecraftClient, i, j, k, l, m);
    }

    @Override
    protected void renderList(DrawContext context, int mouseX, int mouseY, float delta)
    {
        int i = this.getRowLeft();
        int j = this.getRowWidth();
        int k = this.itemHeight - 4;
        int l = this.getEntryCount();

        context.drawText(client.textRenderer, Text.translatable("ctms.screen.ctmPacks"), 50, 50, 0xFFFFFF, false);

        for (int m = 0; m < l; ++m)
        {
            int n = this.getRowTop(m);
            int o = this.getRowBottom(m);
            if (o < this.top || n > this.bottom)
            {
                continue;
            }
            this.renderEntry(context, mouseX, mouseY, delta, m, i, n + 10 * m, j, k);
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
