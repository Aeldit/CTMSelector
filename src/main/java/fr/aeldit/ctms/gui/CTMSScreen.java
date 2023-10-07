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

import com.terraformersmc.modmenu.gui.widget.LegacyTexturedButtonWidget;
import fr.aeldit.ctms.textures.CTMBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static fr.aeldit.ctms.textures.CTMBlocks.CTM_BLOCKS_MAP;
import static fr.aeldit.ctms.util.Utils.CTMS_MODID;
import static fr.aeldit.ctms.util.Utils.TEXTURES_HANDLING;

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
    public void render(DrawContext context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 5, 0xffffff);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta)
    {
        super.renderBackgroundTexture(context);
    }

    @Override
    protected void init()
    {
        int i = 0;
        List<String> sortedPacksNames = new ArrayList<>(CTM_BLOCKS_MAP.keySet());
        Collections.sort(sortedPacksNames);

        for (String packName : sortedPacksNames)
        {
            // Temporary, will add a page system at some point
            if (i < 6)
            {
                if (CTMBlocks.getEnabledPacks().contains("file/" + packName.replace(" (folder)", "")))
                {
                    addDrawableChild(
                            ButtonWidget.builder(Text.of(packName.replace(".zip", "")),
                                            button -> Objects.requireNonNull(client).setScreen(new ResourcePackScreen(
                                                            this,
                                                            packName
                                                    )
                                            )
                                    )
                                    .dimensions(width / 2 - 205, 30 + 20 * i + 10 * i, 200, 20)
                                    .build()
                    );
                }
                else
                {
                    addDrawableChild(
                            ButtonWidget.builder(Text.of(Formatting.ITALIC + packName.replace(".zip", "")),
                                            button -> Objects.requireNonNull(client).setScreen(new ResourcePackScreen(
                                                            this,
                                                            packName
                                                    )
                                            )
                                    )
                                    .tooltip(Tooltip.of(Text.translatable("ctms.screen.packDisabled.tooltip")))
                                    .dimensions(width / 2 - 205, 30 + 20 * i + 10 * i, 200, 20)
                                    .build()
                    );
                }
            }
            i++;
        }

        addDrawableChild(getReloadButton()); // TODO -> Fix textures

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

        addDrawableChild(getControlsButton());
    }

    private @NotNull ButtonWidget getReloadButton()
    {
        ButtonWidget reloadButton = new LegacyTexturedButtonWidget(width / 2 - 180, height - 28, 20, 20, 0, 0, 20,
                new Identifier(CTMS_MODID, "textures/gui/reload.png"), 20, 40,
                (button) -> {
                    TEXTURES_HANDLING.load();
                    MinecraftClient.getInstance().setScreen(this);
                });
        reloadButton.setTooltip(Tooltip.of(Text.translatable("ctms.screen.reload.tooltip")));
        return reloadButton;
    }

    private @NotNull ButtonWidget getControlsButton()
    {
        ButtonWidget controlsButton = new LegacyTexturedButtonWidget(width / 2 + 160, height - 28, 20, 20, 0, 0, 20,
                new Identifier(CTMS_MODID, "textures/gui/controls.png"), 20, 40,
                (button) -> MinecraftClient.getInstance().setScreen(new ControlsScreen(this))
        );
        controlsButton.setTooltip(Tooltip.of(Text.translatable("ctms.screen.controls.tooltip")));
        return controlsButton;
    }
}
