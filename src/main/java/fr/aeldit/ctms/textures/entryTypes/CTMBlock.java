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
    private final String blockName;
    private final Text prettyName;
    private final Identifier identifier;
    private boolean enabled;
    private final boolean isTile;
    private final String propertiesPath;

    public CTMBlock(@NotNull String blockName, Identifier identifier, boolean enabled, boolean isTile,
                    String propertiesPath
    )
    {
        this.blockName      = blockName;
        this.identifier     = identifier;
        this.enabled        = enabled;
        this.isTile         = isTile;
        this.propertiesPath = propertiesPath;

        if (blockName.contains(":"))
        {
            Text tmp_name = Text.of(getPrettyString(blockName.split("_")));

            String[] split = blockName.split(":");
            // If the namespace is specified
            if (split.length == 2 && !split[1].contains("="))
            {
                this.prettyName = Text.of(getPrettyString(split[1].split("_")));
            }
            else // we have block states
            {
                StringBuilder sb = new StringBuilder();
                for (String text : blockName.split(":"))
                {
                    if (!text.contains("="))
                    {
                        sb.append(text);
                        continue;
                    }

                    String[] state = text.split("=");
                    sb.append(state[0]).append("_").append(state[1]);
                }
                this.prettyName = Text.of(getPrettyString((blockName + "_" + sb).split("_")));
            }
        }
        else
        {
            this.prettyName = Text.of(getPrettyString(blockName.split("_")));
        }
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

    public boolean isTile()
    {
        return isTile;
    }

    public String getPropertiesPath()
    {
        return propertiesPath;
    }
}
