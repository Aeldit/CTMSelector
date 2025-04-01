package fr.aeldit.ctms.textures.entryTypes;

import fr.aeldit.ctms.textures.Group;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import static fr.aeldit.ctms.Utils.getPrettyString;

/**
 * Represents a block found in a {@link java.util.Properties Properties} file
 * that has the CTM method
 * <p>
 * {@link Group} object that contains this block
 * <p>
 * {@link #blockName} is in the form {@code "block_name"}
 * <p>
 * {@link #prettyName} is in the form {@code "Block Name"}
 * <p>
 * A block being enabled or disabled depends only on the state of
 * the field {@link #enabled} if the block is not contained
 * by any {@link Group}. If the block is contained by at least 1
 * {@link Group},
 * it depends on whether this group is activated or not
 */
public class CTMBlock
{
    public final String blockName;
    public final Text prettyName;
    public final Identifier identifier;
    private boolean enabled;
    public final boolean isTile;
    public final String propertiesPath;

    public CTMBlock(
            @NotNull String blockName, Identifier identifier, boolean enabled, boolean isTile, String propertiesPath
    )
    {
        this.blockName      = blockName;
        this.identifier     = identifier;
        this.enabled        = enabled;
        this.isTile         = isTile;
        this.propertiesPath = propertiesPath;

        if (blockName.contains(":"))
        {
            String[] split = blockName.split(":");
            // If the namespace is specified (namespace:block)
            if (split.length == 2 && !split[1].contains("="))
            {
                this.prettyName = Text.of(getPrettyString(split[1].split("_")));
            }
            else // If there are block states
            {
                StringBuilder sb = new StringBuilder();
                if (!split[1].contains("="))
                {
                    sb.append(split[1]);
                    sb.append("_");
                }
                else if (!split[0].contains("="))
                {
                    sb.append(split[0]);
                    sb.append("_");
                }
                for (String text : split)
                {
                    if (text.contains("="))
                    {
                        if (text.split("=")[1].equals("true"))
                        {
                            sb.append(text.split("=")[0]);
                            sb.append("_");
                        }
                    }
                }
                this.prettyName = Text.of(getPrettyString(sb.toString().split("_")));
            }
        }
        else
        {
            this.prettyName = Text.of(getPrettyString(blockName.split("_")));
        }
    }

    public boolean isEnabled()
    {
        return enabled;
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
