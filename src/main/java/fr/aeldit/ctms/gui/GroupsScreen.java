package fr.aeldit.ctms.gui;

import fr.aeldit.ctms.gui.widgets.GroupsListWidget;
import fr.aeldit.ctms.textures.CTMSelector;
import fr.aeldit.ctms.textures.Group;
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
public class GroupsScreen extends Screen
{
    private static final Text TEXT_RESET = Text.translatable("ctms.screen.config.reset");
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
        this.parent      = parent;
        this.ctmPack     = ctmPack;
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
        CTMSelector ctmSelector = ctmPack.getCtmSelector();

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
                ButtonWidget.builder(
                                    TEXT_RESET, button -> {
                                        ctmSelector.resetOptions();
                                        close();
                                    }
                            )
                            .tooltip(Tooltip.of(Text.translatable("ctms.screen.config.reset.tooltip")))
                            .dimensions(10, 6, 75, 20)
                            .build()
        );

        addDrawableChild(
                ButtonWidget.builder(ScreenTexts.DONE, button -> close())
                            .dimensions(width / 2 - 100, height - 28, 200, 20)
                            .build()
        );
    }
}
