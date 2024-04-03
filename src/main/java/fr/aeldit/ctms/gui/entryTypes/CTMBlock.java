package fr.aeldit.ctms.gui.entryTypes;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class CTMBlock
{
    private final String blockName;
    private final String prettyName;
    private final Identifier identifier;
    private boolean enabled;

    public CTMBlock(@NotNull String blockName, Identifier identifier, boolean enabled)
    {
        this.blockName = blockName;
        this.identifier = identifier;
        this.enabled = enabled;

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
        this.prettyName = stringBuilder.toString();
    }

    public Text getName()
    {
        return Text.of(prettyName);
    }

    public String getBlockName()
    {
        return blockName;
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
}
