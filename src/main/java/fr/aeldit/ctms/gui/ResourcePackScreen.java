package fr.aeldit.ctms.gui;

import fr.aeldit.ctms.gui.entryTypes.CTMBlock;
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

import static fr.aeldit.ctms.util.Utils.TEXTURES_HANDLING;

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
                      ".packDisabledTitle").getString())
        );
        this.parent = parent;
        this.ctmPack = ctmPack;
        this.enabled = CTMPacks.getEnabledPacks().contains("file/" + ctmPack.getName());
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
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta)
    {
        super.renderBackgroundTexture(context);
    }

    @Override
    protected void init()
    {
        if (enabled)
        {
            ListWidget list = new ListWidget(client, width, height, 32, height - 32, 25, ctmPack);
            addDrawableChild(list);

            // Sorts the blocks alphabetically
            ArrayList<CTMBlock> toSort = new ArrayList<>(ctmPack.getCtmBlocks());
            toSort.sort(Comparator.comparing(block -> block.getPrettyName().getString()));

            for (CTMBlock block : toSort)
            {
                list.add(block);
            }

            addDrawableChild(
                    ButtonWidget.builder(Text.translatable("ctms.screen.config.reset"), button -> {
                                ctmPack.resetOptions();
                                TEXTURES_HANDLING.updateUsedTextures(ctmPack);
                                close();
                            })
                            .tooltip(Tooltip.of(Text.translatable("ctms.screen.config.reset.tooltip")))
                            .dimensions(10, 6, 75, 20)
                            .build()
            );

            if (ctmPack.hasCtmSelector())
            {
                addDrawableChild(
                        ButtonWidget.builder(Text.translatable("ctms.screen.config.controls"), button ->
                                        Objects.requireNonNull(client).setScreen(new ControlsScreen(this, ctmPack))
                                )
                                .tooltip(Tooltip.of(Text.translatable("ctms.screen.config.controls.tooltip")))
                                .dimensions(width - 110, 6, 100, 20)
                                .build()
                );
            }

            addDrawableChild(
                    ButtonWidget.builder(ScreenTexts.DONE, button -> {
                                TEXTURES_HANDLING.updateUsedTextures(ctmPack);
                                close();
                            })
                            .dimensions(width / 2 - 100, height - 28, 200, 20)
                            .build()
            );
        }
        else
        {
            addDrawableChild(
                    ButtonWidget.builder(ScreenTexts.DONE, button -> close())
                            .dimensions(width / 2 - 75, height - 28, 150, 20)
                            .build()
            );
        }
    }

    /**
     * Modified by me to fit my purpose
     *
     * @author dicedpixels (<a href="https://github.com/dicedpixels">...</a>)
     */
    private static class ListWidget extends ElementListWidget<Entry>
    {
        private final EntryBuilder builder = new EntryBuilder(client, width);
        private final CTMPack ctmPack;

        public ListWidget(
                MinecraftClient client, int width, int height, int top, int bottom, int itemHeight, CTMPack ctmPack
        )
        {
            super(client, width, height, top, bottom, itemHeight);
            this.ctmPack = ctmPack;
        }

        public void add(CTMBlock block)
        {
            addEntry(builder.build(block, ctmPack));
        }
    }

    private record EntryBuilder(MinecraftClient client, int width)
    {
        @Contract("_, _ -> new")
        public @NotNull Entry build(@NotNull CTMBlock block, @NotNull CTMPack ctmPack)
        {
            var layout = DirectionalLayoutWidget.horizontal().spacing(5);
            var text = new TextWidget(160, 20 + 2, ctmPack.isBlockDisabledFromGroup(block)
                                                   ?
                                                   Text.of(Formatting.RED + Text.of(Formatting.ITALIC + block.getPrettyName().getString()).getString())
                                                   : block.getPrettyName(),
                    client.textRenderer
            );
            var toggleButton = CyclingButtonWidget.onOffBuilder()
                    .omitKeyText()
                    .initially(block.isEnabled())
                    .build(0, 0, 30, 20, Text.empty(),
                            (button, value) -> ctmPack.toggle(block)
                    );
            toggleButton.setTooltip(ctmPack.isBlockDisabledFromGroup(block)
                                    ? Tooltip.of(Text.translatable("ctms.screen.block.parentControlIsDisabled"))
                                    : Tooltip.of(Text.empty())
            );
            text.alignLeft();
            layout.add(EmptyWidget.ofWidth(15));
            layout.add(text);
            layout.add(toggleButton);
            layout.refreshPositions();
            layout.setX(width / 2 - layout.getWidth() / 2);
            return new Entry(block, layout);
        }
    }

    static class Entry extends ElementListWidget.Entry<Entry>
    {
        private final CTMBlock block;
        private final LayoutWidget layout;
        private final List<ClickableWidget> children = Lists.newArrayList();

        Entry(CTMBlock block, LayoutWidget layout)
        {
            this.block = block;
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
            context.drawTexture(block.getIdentifier(), x, y + 2, 0, 0, 16, 16, 16, 16);
            layout.forEachChild(child -> {
                child.setY(y);
                child.render(context, mouseX, mouseY, delta);
            });
        }
    }
}
