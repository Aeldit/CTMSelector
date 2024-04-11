package fr.aeldit.ctms.gui;

import fr.aeldit.ctms.gui.entryTypes.CTMPack;
import fr.aeldit.ctms.textures.CTMSelector;
import fr.aeldit.ctms.textures.Controls;
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
public class ControlsScreen extends Screen
{
    private final Screen parent;
    private final CTMPack ctmPack;

    public ControlsScreen(Screen parent, @NotNull CTMPack ctmPack)
    {
        super(Text.of(Formatting.GOLD + ctmPack.getName() + Formatting.RESET + Text.translatable("ctms.screen" +
                ".controls.title").getString()));
        this.parent = parent;
        this.ctmPack = ctmPack;
    }

    @Override
    public void close()
    {
        Objects.requireNonNull(client).setScreen(parent);
    }

    @Override
    public void render(@NotNull DrawContext drawContext, int mouseX, int mouseY, float delta)
    {
        super.render(drawContext, mouseX, mouseY, delta);
        drawContext.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xffffff);
    }

    @Override
    public void renderBackground(DrawContext drawContext, int mouseX, int mouseY, float delta)
    {
        super.renderBackgroundTexture(drawContext);
    }

    @Override
    protected void init()
    {
        CTMSelector ctmSelector = ctmPack.getCtmSelector();

        ControlsListWidget list = new ControlsListWidget(client, width, height, 32, height - 32, 25, ctmSelector);
        addDrawableChild(list);

        // Sorts the blocks alphabetically
        ArrayList<Controls> toSort = new ArrayList<>(ctmSelector.getControls());
        toSort.sort(Comparator.comparing(Controls::getGroupName));

        for (Controls controls : toSort)
        {
            list.add(controls);
        }

        addDrawableChild(
                ButtonWidget.builder(Text.translatable("ctms.screen.config.reset"), button -> {
                            ctmSelector.resetOptions();
                            ctmSelector.updateControlsStates();
                            TEXTURES_HANDLING.updateUsedTextures(ctmPack);
                            close();
                        })
                        .tooltip(Tooltip.of(Text.translatable("ctms.screen.config.reset.tooltip")))
                        .dimensions(10, 6, 75, 20)
                        .build()
        );

        addDrawableChild(
                ButtonWidget.builder(ScreenTexts.DONE, button -> {
                            ctmSelector.updateControlsStates();
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
    private static class ControlsListWidget extends ElementListWidget<Entry>
    {
        private final EntryBuilder builder = new EntryBuilder(client, width);
        private final CTMSelector ctmSelector;

        public ControlsListWidget(
                MinecraftClient client, int width, int height, int top, int bottom, int itemHeight,
                CTMSelector ctmSelector
        )
        {
            super(client, width, height, top, bottom, itemHeight);
            this.ctmSelector = ctmSelector;
        }

        public void add(Controls controls)
        {
            addEntry(builder.build(ctmSelector, controls));
        }
    }

    private record EntryBuilder(MinecraftClient client, int width)
    {
        @Contract("_, _ -> new")
        public @NotNull Entry build(
                CTMSelector ctmSelector, @NotNull Controls controls
        )
        {
            var layout = DirectionalLayoutWidget.horizontal().spacing(5);
            var text = new TextWidget(160, 20 + 2, controls.getGroupNameAsText(), client.textRenderer);
            var toggleButton = CyclingButtonWidget.onOffBuilder()
                    .omitKeyText()
                    .initially(controls.isEnabled())
                    .build(0, 0, 30, 20, Text.empty(),
                            (button, value) -> ctmSelector.toggle(controls)
                    );
            toggleButton.setTooltip(Tooltip.of(controls.getButtonTooltip()));
            text.alignLeft();
            layout.add(EmptyWidget.ofWidth(15));
            layout.add(text);
            layout.add(toggleButton);
            layout.refreshPositions();
            layout.setX(width / 2 - layout.getWidth() / 2);
            return new Entry(controls, layout);
        }
    }

    static class Entry extends ElementListWidget.Entry<Entry>
    {
        private final Controls controls;
        private final LayoutWidget layout;
        private final List<ClickableWidget> children = Lists.newArrayList();

        Entry(Controls controls, LayoutWidget layout)
        {
            this.controls = controls;
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
            context.drawTexture(controls.getIdentifier(), x, y + 2, 0, 0, 16, 16, 16, 16);
            layout.forEachChild(child -> {
                child.setY(y);
                child.render(context, mouseX, mouseY, delta);
            });
        }
    }
}
