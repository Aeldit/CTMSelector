package fr.aeldit.ctms.gui.entryTypes;

import fr.aeldit.ctms.textures.Controls;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a a block found in a {@link java.util.Properties Properties} file
 * that has the CTM method
 *
 * @apiNote The {@link #controlsGroup} contains the
 * {@link Controls} object that contains this block
 * <p>
 * {@link #blockName} is in the form {@code "block_name"}
 * <p>
 * {@link #prettyName} is in the form {@code "Block Name"}
 * <p>
 * A block being enabled or disabled depends only on the state of
 * the field {@link #enabled} if the block is not contained
 * by any {@link Controls}. If the block is contained by at least 1
 * {@link Controls},
 * it depends on whether this group is activated or not
 */
public class CTMBlock
{
    private final String blockName;
    private final Text prettyName;
    private final Identifier identifier;
    private Controls controlsGroup;
    private boolean enabled;

    public CTMBlock(@NotNull String blockName, Identifier identifier, boolean enabled)
    {
        this.blockName = blockName;
        this.identifier = identifier;
        this.enabled = enabled;

        // Changes the lowercase and underscore separated string by replacing each '_' by a space
        // and by capitalizing the first letter of each word
        String[] tmp = blockName.split("_");
        StringBuilder stringBuilder = new StringBuilder();
        int index = 0;

        for (String str : tmp)
        {
            stringBuilder.append(str.substring(0, 1).toUpperCase()).append(str.substring(1));

            if (index < tmp.length - 1)
            {
                stringBuilder.append(" ");
            }
            index++;
        }
        this.prettyName = Text.of(stringBuilder.toString());
    }

    public String getBlockName()
    {
        return blockName;
    }

    public Text getPrettyName()
    {
        return prettyName;
    }

    public Identifier getIdentifier()
    {
        return identifier;
    }

    public Controls getControlsGroup()
    {
        return controlsGroup;
    }

    public void setControlsGroup(Controls controlsGroup)
    {
        this.controlsGroup = controlsGroup;
    }

    public boolean isEnabled()
    {
        if (controlsGroup != null)
        {
            if (!controlsGroup.isEnabled())
            {
                return false;
            }
        }
        return enabled;
    }

    public boolean isDisabledFromGroup()
    {
        if (controlsGroup != null)
        {
            return !controlsGroup.isEnabled();
        }
        return false;
    }

    public void setEnabled(boolean value)
    {
        this.enabled = value;
    }

    public void toggle()
    {
        this.enabled = !this.enabled;
    }
}
