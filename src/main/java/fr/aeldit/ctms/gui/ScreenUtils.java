package fr.aeldit.ctms.gui;

import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;

public class ScreenUtils
{
    public static final Text TEXT_RESET = Text.translatable("ctms.screen.config.reset");
    public static final Text TEXT_OPEN = Text.translatable("ctms.screen.open");
    public static final Tooltip TOOLTIP_RESET = Tooltip.of(Text.translatable("ctms.screen.config.reset.tooltip"));
    public static final Tooltip TOOLTIP_OPEN = Tooltip.of(Text.translatable("ctms.screen.open.tooltip"));
}
