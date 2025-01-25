package fr.aeldit.ctms.gui.entries;

import fr.aeldit.ctms.gui.ResourcePackScreen;
import fr.aeldit.ctms.textures.CTMPacks;
import fr.aeldit.ctms.textures.entryTypes.CTMPack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record PackEntryBuilder(MinecraftClient client, int width)
{
    @Contract("_, _ -> new")
    public @NotNull PackEntry build(@NotNull CTMPack ctmPack, @NotNull Screen parent)
    {
        var layout = DirectionalLayoutWidget.horizontal().spacing(10);
        var text = new TextWidget(
                180, 24,
                CTMPacks.isPackEnabled(ctmPack.getName())
                // If the pack is not enabled, it is in italic and gray
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
        return new PackEntry(ctmPack, layout);
    }
}
