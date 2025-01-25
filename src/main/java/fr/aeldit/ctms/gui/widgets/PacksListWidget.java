package fr.aeldit.ctms.gui.widgets;

import fr.aeldit.ctms.gui.entries.PackEntry;
import fr.aeldit.ctms.gui.entries.PackEntryBuilder;
import fr.aeldit.ctms.textures.entryTypes.CTMPack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ElementListWidget;

/**
 * Modified by me to fit my purpose
 *
 * @author dicedpixels (<a href="https://github.com/dicedpixels">...</a>)
 */
public class PacksListWidget extends ElementListWidget<PackEntry>
{
    private final PackEntryBuilder builder = new PackEntryBuilder(client, width);
    private final Screen parent;


    //? if >=1.20.4 {
    public PacksListWidget(MinecraftClient client, int width, int height, int y, int itemHeight, Screen parent)
    {
        super(client, width, height, y, itemHeight);
        this.parent = parent;
    }
    //?} else {
        /*public PacksListWidget(
                MinecraftClient client, int width, int height, int top, int bottom, int itemHeight,
                Screen parent

        )
        {
            super(client, width, height, top, bottom, itemHeight);
            this.parent = parent;
        }
        *///?}

    //? if <1.20.6 {
        /*@Override
        protected int getScrollbarPositionX()
        {
            return this.width / 2 + 160;
        }
        *///?}

    @Override
    public int getRowWidth()
    {
        return 280;
    }

    public void add(CTMPack ctmPack)
    {
        addEntry(builder.build(ctmPack, parent));
    }
}
