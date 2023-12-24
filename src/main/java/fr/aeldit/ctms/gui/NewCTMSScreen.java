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

import com.terraformersmc.modmenu.gui.widget.LegacyTexturedButtonWidget;
import fr.aeldit.ctms.gui.entryTypes.CTMPack;
import fr.aeldit.ctms.textures.CTMPacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static fr.aeldit.ctms.util.Utils.CTMS_MODID;
import static fr.aeldit.ctms.util.Utils.TEXTURES_HANDLING;

@Environment(EnvType.CLIENT)
public class NewCTMSScreen extends Screen
{
    private final Screen parent;

    public NewCTMSScreen(Screen parent)
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
        ListWidget list = new ListWidget(client, width, height, 32, height - 32, 25, this);
        addDrawableChild(list);

        List<CTMPack> toSort = new ArrayList<>(CTMPacks.getAvailableCtmPacks());
        // Sorts the blocks alphabetically
        toSort.sort(Comparator.comparing(CTMPack::getNameAsString));

        for (CTMPack ctmPack : toSort)
        {
            list.add(ctmPack);
        }

        addDrawableChild(
                ButtonWidget.builder(Text.translatable("ctms.screen.done"), button -> close())
                        .dimensions(width / 2 - 75, height - 28, 150, 20)
                        .build()
        );

        addDrawableChild(getReloadButton());
        addDrawableChild(getControlsButton());
    }

    /*@Override
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
                if (CTMPacks.getEnabledPacks().contains("file/" + packName.replace(" (folder)", "")))
                {
                    addDrawableChild(
                            ButtonWidget.builder(Text.of(packName.replace(".zip", "")),
                                            button -> client.setScreen(new ResourcePackScreen(
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
                    /*
                    When the pack is not loaded, we send a toast to the player
                    This is because when a pack is not loaded, all textures
                    that are not in minecraft appear as a bugged texture
                     *
                    addDrawableChild(
                            ButtonWidget.builder(Text.of(Formatting.ITALIC + packName.replace(".zip", "")),
                                            button -> client.getToastManager().add(new PackNotEnabledToast(
                                                    PackNotEnabledToast.Type.PACK_NOT_ENABLED,
                                                    Text.translatable("ctms.toast.packNotEnabled"),
                                                    null
                                            ))
                                    )
                                    .tooltip(Tooltip.of(Text.translatable("ctms.screen.packDisabled.tooltip")))
                                    .dimensions(width / 2 - 205, 30 + 20 * i + 10 * i, 200, 20)
                                    .build()
                    );
                }
            }
            i++;
        }

        addDrawableChild(getReloadButton());

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
    }*/

    private @NotNull ButtonWidget getReloadButton()
    {
        ButtonWidget reloadButton = new LegacyTexturedButtonWidget(width / 2 - 180, height - 28, 20, 20, 0, 0,
                20, new Identifier(CTMS_MODID, "textures/gui/reload.png"), 20, 40,
                (button) -> {
                    TEXTURES_HANDLING.load();
                    MinecraftClient.getInstance().setScreen(this);
                });
        reloadButton.setTooltip(Tooltip.of(Text.translatable("ctms.screen.reload.tooltip")));
        return reloadButton;
    }

    private @NotNull ButtonWidget getControlsButton()
    {
        ButtonWidget controlsButton = new LegacyTexturedButtonWidget(width / 2 + 160, height - 28, 20, 20, 0, 0,
                20, new Identifier(CTMS_MODID, "textures/gui/controls.png"), 20, 40,
                (button) -> MinecraftClient.getInstance().setScreen(new ControlsScreen(this))
        );
        controlsButton.setTooltip(Tooltip.of(Text.translatable("ctms.screen.controls.tooltip")));
        return controlsButton;
    }

    /**
     * Modified by me to fit my purpose
     *
     * @author dicedpixels (<a href="https://github.com/dicedpixels">...</a>)
     */
    private static class ListWidget extends ElementListWidget<Entry>
    {
        private final EntryBuilder builder = new EntryBuilder(client, width);
        private final Screen parent;

        public ListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight,
                          Screen parent
        )
        {
            super(client, width, height, top, bottom, itemHeight);
            this.parent = parent;
        }

        @Override
        protected int getScrollbarPositionX()
        {
            return this.width / 2 + 160;
        }

        @Override
        public int getRowWidth()
        {
            return 280;
        }

        public void add(CTMPack pack)
        {
            addEntry(builder.build(pack, parent));
        }
    }

    private record EntryBuilder(MinecraftClient client, int width)
    {
        @Contract("_, _ -> new")
        public @NotNull Entry build(@NotNull CTMPack pack, @NotNull Screen parent)
        {
            var layout = DirectionalLayoutWidget.horizontal().spacing(10);
            var text = new TextWidget(180, 24, pack.getName(), client.textRenderer);
            var followButton = ButtonWidget.builder(
                            Text.translatable("ctms.screen.open"),
                            button -> client.setScreen(new ResourcePackScreen(parent, pack.getNameAsString()))
                    )
                    .dimensions(0, 0, 40, 20)
                    .tooltip(Tooltip.of(Text.translatable("ctms.screen.open.tooltip")))
                    .build();
            text.alignCenter();
            layout.add(text);
            layout.add(followButton);
            layout.refreshPositions();
            layout.setX(width / 2 - layout.getWidth() / 2 + 22);
            return new Entry(pack, layout);
        }
    }

    static class Entry extends ElementListWidget.Entry<Entry>
    {
        private final CTMPack pack;
        private final LayoutWidget layout;
        private final List<ClickableWidget> children = Lists.newArrayList();

        Entry(CTMPack pack, LayoutWidget layout)
        {
            this.pack = pack;
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
            context.drawTexture(pack.getIdentifier(), x, y, 0, 0, 24, 24, 24, 24);
            layout.forEachChild(child -> {
                child.setY(y);
                child.render(context, mouseX, mouseY, delta);
            });
        }
    }
}
