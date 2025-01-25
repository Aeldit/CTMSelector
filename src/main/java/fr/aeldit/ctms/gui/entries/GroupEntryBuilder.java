package fr.aeldit.ctms.gui.entries;

import fr.aeldit.ctms.textures.Group;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.EmptyWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public record GroupEntryBuilder(MinecraftClient client, int width)
{
    public @NotNull GroupEntry build(@NotNull Group group)
    {
        var layout = DirectionalLayoutWidget.horizontal().spacing(5);
        var text = new TextWidget(160, 20 + 2, Text.of(group.getGroupName()), client.textRenderer);
        var toggleButton = CyclingButtonWidget.onOffBuilder()
                                              .omitKeyText()
                                              .initially(group.isEnabled())
                                              .build(
                                                      0, 0, 30, 20, Text.empty(),
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
