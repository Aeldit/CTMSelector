package fr.aeldit.ctms.gui.entries;

import fr.aeldit.ctms.textures.entryTypes.CTMPack;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PackEntry extends ElementListWidget.Entry<PackEntry>
{
    // TODO -> Get each pack's icon identifier
    private final CTMPack pack;
    private final LayoutWidget layout;
    private final List<ClickableWidget> children = new ArrayList<>();

    PackEntry(CTMPack pack, LayoutWidget layout)
    {
        this.pack   = pack;
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
        //context.drawTexture(pack.getIdentifier(), x, y, 0, 0, 24, 24, 24, 24);
        layout.forEachChild(child -> {
            child.setY(y);
            child.render(context, mouseX, mouseY, delta);
        });
    }
}