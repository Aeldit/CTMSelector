package fr.aeldit.ctms.gui;

import fr.aeldit.ctms.gui.widgets.BlocksListWidget;
import fr.aeldit.ctms.textures.CTMPacks;
import fr.aeldit.ctms.textures.entryTypes.CTMBlock;
import fr.aeldit.ctms.textures.entryTypes.CTMPack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

import static fr.aeldit.ctms.Utils.TEXTURES_HANDLING;

@Environment(EnvType.CLIENT)
public class ResourcePackScreen extends Screen
{
    private static final Text TEXT_GROUPS = Text.translatable("ctms.screen.config.groups");
    private static final Text TEXT_RESET = Text.translatable("ctms.screen.config.reset");
    private static final Text TEXT_MODS = Text.translatable("ctms.screen.config.mods");
    private final Screen parent;
    private final CTMPack ctmPack;
    private final boolean enabled;

    public ResourcePackScreen(Screen parent, @NotNull CTMPack ctmPack)
    {
        super(CTMPacks.getEnabledPacks().contains("file/" + ctmPack.getName())
              ? Text.of(ctmPack.getName().replace(".zip", ""))
              : Text.of(Formatting.ITALIC + ctmPack.getName().replace(".zip", "") + Text.translatable("ctms.screen" +
                                                                                                              ".packDisabledTitle").getString())
        );
        this.parent = parent;
        this.ctmPack = ctmPack;
        this.enabled = CTMPacks.getEnabledPacks().contains("file/" + ctmPack.getName());
    }

    @Override
    public void close()
    {
        if (enabled)
        {
            TEXTURES_HANDLING.updateUsedTextures(ctmPack);
        }
        Objects.requireNonNull(client).setScreen(parent);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta)
    {
        super.render(drawContext, mouseX, mouseY, delta);
        drawContext.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xffffff);
    }

    @Override
    protected void init()
    {
        if (enabled)
        {
            BlocksListWidget list = new BlocksListWidget(
                    //? if <1.20.4 {
                    /*client, width, height, 32, height - 32, 24,
                     *///?} else {
                    client, width, height - 64, 32, 24,
                    //?}
                    ctmPack
            );
            addDrawableChild(list);

            // Sorts the blocks alphabetically
            ArrayList<CTMBlock> toSort = new ArrayList<>(ctmPack.getAllCTMBlocks());
            toSort.sort(Comparator.comparing(block -> block.getPrettyName().getString()));

            for (CTMBlock block : toSort)
            {
                list.add(block);
            }

            addDrawableChild(
                    ButtonWidget.builder(TEXT_RESET, button -> {
                                ctmPack.resetOptions();
                                close();
                            })
                            .tooltip(Tooltip.of(Text.translatable("ctms.screen.config.reset.tooltip")))
                            .dimensions(10, 6, 100, 20)
                            .build()
            );

            addDrawableChild(
                    ButtonWidget.builder(TEXT_GROUPS, button ->
                                    Objects.requireNonNull(client).setScreen(new GroupsScreen(this, ctmPack))
                            )
                            .tooltip(Tooltip.of(Text.translatable("ctms.screen.config.groups.tooltip")))
                            .dimensions(width - 110, 6, 100, 20)
                            .build()
            );

            if (ctmPack.isModded())
            {
                addDrawableChild(
                        ButtonWidget.builder(TEXT_MODS, button ->
                                        Objects.requireNonNull(client).setScreen(new NamespacesListScreen(
                                                this,
                                                ctmPack
                                        ))
                                )
                                .tooltip(Tooltip.of(Text.translatable("ctms.screen.config.mods.tooltip")))
                                .dimensions(width - 110, height - 28, 100, 20)
                                .build()
                );
            }
        }

        addDrawableChild(
                ButtonWidget.builder(ScreenTexts.DONE, button -> close())
                        .dimensions(width / 2 - 100, height - 26, 200, 20)
                        .build()
        );
    }
}