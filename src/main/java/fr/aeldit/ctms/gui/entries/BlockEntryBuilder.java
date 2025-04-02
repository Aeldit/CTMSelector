package fr.aeldit.ctms.gui.entries;

import fr.aeldit.ctms.textures.entryTypes.CTMBlock;
import fr.aeldit.ctms.textures.entryTypes.CTMPack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Modified by me to fit my purpose
 *
 * @author dicedpixels (<a href="https://github.com/dicedpixels">...</a>)
 */
public record BlockEntryBuilder(MinecraftClient client, int width)
{
    @Contract("_, _ -> new")
    public @NotNull BlockEntry build(@NotNull CTMBlock block, @NotNull CTMPack ctmPack)
    {
        var layout = DirectionalLayoutWidget.horizontal().spacing(5);
        var text = new TextWidget(
                160,
                20 + 2,
                ctmPack.isBlockDisabledFromGroup(block) || !block.isEnabled()
                ? Text.of(Formatting.RED + Text.of(Formatting.ITALIC + block.prettyName.getString()).getString())
                : block.prettyName,
                client.textRenderer
        );

        text.alignLeft();
        layout.add(EmptyWidget.ofWidth(15));
        layout.add(text);

        if (ctmPack.isBlockDisabledFromGroup(block))
        {
            var toggleButton = ButtonWidget.builder(ScreenTexts.OFF, button -> {})
                                           .dimensions(0, 0, 30, 20)
                                           .build();
            toggleButton.setTooltip(Tooltip.of(Text.translatable("ctms.screen.block.parentControlIsDisabled")));
            layout.add(toggleButton);
        }
        else
        {
            var toggleButton = CyclingButtonWidget.onOffBuilder()
                                                  .omitKeyText()
                                                  .initially(block.isEnabled())
                                                  .build(
                                                          0, 0, 30, 20, Text.empty(),
                                                          (button, value) -> ctmPack.toggle(block)
                                                  );
            toggleButton.setTooltip(Tooltip.of(Text.empty()));
            layout.add(toggleButton);
        }
        layout.refreshPositions();
        layout.setX(width / 2 - layout.getWidth() / 2);
        return new BlockEntry(block, layout);
    }
}