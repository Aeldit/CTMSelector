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
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static fr.aeldit.ctms.util.Utils.CTMS_OPTIONS_STORAGE;
import static fr.aeldit.ctms.util.Utils.TEXTURES_HANDLING;

@Environment(EnvType.CLIENT)
public class ResourcePackScreen extends Screen
{
    private final String packName;
    private final Screen parent;
    private OptionListWidget optionList;
    // Used for when the player uses the escape key to exit the screen, which like the cancel button, reverts the modified but no saved options to their previous value
    private boolean save = false;
    private boolean reset = false;

    public ResourcePackScreen(Screen parent, @NotNull String packName)
    {
        super(CTMS_OPTIONS_STORAGE.getEnabledPacks().contains("file/" + packName)
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
        renderBackgroundTexture(DrawContext);
        optionList.render(DrawContext, mouseX, mouseY, delta);
        DrawContext.drawCenteredTextWithShadow(textRenderer, title, width / 2, 5, 0xffffff);
        super.render(DrawContext, mouseX, mouseY, delta);
    }

    @Override
    protected void init()
    {
        optionList = new OptionListWidget(client, width, height, 32, height - 32, 25);
        optionList.addAll(CTMS_OPTIONS_STORAGE.asConfigOptions(packName));
        addSelectableChild(optionList);

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
}
