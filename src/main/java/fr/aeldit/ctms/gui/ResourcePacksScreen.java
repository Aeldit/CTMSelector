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

import fr.aeldit.ctms.config.CTMSOptionsStorage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class ResourcePacksScreen extends Screen
{
    private final String packName;
    private final CTMSOptionsStorage optionsStorage;
    private final Screen parent;
    private final ArrayList<CTMSOptionsStorage.BooleanOption> options = new ArrayList<>();
    private OptionListWidget optionList;
    // Used for when the player uses the escape key to exit the screen, which like the cancel button, reverts the modified but no saved options to their previous value
    private boolean save = false;

    public ResourcePacksScreen(@NotNull CTMSOptionsStorage optionsStorage, Screen parent, String packName, ArrayList<CTMSOptionsStorage.BooleanOption> options)
    {
        super(Text.translatable("ctms.screen.%s.title".formatted(packName)));
        this.packName = packName;
        this.optionsStorage = optionsStorage;
        this.parent = parent;
        this.options.addAll(options);
    }

    @Override
    public void close()
    {
        if (!save)
        {
            if (!optionsStorage.getUnsavedChangedOptions().isEmpty())
            {
                for (Map.Entry<String, Boolean> entry : optionsStorage.getUnsavedChangedOptions().get(packName).entrySet())
                {
                    optionsStorage.setBooleanOption(packName, entry.getKey(), entry.getValue());
                }
            }
        }
        save = false;
        optionsStorage.writeConfig();
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
        optionList.addAll(CTMSOptionsStorage.asConfigOptions(options));
        addSelectableChild(optionList);

        addDrawableChild(
                ButtonWidget.builder(Text.translatable("cyanlib.screen.config.reset"), button -> {
                            optionsStorage.resetOptions(packName);
                            save = true;
                            close();
                        })
                        .tooltip(Tooltip.of(Text.translatable("cyanlib.screen.config.reset.tooltip")))
                        .dimensions(10, 6, 100, 20)
                        .build()
        );

        addDrawableChild(
                ButtonWidget.builder(ScreenTexts.CANCEL, button -> {
                            save = false;
                            close();
                        })
                        .tooltip(Tooltip.of(Text.translatable("cyanlib.screen.config.cancel.tooltip")))
                        .dimensions(width / 2 - 154, height - 28, 150, 20)
                        .build()
        );
        addDrawableChild(
                ButtonWidget.builder(Text.translatable("cyanlib.screen.config.save&quit"), button -> {
                            save = true;
                            close();
                        })
                        .dimensions(width / 2 + 4, height - 28, 150, 20)
                        .build()
        );
    }
}
