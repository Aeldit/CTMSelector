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

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static fr.aeldit.ctms.util.Utils.CTMS_OPTIONS_STORAGE;
import static fr.aeldit.ctms.util.Utils.TEXTURES_HANDLING;

@Environment(EnvType.CLIENT)
public class ResourcePackScreen extends Screen
{
    private final String packName;
    private final Screen parent;
    private OptionListWidget optionList;
    private ListWidget list;
    // Used for when the player uses the escape key to exit the screen, which like the cancel button, reverts the modified but no saved options to their previous value
    private boolean save = false;
    private boolean reset = false;

    public ResourcePackScreen(Screen parent, @NotNull String packName)
    {
        super(CTMS_OPTIONS_STORAGE.getEnabledPacks().contains("file/" + packName.replace(" (folder)", ""))
                ? Text.of(packName.replace(".zip", ""))
                : Text.of(Formatting.ITALIC + packName.replace(".zip", "") + Text.translatable("ctms.screen.packDisabledTitle").getString())
        );
        this.packName = packName;
        this.parent = parent;
    }

    @Override
    public void close()
    {
        if (save)
        {
            if (CTMS_OPTIONS_STORAGE.optionsChanged(packName) || reset)
            {
                CTMS_OPTIONS_STORAGE.setOption(packName);
                TEXTURES_HANDLING.updateUsedTextures(packName);
            }
        }
        save = false;
        CTMS_OPTIONS_STORAGE.clearUnsavedOptionsMap(packName);
        Objects.requireNonNull(client).setScreen(parent);
    }

    @Override
    public void render(DrawContext DrawContext, int mouseX, int mouseY, float delta)
    {
        super.render(DrawContext, mouseX, mouseY, delta);
        DrawContext.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xffffff);
        //optionList.render(DrawContext, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta)
    {
        super.renderBackgroundTexture(context);
    }

    @Override
    protected void init()
    {
        //optionList = new OptionListWidget(client, width, height, 32, height - 32, 25);
        //optionList.addAll(CTMS_OPTIONS_STORAGE.asConfigOptions(packName));
        //addSelectableChild(optionList);

        list = new ListWidget(client, width, height, 32, height - 32, 25);
        addDrawableChild(list);
        CTMBlocks.getAvailableCtmBlocks().stream()
                .sorted(Comparator.comparing(block -> block.getName().getString()))
                .forEach(block -> list.add(block));

        addDrawableChild(
                ButtonWidget.builder(Text.translatable("ctms.screen.config.reset"), button -> {
                            CTMS_OPTIONS_STORAGE.resetOptions(packName);
                            save = reset = true;
                            close();
                        })
                        .tooltip(Tooltip.of(Text.translatable("ctms.screen.config.reset.tooltip")))
                        .dimensions(10, 6, 100, 20)
                        .build()
        );

        addDrawableChild(
                ButtonWidget.builder(ScreenTexts.CANCEL, button -> {
                            save = false;
                            close();
                        })
                        .tooltip(Tooltip.of(Text.translatable("ctms.screen.config.cancel.tooltip")))
                        .dimensions(width / 2 - 154, height - 28, 150, 20)
                        .build()
        );
        addDrawableChild(
                ButtonWidget.builder(Text.translatable("ctms.screen.config.save&quit"), button -> {
                            save = true;
                            close();
                        })
                        .tooltip(Tooltip.of(Text.translatable("ctms.screen.config.save&quit.tooltip")))
                        .dimensions(width / 2 + 4, height - 28, 150, 20)
                        .build()
        );
    }

    private static class ListWidget extends ElementListWidget<Entry>
    {
        private final EntryBuilder builder = new EntryBuilder(client, width);

        public ListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight)
        {
            super(client, width, height, top, bottom, itemHeight);
        }

        public void add(CTMBlocks.CTMBlock block)
        {
            addEntry(builder.build(block));
        }
    }

    private record EntryBuilder(MinecraftClient client, int width)
    {
        @Contract("_ -> new")
        public @NotNull Entry build(@NotNull CTMBlocks.CTMBlock block)
        {
            var layout = DirectionalLayoutWidget.horizontal().spacing(5);
            var text = new TextWidget(160, 20 + 2, block.getName(), client.textRenderer);
            var toggleButton = CyclingButtonWidget.onOffBuilder()
                    .omitKeyText()
                    .initially(CTMBlocks.contains(block))
                    .build(0, 0, 30, 20, Text.empty(), (button, value) -> CTMBlocks.toggle(block));
            text.alignLeft();
            layout.add(EmptyWidget.ofWidth(15));
            layout.add(text);
            layout.add(toggleButton);
            layout.refreshPositions();
            layout.setX(width / 2 - layout.getWidth() / 2);
            return new Entry(block, layout, toggleButton);
        }
    }

    static class Entry extends ElementListWidget.Entry<Entry>
    {
        private final CTMBlocks.CTMBlock block;
        private final LayoutWidget layout;
        private final List<ClickableWidget> children = Lists.newArrayList();
        //private final String blockId;
        private final CyclingButtonWidget<Boolean> button;
        private static final MinecraftClient client = MinecraftClient.getInstance();

        Entry(CTMBlocks.CTMBlock block, LayoutWidget layout, CyclingButtonWidget<Boolean> button)
        {
            this.block = block;
            this.layout = layout;
            this.button = button;
            //this.blockId = Registries.BLOCK.getId(block).toString();
            this.layout.forEachChild(this.children::add);
        }

        public void update()
        {
            button.setValue(CTMBlocks.contains(block));
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
                DrawContext context,
                int index,
                int y,
                int x,
                int entryWidth,
                int entryHeight,
                int mouseX,
                int mouseY,
                boolean hovered,
                float delta)
        {
            context.drawTexture(block.identifier(), x, y + 2, 0, 0, 16, 16, 16, 16);
            layout.forEachChild(child -> {
                child.setY(y);
                child.render(context, mouseX, mouseY, delta);
            });
        }
    }
}
