package fr.aeldit.ctms.gui;

import fr.aeldit.ctms.textures.entryTypes.CTMPack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static fr.aeldit.ctms.Utils.TEXTURES_HANDLING;

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
                /*client, width, height - 64, 28, 32, this
                 *///?} else {
                client, width, height, 32, height - 32, 25, this
                //?}
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
                ButtonWidget.builder(Text.translatable("ctms.screen.config.reset"), button -> {
                            ctmPack.resetOptions();
                            TEXTURES_HANDLING.updateUsedTextures(ctmPack);
                            close();
                        })
                        .tooltip(Tooltip.of(Text.translatable("ctms.screen.config.reset.tooltip")))
                        .dimensions(10, 6, 100, 20)
                        .build()
        );

        addDrawableChild(
                ButtonWidget.builder(ScreenTexts.DONE, button -> {
                            TEXTURES_HANDLING.updateUsedTextures(ctmPack);
                            close();
                        })
                        .dimensions(width / 2 - 100, height - 28, 200, 20)
                        .build()
        );
    }

    /**
     * Modified by me to fit my purpose
     *
     * @author dicedpixels (<a href="https://github.com/dicedpixels">...</a>)
     */
    private static class ModListWidget extends ElementListWidget<ModEntry>
    {
        private final ModEntryBuilder builder = new ModEntryBuilder(client, width);
        private final Screen parent;


        //? if >=1.20.4 {
        /*public ModListWidget(MinecraftClient client, int width, int height, int y, int itemHeight, Screen parent)
        {
            super(client, width, height, y, itemHeight);
            this.parent = parent;
        }
        *///?} else {
        public ModListWidget(
                MinecraftClient client, int width, int height, int top, int bottom, int itemHeight,
                Screen parent
        )
        {
            super(client, width, height, top, bottom, itemHeight);
            this.parent = parent;
        }
        //?}

        @Contract(pure = true)
        @Override
        protected int getScrollbarPositionX()
        {
            return this.width / 2 + 160;
        }

        @Contract(pure = true)
        @Override
        public int getRowWidth()
        {
            return 300;
        }

        public void add(CTMPack ctmPack, String namespace)
        {
            addEntry(builder.build(ctmPack, namespace, parent));
        }
    }

    private record ModEntryBuilder(MinecraftClient client, int width)
    {
        @Contract("_, _, _ -> new")
        public NamespacesListScreen.@NotNull ModEntry build(CTMPack ctmPack, String namespace, @NotNull Screen parent)
        {
            var layout = DirectionalLayoutWidget.horizontal().spacing(10);
            var text = new TextWidget(180, 24, Text.of(namespace), client.textRenderer);
            var followButton = ButtonWidget.builder(
                            Text.translatable("ctms.screen.open"),
                            button -> client.setScreen(new NamespaceBlocksScreen(parent, ctmPack, namespace))
                    )
                    .dimensions(0, 0, 40, 20)
                    .tooltip(Tooltip.of(Text.translatable("ctms.screen.open.tooltip")))
                    .build();
            text.alignCenter();
            layout.add(text);
            layout.add(followButton);
            layout.refreshPositions();
            layout.setX(width / 2 - layout.getWidth() / 2 + 22);
            return new ModEntry(layout);
        }
    }

    static class ModEntry extends ElementListWidget.Entry<ModEntry>
    {
        private final LayoutWidget layout;
        private final List<ClickableWidget> children = Lists.newArrayList();

        ModEntry(LayoutWidget layout)
        {
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
            layout.forEachChild(child -> {
                child.setY(y);
                child.render(context, mouseX, mouseY, delta);
            });
        }
    }
}