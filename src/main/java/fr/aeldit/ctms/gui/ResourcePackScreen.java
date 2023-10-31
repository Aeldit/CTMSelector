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

import fr.aeldit.ctms.textures.CTMBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static fr.aeldit.ctms.util.Utils.TEXTURES_HANDLING;

@Environment(EnvType.CLIENT)
public class ResourcePackScreen extends Screen
{
    private final String packName;
    private final Screen parent;

    public ResourcePackScreen(Screen parent, @NotNull String packName)
    {
        super(CTMBlocks.getEnabledPacks().contains("file/" + packName.replace(" (folder)", ""))
                ? Text.of(packName.replace(".zip", ""))
                : Text.of(Formatting.ITALIC + packName.replace(".zip", "") + Text.translatable("ctms.screen.packDisabledTitle").getString())
        );
        this.packName = packName;
        this.parent = parent;
    }

    @Override
    public void close()
    {
        Objects.requireNonNull(client).setScreen(parent);
    }

    @Override
    public void render(DrawContext DrawContext, int mouseX, int mouseY, float delta)
    {
        super.render(DrawContext, mouseX, mouseY, delta);
        DrawContext.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xffffff);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta)
    {
        super.renderBackgroundTexture(context);
    }

    @Override
    protected void init()
    {
        ListWidget list = new ListWidget(client, width, height, 32, height - 32, 25, packName);
        addDrawableChild(list);

        // Sorts the blocks alphabetically
        List<CTMBlocks.CTMBlock> toSort = new ArrayList<>(CTMBlocks.getAvailableCtmBlocks(packName));
        toSort.sort(Comparator.comparing(block -> block.getName().getString()));

        for (CTMBlocks.CTMBlock block : toSort)
        {
            list.add(block);
        }

        addDrawableChild(
                ButtonWidget.builder(Text.translatable("ctms.screen.config.reset"), button -> {
                            CTMBlocks.getCTMBlocks(packName).resetOptions();
                            CTMBlocks.getCTMBlocks(packName).clearUnsavedOptions();
                            TEXTURES_HANDLING.updateUsedTextures(packName);
                            close();
                        })
                        .tooltip(Tooltip.of(Text.translatable("ctms.screen.config.reset.tooltip")))
                        .dimensions(10, 6, 100, 20)
                        .build()
        );

        addDrawableChild(
                ButtonWidget.builder(ScreenTexts.CANCEL, button -> {
                            CTMBlocks.getCTMBlocks(packName).restoreUnsavedOptions();
                            close();
                        })
                        .tooltip(Tooltip.of(Text.translatable("ctms.screen.config.cancel.tooltip")))
                        .dimensions(width / 2 - 154, height - 28, 150, 20)
                        .build()
        );
        addDrawableChild(
                ButtonWidget.builder(Text.translatable("ctms.screen.config.save&quit"), button -> {
                            if (CTMBlocks.getCTMBlocks(packName).optionsChanged())
                            {
                                CTMBlocks.getCTMBlocks(packName).clearUnsavedOptions();
                                TEXTURES_HANDLING.updateUsedTextures(packName);
                            }
                            close();
                        })
                        .tooltip(Tooltip.of(Text.translatable("ctms.screen.config.save&quit.tooltip")))
                        .dimensions(width / 2 + 4, height - 28, 150, 20)
                        .build()
        );
    }

    /**
     * Modified by me to fit my purpose
     *
     * @author dicedpixels (<a href="https://github.com/dicedpixels">...</a>)
     */
    private static class ListWidget extends ElementListWidget<Entry>
    {
        private final EntryBuilder builder = new EntryBuilder(client, width);
        private final String packName;

        public ListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight, String packName)
        {
            super(client, width, height, top, bottom, itemHeight);
            this.packName = packName;
        }

        public void add(CTMBlocks.CTMBlock block)
        {
            addEntry(builder.build(block, packName));
        }
    }

    private record EntryBuilder(MinecraftClient client, int width)
    {
        @Contract("_, _ -> new")
        public @NotNull Entry build(@NotNull CTMBlocks.CTMBlock block, String packName)
        {
            var layout = DirectionalLayoutWidget.horizontal().spacing(5);
            var text = new TextWidget(160, 20 + 2, block.getName(), client.textRenderer);
            var toggleButton = CyclingButtonWidget.onOffBuilder()
                    .omitKeyText()
                    //.initially(CTMBlocks.getCTMBlocks(packName).contains(block))
                    .initially(block.getEnabled())
                    .build(0, 0, 30, 20, Text.empty(),
                            (button, value) -> CTMBlocks.getCTMBlocks(packName).toggle(block)
                    );
            text.alignLeft();
            layout.add(EmptyWidget.ofWidth(15));
            layout.add(text);
            layout.add(toggleButton);
            layout.refreshPositions();
            layout.setX(width / 2 - layout.getWidth() / 2);
            return new Entry(block, layout);
        }
    }

    static class Entry extends ElementListWidget.Entry<Entry>
    {
        private final CTMBlocks.CTMBlock block;
        private final LayoutWidget layout;
        private final List<ClickableWidget> children = Lists.newArrayList();

        Entry(CTMBlocks.CTMBlock block, LayoutWidget layout)
        {
            this.block = block;
            this.layout = layout;
            this.layout.forEachChild(this.children::add);
        }

        @Override
        public List<? extends Selectable> selectableChildren()
        {
            return children;
        }

        @Override
        public List<? extends Element> children()
        {
            return children;
        }

        @Override
        public void render(
                @NotNull DrawContext context, int index, int y, int x,
                int entryWidth, int entryHeight, int mouseX, int mouseY,
                boolean hovered, float delta
        )
        {
            context.drawTexture(block.getIdentifier(), x, y + 2, 0, 0, 16, 16, 16, 16);
            layout.forEachChild(child -> {
                child.setY(y);
                child.render(context, mouseX, mouseY, delta);
            });
        }
    }
}
