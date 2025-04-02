package fr.aeldit.ctms.gui.entries;

import fr.aeldit.ctms.gui.NamespaceBlocksScreen;
import fr.aeldit.ctms.textures.entryTypes.CTMPack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record ModEntryBuilder(MinecraftClient client, int width)
{
    @Contract("_, _, _ -> new")
    public @NotNull ModEntry build(CTMPack ctmPack, @NotNull String namespace, @NotNull Screen parent)
    {
        var layout = DirectionalLayoutWidget.horizontal().spacing(10);
        var text = new TextWidget(
                180, 24, Text.of(namespace.substring(0, 1).toUpperCase() + namespace.substring(1)),
                client.textRenderer
        );
        var followButton = ButtonWidget.builder(
                                               Text.translatable("ctms.screen.open"),
                                               button -> client.setScreen(new NamespaceBlocksScreen(
                                                       parent, ctmPack,
                                                       namespace
                                               ))
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
