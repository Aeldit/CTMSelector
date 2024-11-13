package fr.aeldit.ctms.gui;

import fr.aeldit.ctms.gui.widgets.BlocksListWidget;
import fr.aeldit.ctms.textures.FilesHandling;
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class NamespaceBlocksScreen extends Screen
{
    private static final Text TEXT_RESET = Text.translatable("ctms.screen.config.reset");
    private static final Tooltip TOOLTIP_RESET = Tooltip.of(Text.translatable("ctms.screen.config.reset.tooltip"));
    private final Screen parent;
    private final CTMPack ctmPack;
    private final String namespace;

    public NamespaceBlocksScreen(Screen parent, @NotNull CTMPack ctmPack, String namespace)
    {
        super(Text.of(namespace));
        this.parent = parent;
        this.ctmPack = ctmPack;
        this.namespace = namespace;
    }

    @Override
    public void close()
    {
        FilesHandling.updateUsedTextures(ctmPack);
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
        BlocksListWidget list = new BlocksListWidget(
                //? if <1.20.4 {
                /*client, width, height, 32, height - 32, 25,
                 *///?} else {
                client, width, height - 64, 28, 24,
                //?}
                ctmPack
        );
        addDrawableChild(list);

        // Sorts the blocks alphabetically
        ArrayList<CTMBlock> toSort = new ArrayList<>(ctmPack.getCTMBlocksForNamespace(namespace));
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
                            .tooltip(TOOLTIP_RESET)
                            .dimensions(10, 6, 100, 20)
                            .build()
        );

        addDrawableChild(
                ButtonWidget.builder(ScreenTexts.DONE, button -> close())
                            .dimensions(width / 2 - 100, height - 28, 200, 20)
                            .build()
        );
    }
}