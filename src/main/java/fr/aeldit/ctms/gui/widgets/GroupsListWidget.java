package fr.aeldit.ctms.gui.widgets;

import fr.aeldit.ctms.gui.entries.GroupEntry;
import fr.aeldit.ctms.gui.entries.GroupEntryBuilder;
import fr.aeldit.ctms.textures.Group;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ElementListWidget;

/**
 * Modified by me to fit my purpose
 *
 * @author dicedpixels (<a href="https://github.com/dicedpixels">...</a>)
 */
public class GroupsListWidget extends ElementListWidget<GroupEntry>
{
    private final GroupEntryBuilder builder = new GroupEntryBuilder(client, width);

    //? if <1.20.4 {
    /*public GroupsListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight)
    {
        super(client, width, height, top, bottom, itemHeight);
        this.ctmSelector = ctmSelector;
    }
    *///?} else {
    public GroupsListWidget(MinecraftClient client, int width, int height, int y, int itemHeight)
    {
        super(client, width, height, y, itemHeight);
    }
    //?}

    public void add(Group group)
    {
        addEntry(builder.build(group));
    }
}
