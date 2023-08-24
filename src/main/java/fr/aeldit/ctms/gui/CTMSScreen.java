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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static fr.aeldit.ctms.util.Utils.CTMS_OPTIONS_STORAGE;

@Environment(EnvType.CLIENT)
public class CTMSScreen extends Screen
{
    private final Screen parent;

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
        //resourcePacksListWidgetList.render(DrawContext, mouseX, mouseY, delta);
        DrawContext.drawCenteredTextWithShadow(textRenderer, title, width / 2, 5, 0xffffff);
        super.render(DrawContext, mouseX, mouseY, delta);
    }

    @Override
    protected void init()
    {
        int i = 0;
        List<String> sortedPacksNames = new ArrayList<>(CTMS_OPTIONS_STORAGE.getOptionsMaps().keySet());
        Collections.sort(sortedPacksNames);

        for (String packName : sortedPacksNames)
        {
            // Temporary, will add a page system at some point
            if (i < 6)
            {
                addDrawableChild(
                        ButtonWidget.builder(Text.of(packName.replace(".zip", "")),
                                        button -> Objects.requireNonNull(client).setScreen(new ResourcePackScreen(
                                                        parent,
                                                        packName
                                                )
                                        )
                                )
                                .dimensions(30, 30 + 20 * i + 10 * i, 200, 20)
                                .build()
                );
            }
            else
            {
                addDrawableChild(
                        ButtonWidget.builder(Text.of(packName.replace(".zip", "")),
                                        button -> Objects.requireNonNull(client).setScreen(new ResourcePackScreen(
                                                parent,
                                                packName
                                        ))
                                )
                                .dimensions(width - 180, 30 + 30 * (i - 6), 200, 20)
                                .build()
                );
            }
            i++;
        }

        addDrawableChild(
                ButtonWidget.builder(Text.translatable("ctms.screen.openResourcePacksFolder"),
                                button -> Util.getOperatingSystem().open(new File(FabricLoader.getInstance().getGameDir().toFile(), "resourcepacks"))
                        )
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
