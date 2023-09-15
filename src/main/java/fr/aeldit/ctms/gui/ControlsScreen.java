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
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static fr.aeldit.ctms.util.Utils.CTMS_OPTIONS_STORAGE;

@Environment(EnvType.CLIENT)
public class ControlsScreen extends Screen
{
    private final String packName;
    private final Screen parent;
    private OptionListWidget optionList;

    public ControlsScreen(Screen parent, @NotNull String packName)
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
    }
}
