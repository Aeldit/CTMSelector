package fr.aeldit.ctms.gui;

import fr.aeldit.ctms.textures.CTMSelector;
import fr.aeldit.ctms.textures.Group;
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
import static fr.aeldit.ctms.gui.ScreenUtils.TEXT_RESET;
import static fr.aeldit.ctms.gui.ScreenUtils.TOOLTIP_RESET;

@Environment(EnvType.CLIENT)
public class GroupsScreen extends Screen
{
    private final Screen parent;
    private final CTMPack ctmPack;
    private final CTMSelector ctmSelector;

    public GroupsScreen(Screen parent, @NotNull CTMPack ctmPack)
    {
        super(Text.of(
                Formatting.GOLD + ctmPack.getName()
                        + Formatting.RESET
                        + (ctmPack.getName().endsWith("s") ? " " : "'s ")
                        + Text.translatable("ctms.screen.group.title").getString())
        );
        this.parent = parent;
        this.ctmPack = ctmPack;
        this.ctmSelector = ctmPack.getCtmSelector();
    }

    @Override
    public void close()
    {
        ctmSelector.updateGroupsStates();
        TEXTURES_HANDLING.updateUsedTextures(ctmPack);
        Objects.requireNonNull(client).setScreen(parent);
    }

    @Override
    public void render(@NotNull DrawContext drawContext, int mouseX, int mouseY, float delta)
    {
        super.render(drawContext, mouseX, mouseY, delta);
        drawContext.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xffffff);
    }

    //? if <1.20.6 {
    /*@Override
    public void renderBackground(DrawContext drawContext, int mouseX, int mouseY, float delta)
    {
        super.renderBackgroundTexture(drawContext);
    }
    *///?}

    @Override
    protected void init()
    {
        GroupsListWidget list = new GroupsListWidget(
                //? if <1.20.4 {
                /*client, width, height, 32, height - 32, 24
                 *///?} else {
                client, width, height - 64, 32, 24
                //?}
        );
        addDrawableChild(list);

        // Sorts the blocks alphabetically
        ArrayList<Group> toSort = new ArrayList<>(ctmSelector.getGroups());
        toSort.sort(Comparator.comparing(Group::getGroupName));

        for (Group group : toSort)
        {
            list.add(group);
        }

        addDrawableChild(
                ButtonWidget.builder(TEXT_RESET, button -> {
                            ctmSelector.resetOptions();
                            close();
                        })
                        .tooltip(TOOLTIP_RESET)
                        .dimensions(10, 6, 100, 20)
                        .build()
        );

        addDrawableChild(
                ButtonWidget.builder(ScreenTexts.DONE, button -> close())
                        .dimensions(width / 2 - 100, height - 26, 200, 20)
                        .build()
        );
    }

    /**
     * Modified by me to fit my purpose
     *
     * @author dicedpixels (<a href="https://github.com/dicedpixels">...</a>)
     */
    private static class GroupsListWidget extends ElementListWidget<GroupEntry>
    {
        private final GroupEntryBuilder builder = new GroupEntryBuilder(client, width);

        //? if <1.20.4 {
        /*public GroupsListWidget(
                MinecraftClient client, int width, int height, int top, int bottom, int itemHeight,
                CTMSelector ctmSelector
        )
        {
            super(client, width, height, top, bottom, itemHeight);
            this.ctmSelector = ctmSelector;
        }

        *///?} else {
        public GroupsListWidget(
                MinecraftClient client, int width, int height, int y, int itemHeight
        )
        {
            super(client, width, height, y, itemHeight);
        }
        //?}

        public void add(Group group)
        {
            addEntry(builder.build(group));
        }
    }

    private record GroupEntryBuilder(MinecraftClient client, int width)
    {
        @Contract("_ -> new")
        public @NotNull GroupsScreen.GroupEntry build(@NotNull Group group)
        {
            var layout = DirectionalLayoutWidget.horizontal().spacing(5);
            var text = new TextWidget(
                    160, 20 + 2,
                    Text.of(group.isEnabled()
                            ? group.getGroupName()
                            : Formatting.RED + Text.of(Formatting.ITALIC + group.getGroupName()).getString()
                    ),
                    client.textRenderer
            );
            var toggleButton = CyclingButtonWidget.onOffBuilder()
                    .omitKeyText()
                    .initially(group.isEnabled())
                    .build(0, 0, 30, 20, Text.empty(),
                           (button, value) -> group.toggle()
                    );
            toggleButton.setTooltip(Tooltip.of(group.getButtonTooltip()));
            text.alignLeft();
            layout.add(EmptyWidget.ofWidth(15));
            layout.add(text);
            layout.add(toggleButton);
            layout.refreshPositions();
            layout.setX(width / 2 - layout.getWidth() / 2);
            return new GroupEntry(group, layout);
        }
    }

    static class GroupEntry extends ElementListWidget.Entry<GroupEntry>
    {
        private final Group group;
        private final LayoutWidget layout;
        private final List<ClickableWidget> children = Lists.newArrayList();

        GroupEntry(Group group, LayoutWidget layout)
        {
            this.group = group;
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
            context.drawTexture(group.getIdentifier(), x, y + 2, 0, 0, 16, 16, 16, 16);
            layout.forEachChild(child -> {
                child.setY(y);
                child.render(context, mouseX, mouseY, delta);
            });
        }
    }
}