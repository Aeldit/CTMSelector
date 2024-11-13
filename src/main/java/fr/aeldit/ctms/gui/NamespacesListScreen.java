package fr.aeldit.ctms.gui;

import fr.aeldit.ctms.gui.widgets.ModListWidget;
import fr.aeldit.ctms.textures.FilesHandling;
import fr.aeldit.ctms.textures.entryTypes.CTMPack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

import static fr.aeldit.ctms.gui.ScreenUtils.TEXT_RESET;
import static fr.aeldit.ctms.gui.ScreenUtils.TOOLTIP_RESET;

@Environment(EnvType.CLIENT)
public class NamespacesListScreen extends Screen
{
    private final Screen parent;
    private final CTMPack ctmPack;

    public NamespacesListScreen(Screen parent, @NotNull CTMPack ctmPack)
    {
        super(Text.of(Formatting.GOLD + ctmPack.getName().replace(".zip", "")
                      + Formatting.RESET + Text.translatable("ctms.screen.byMod.title").getString())
        );
        this.parent = parent;
        this.ctmPack = ctmPack;
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
        ModListWidget list = new ModListWidget(
                //? if >=1.20.4 {
                client, width, height - 64, 28, 32, this
                //?} else {
                /*client, width, height, 32, height - 32, 25, this
                 *///?}
        );
        addDrawableChild(list);

        // Sorts the namespaces alphabetically
        ArrayList<String> toSort = ctmPack.getNamespaces();
        toSort.sort(Comparator.comparing(s -> s));

        for (String namespace : toSort)
        {
            list.add(ctmPack, namespace);
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