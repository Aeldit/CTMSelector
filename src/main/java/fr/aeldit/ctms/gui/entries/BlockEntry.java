package fr.aeldit.ctms.gui.entries;

import fr.aeldit.ctms.textures.entryTypes.CTMBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.render.RenderLayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Modified by me to fit my purpose
 *
 * @author dicedpixels (<a href="https://github.com/dicedpixels">...</a>)
 */
public class BlockEntry extends ElementListWidget.Entry<BlockEntry>
{
    private final CTMBlock block;
    private final LayoutWidget layout;
    private final List<ClickableWidget> children = new ArrayList<>();

    BlockEntry(CTMBlock block, LayoutWidget layout)
    {
        this.block  = block;
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
        //? if <1.21.2-1.21.3 {
         /*context.drawTexture(block.identifier, x, y + 2, 0, 0, 16, 16, 16, 16);
        *///?} else {
        context.drawTexture(RenderLayer::getGuiTextured, block.identifier, x, y + 2, 0, 0, 16, 16, 16, 16);
        //?}
        layout.forEachChild(child -> {
            child.setY(y);
            child.render(context, mouseX, mouseY, delta);
        });
    }
}