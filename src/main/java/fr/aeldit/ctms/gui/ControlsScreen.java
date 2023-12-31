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
 * in the repo of this mod (https://github.com/Aeldit/CTMSelector)
 */

package fr.aeldit.ctms.gui;

import fr.aeldit.ctms.gui.entryTypes.CTMPack;
import fr.aeldit.ctms.textures.controls.Controls;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class ControlsScreen extends Screen
{
    private final Screen parent;
    private final CTMPack ctmPack;

    public ControlsScreen(Screen parent, @NotNull CTMPack ctmPack)
    {
        super(Text.of(Formatting.GOLD + ctmPack.getName() + Formatting.RESET + Text.translatable("ctms.screen.controls.title").getString()));
        this.parent = parent;
        this.ctmPack = ctmPack;
    }

    @Override
    public void close()
    {
        Objects.requireNonNull(client).setScreen(parent);
    }

    @Override
    public void render(@NotNull DrawContext drawContext, int mouseX, int mouseY, float delta)
    {
        drawContext.drawCenteredTextWithShadow(textRenderer, title, width / 2, 5, 0xffffff);
        super.render(drawContext, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(DrawContext drawContext, int mouseX, int mouseY, float delta)
    {
        super.renderBackgroundTexture(drawContext);
    }

    @Override
    protected void init()
    {
        ControlsListWidget list = new ControlsListWidget(client, width, height, 32, height - 32, 25);
        addDrawableChild(list);

        // Sorts the blocks alphabetically
        List<Controls> toSort = new ArrayList<>(ctmPack.getCtmSelector().getPackControls());
        toSort.sort(Comparator.comparing(controls -> controls.getGroupName().getString()));

        for (Controls controls : toSort)
        {
            list.add(controls);
        }
    }

    /**
     * Modified by me to fit my purpose
     *
     * @author dicedpixels (<a href="https://github.com/dicedpixels">...</a>)
     */
    private static class ControlsListWidget extends ElementListWidget<Entry>
    {
        private final EntryBuilder builder = new EntryBuilder(client, width);

        public ControlsListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight)
        {
            super(client, width, height, top, bottom, itemHeight);
        }

        public void add(Controls controls)
        {
            addEntry(builder.build(controls));
        }
    }

    private record EntryBuilder(MinecraftClient client, int width)
    {
        @Contract("_ -> new")
        public @NotNull Entry build(@NotNull Controls controls)
        {
            var layout = DirectionalLayoutWidget.horizontal().spacing(5);
            var text = new TextWidget(160, 20 + 2, controls.getGroupName(), client.textRenderer);
            var toggleButton = CyclingButtonWidget.onOffBuilder()
                    .omitKeyText()
                    .initially(controls.isEnabled())
                    .build(0, 0, 30, 20, Text.empty(),
                            (button, value) -> controls.toggle()
                    );
            text.alignLeft();
            layout.add(EmptyWidget.ofWidth(15));
            layout.add(text);
            layout.add(toggleButton);
            layout.refreshPositions();
            layout.setX(width / 2 - layout.getWidth() / 2);
            return new Entry(controls, layout);
        }
    }

    static class Entry extends ElementListWidget.Entry<Entry>
    {
        private final Controls controls;
        private final LayoutWidget layout;
        private final List<ClickableWidget> children = Lists.newArrayList();

        Entry(Controls controls, LayoutWidget layout)
        {
            this.controls = controls;
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
            context.drawTexture(controls.getIdentifier(), x, y + 2, 0, 0, 16, 16, 16, 16);
            layout.forEachChild(child -> {
                child.setY(y);
                child.render(context, mouseX, mouseY, delta);
            });
        }
    }
}
