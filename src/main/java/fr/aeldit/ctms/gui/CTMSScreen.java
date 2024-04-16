package fr.aeldit.ctms.gui;

import com.terraformersmc.modmenu.gui.widget.LegacyTexturedButtonWidget;
import fr.aeldit.ctms.textures.CTMPacks;
import fr.aeldit.ctms.textures.entryTypes.CTMPack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static fr.aeldit.ctms.Utils.*;

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
        TEXTURES_HANDLING.load();
        ListWidget list = new ListWidget(client, width, height, 32, height - 32, 25, this);
        addDrawableChild(list);

        ArrayList<CTMPack> toSort = new ArrayList<>(CTM_PACKS.getAvailableCTMPacks());
        // Sorts the blocks alphabetically
        toSort.sort(Comparator.comparing(CTMPack::getName));

        for (CTMPack ctmPack : toSort)
        {
            list.add(ctmPack);
        }

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

        ButtonWidget reloadButton = new LegacyTexturedButtonWidget(width / 2 - 180, height - 28, 20, 20, 0, 0,
                20, new Identifier(CTMS_MODID, "textures/gui/reload.png"), 20, 40,
                button -> {
                    TEXTURES_HANDLING.load();
                    MinecraftClient.getInstance().setScreen(this);
                }
        );
        reloadButton.setTooltip(Tooltip.of(Text.translatable("ctms.screen.reload.tooltip")));
        addDrawableChild(reloadButton);
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

        public ListWidget(
                MinecraftClient client, int width, int height, int top, int bottom, int itemHeight,
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

        public void add(CTMPack ctmPack)
        {
            addEntry(builder.build(ctmPack, parent));
        }
    }

    private record EntryBuilder(MinecraftClient client, int width)
    {
        @Contract("_, _ -> new")
        public @NotNull Entry build(@NotNull CTMPack ctmPack, @NotNull Screen parent)
        {
            var layout = DirectionalLayoutWidget.horizontal().spacing(10);
            var text = new TextWidget(180, 24,
                    CTMPacks.isPackEnabled(ctmPack.getName()) // If the pack is not enabled, it is in italic and gray
                    ? ctmPack.getNameAsText()
                    : Text.of(Formatting.GRAY
                            + Text.of(Formatting.ITALIC
                            + ctmPack.getName()).getString()
                    )
                    , client.textRenderer
            );
            var followButton = ButtonWidget.builder(
                            Text.translatable("ctms.screen.open"),
                            button -> client.setScreen(new ResourcePackScreen(parent, ctmPack))
                    )
                    .dimensions(0, 0, 40, 20)
                    .tooltip(Tooltip.of(Text.translatable("ctms.screen.open.tooltip")))
                    .build();
            text.alignCenter();
            layout.add(text);
            layout.add(followButton);
            layout.refreshPositions();
            layout.setX(width / 2 - layout.getWidth() / 2 + 22);
            return new Entry(ctmPack, layout);
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
            //context.drawTexture(pack.getIdentifier(), x, y, 0, 0, 24, 24, 24, 24);
            layout.forEachChild(child -> {
                child.setY(y);
                child.render(context, mouseX, mouseY, delta);
            });
        }
    }
}
