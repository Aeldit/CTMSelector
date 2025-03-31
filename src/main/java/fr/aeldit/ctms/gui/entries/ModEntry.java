package fr.aeldit.ctms.gui.entries;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ModEntry extends ElementListWidget.Entry<ModEntry>
{
    private final LayoutWidget layout;
    private final List<ClickableWidget> children = new ArrayList<>();

    ModEntry(LayoutWidget layout)
    {
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
        layout.forEachChild(child -> {
            child.setY(y);
            child.render(context, mouseX, mouseY, delta);
        });
    }
}