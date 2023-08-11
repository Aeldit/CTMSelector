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

import fr.aeldit.ctms.gui.widgets.ResourcePacksListWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.Objects;

import static fr.aeldit.ctms.textures.ConnectedTexturesHandling.getCtmResourcePacks;

@Environment(EnvType.CLIENT)
public class CTMSScreen extends Screen
{
    private final Screen parent;
    private ResourcePacksListWidget resourcePacksListWidgetList;

    public CTMSScreen(Screen parent)
    {
        super(Text.translatable("ctms.screen.title"));
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
        renderBackgroundTexture(DrawContext);
        resourcePacksListWidgetList.render(DrawContext, mouseX, mouseY, delta);
        DrawContext.drawCenteredTextWithShadow(textRenderer, title, width / 2, 5, 0xffffff);
        super.render(DrawContext, mouseX, mouseY, delta);
    }

    @Override
    protected void init()
    {
        resourcePacksListWidgetList = new ResourcePacksListWidget(client, width / 2 - 8, height, 67, height - 36, 25);
        resourcePacksListWidgetList.addAll(getCtmResourcePacks());
        addSelectableChild(resourcePacksListWidgetList);

        /*int i = 0;
        List<String> sortedPacksNames = new ArrayList<>(CTMS_OPTIONS_STORAGE.getBooleanOptions().keySet());
        Collections.sort(sortedPacksNames);

        for (String packName : sortedPacksNames)
        {
            // Temporary, will add a page system at some point
            if (i < 6)
            {
                addDrawableChild(
                        ButtonWidget.builder(Text.of(packName),
                                        button -> Objects.requireNonNull(client).setScreen(new ResourcePacksScreen(
                                                        CTMS_OPTIONS_STORAGE,
                                                        parent,
                                                        packName,
                                                        CTMS_OPTIONS_STORAGE.getDefaultBooleanOptions().get(packName)
                                                )
                                        )
                                )
                                .dimensions(30, 30 + 20 * i + 10 * i, 150, 20)
                                .build()
                );
            }
            else
            {
                addDrawableChild(
                        ButtonWidget.builder(Text.of(packName),
                                        button -> Objects.requireNonNull(client).setScreen(new ResourcePacksScreen(
                                                CTMS_OPTIONS_STORAGE,
                                                parent,
                                                packName,
                                                CTMS_OPTIONS_STORAGE.getDefaultBooleanOptions().get(packName)
                                        ))
                                )
                                .dimensions(width - 180, 30 + 30 * (i - 6), 150, 20)
                                .build()
                );
            }
            i++;
        }*/

        addDrawableChild(
                ButtonWidget.builder(Text.translatable("ctms.screen.openResourcePacksFolder"), button -> close())
                        .dimensions(width / 2 - 154, height - 28, 150, 20)
                        .build()
        );

        addDrawableChild(
                ButtonWidget.builder(ScreenTexts.DONE, button -> close())
                        .dimensions(width / 2 + 4, height - 28, 150, 20)
                        .build()
        );
    }
}
