package fr.aeldit.ctms.gui.widgets;

import fr.aeldit.ctms.gui.entries.ModEntry;
import fr.aeldit.ctms.gui.entries.ModEntryBuilder;
import fr.aeldit.ctms.textures.entryTypes.CTMPack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ElementListWidget;
import org.jetbrains.annotations.Contract;

/**
 * Modified by me to fit my purpose
 *
 * @author dicedpixels (<a href="https://github.com/dicedpixels">...</a>)
 */
public class ModListWidget extends ElementListWidget<ModEntry>
{
    private final ModEntryBuilder builder = new ModEntryBuilder(client, width);
    private final Screen parent;


    //? if >=1.20.4 {
    public ModListWidget(MinecraftClient client, int width, int height, int y, int itemHeight, Screen parent)
    {
        super(client, width, height, y, itemHeight);
        this.parent = parent;
    }
    //?} else {
        /*public ModListWidget(
                MinecraftClient client, int width, int height, int top, int bottom, int itemHeight,
                Screen parent
        )
        {
            super(client, width, height, top, bottom, itemHeight);
            this.parent = parent;
        }
        *///?}

    //? if <1.20.6 {
        /*@Contract(pure = true)
        @Override
        protected int getScrollbarPositionX()
        {
            return this.width / 2 + 160;
        }
        *///?}

    @Contract(pure = true)
    @Override
    public int getRowWidth()
    {
        return 300;
    }

    public void add(CTMPack ctmPack, String namespace)
    {
        addEntry(builder.build(ctmPack, namespace, parent));
    }
}
