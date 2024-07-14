package fr.aeldit.ctms.gui.widgets;

import fr.aeldit.ctms.gui.entries.BlockEntry;
import fr.aeldit.ctms.gui.entries.BlockEntryBuilder;
import fr.aeldit.ctms.textures.entryTypes.CTMBlock;
import fr.aeldit.ctms.textures.entryTypes.CTMPack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ElementListWidget;

/**
 * Modified by me to fit my purpose
 *
 * @author dicedpixels (<a href="https://github.com/dicedpixels">...</a>)
 */
public class BlocksListWidget extends ElementListWidget<BlockEntry>
{
    private final BlockEntryBuilder builder = new BlockEntryBuilder(client, width);
    private final CTMPack ctmPack;

    //? if <1.20.4 {
    /*public BlocksListWidget(
            MinecraftClient client, int width, int height, int top, int bottom, int itemHeight, CTMPack ctmPack
    )
    {
        super(client, width, height, top, bottom, itemHeight);
        this.ctmPack = ctmPack;
    }
    *///?} else {
    public BlocksListWidget(MinecraftClient client, int width, int height, int y, int itemHeight, CTMPack ctmPack)
    {
        super(client, width, height, y, itemHeight);
        this.ctmPack = ctmPack;
    }
    //?}

    public void add(CTMBlock block)
    {
        addEntry(builder.build(block, ctmPack));
    }
}