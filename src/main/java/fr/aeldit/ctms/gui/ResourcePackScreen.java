package fr.aeldit.ctms.gui;

import fr.aeldit.ctms.gui.widgets.BlocksListWidget;
import fr.aeldit.ctms.textures.CTMPacks;
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

import java.util.Comparator;
import java.util.Objects;

import static fr.aeldit.ctms.Utils.TEXTURES_HANDLING;

@Environment(EnvType.CLIENT)
public class ResourcePackScreen extends Screen
{
    private final Screen parent;
    private final CTMPack ctmPack;
    private final boolean enabled;

    public ResourcePackScreen(Screen parent, @NotNull CTMPack ctmPack)
    {
        super(CTMPacks.getEnabledPacks().contains("file/" + ctmPack.getName())
              ? Text.of(ctmPack.getName().replace(".zip", ""))
              : Text.of(Formatting.ITALIC + ctmPack.getName().replace(".zip", "") + Text.translatable("ctms.screen" +
                                                                                                      ".packDisabledTitle")
                                                                                        .getString())
        );
        this.parent  = parent;
        this.ctmPack = ctmPack;
        this.enabled = CTMPacks.getEnabledPacks().contains("file/" + ctmPack.getName());
    }

    @Override
    public void close()
    {
        if (enabled)
        {
            TEXTURES_HANDLING.updatePropertiesFiles(ctmPack);
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
                    /*client, width, height, 32, height - 32, 25,
                     *///?} else {
                    client, width, height - 64, 28, 24,
                    //?}
                    ctmPack
            );
            addDrawableChild(list);

            ctmPack.getCTMBlocks().stream()
                   .sorted(Comparator.comparing(ctmBlock -> ctmBlock.getPrettyName().getString()))
                   .forEachOrdered(list::add);

            addDrawableChild(
                    ButtonWidget.builder(
                                        Text.translatable("ctms.screen.config.reset"), button -> {
                                            ctmPack.resetOptions();
                                            close();
                                        }
                                )
                                .tooltip(Tooltip.of(Text.translatable("ctms.screen.config.reset.tooltip")))
                                .dimensions(10, 6, 100, 20)
                                .build()
            );

            if (ctmPack.hasCtmSelector())
            {
                addDrawableChild(
                        ButtonWidget.builder(
                                            Text.translatable("ctms.screen.config.controls"), button ->
                                                    Objects.requireNonNull(client).setScreen(new GroupsScreen(
                                                            this,
                                                            ctmPack
                                                    ))
                                    )
                                    .tooltip(Tooltip.of(Text.translatable("ctms.screen.config.controls.tooltip")))
                                    .dimensions(width - 110, 6, 100, 20)
                                    .build()
                );
            }

            if (ctmPack.isModded())
            {
                addDrawableChild(
                        ButtonWidget.builder(
                                            Text.translatable("ctms.screen.config.mods"), button ->
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
                            .dimensions(width / 2 - 100, height - 28, 200, 20)
                            .build()
        );
    }
}