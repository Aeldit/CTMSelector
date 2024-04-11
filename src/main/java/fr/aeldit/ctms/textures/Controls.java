package fr.aeldit.ctms.textures;

import com.google.gson.annotations.SerializedName;
import fr.aeldit.ctms.gui.entryTypes.CTMBlock;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Properties;

public class Controls
{
    // The record will be used to serialize and deserialize the controls
    public record SerializableControls(
            @SerializedName("type") @NotNull String type,
            @SerializedName("group_name") @NotNull String groupName,
            @SerializedName("properties_files") @NotNull ArrayList<String> propertiesFilesPaths,
            @SerializedName("icon") @NotNull String icon,
            @SerializedName("enabled") boolean isEnabled,
            @SerializedName("button_tooltip") @Nullable String buttonTooltip
    )
    {
    }

    private final String type;
    private final String groupName;
    private final ArrayList<String> propertiesFilesStrings;
    private boolean isEnabled;
    private final Text buttonTooltip;
    private final String icon;

    // The following fields are not in the file, and are used only in the code
    private final ArrayList<Path> propertiesFilesPaths = new ArrayList<>();
    private final Identifier identifier;
    private final ArrayList<CTMBlock> containedBlocks = new ArrayList<>();

    public Controls(
            @NotNull String type, @NotNull String groupName, @Nullable String buttonTooltip,
            @NotNull ArrayList<String> propertiesFilesStrings, @NotNull String icon,
            boolean isEnabled, Path packPath
    )
    {
        this.type = type;
        this.groupName = groupName;
        this.buttonTooltip = buttonTooltip == null ? Text.empty() : Text.of(buttonTooltip);
        this.propertiesFilesStrings = propertiesFilesStrings;
        this.icon = icon;
        this.isEnabled = isEnabled;

        // Obtains the path to each block
        for (String s : propertiesFilesStrings)
        {
            Path assetsInPackPath = Path.of(packPath + "/assets/" + s.replace(":", "/"));

            if (!s.endsWith(".properties"))
            {
                if (Files.isDirectory(assetsInPackPath))
                {
                    addPropertiesFilesRec(assetsInPackPath.toFile());
                }
            }
            else
            {
                this.propertiesFilesPaths.add(assetsInPackPath);
            }
        }

        // Case where the namespace is not specified
        if (!icon.contains(":"))
        {
            this.identifier = new Identifier("textures/misc/unknown_pack.png");
        }
        else
        {
            this.identifier = new Identifier(icon.split(":")[0], icon.split(":")[1]);
        }
    }

    public String getGroupName()
    {
        return groupName;
    }

    public Text getGroupNameAsText()
    {
        return Text.of(groupName);
    }

    public Text getButtonTooltip()
    {
        return buttonTooltip;
    }

    public boolean isEnabled()
    {
        return isEnabled;
    }

    public void setEnabled(boolean value)
    {
        this.isEnabled = value;
    }

    public void toggle()
    {
        this.isEnabled = !this.isEnabled;
    }

    public SerializableControls getAsRecord()
    {
        return new SerializableControls(type, groupName, propertiesFilesStrings, icon, isEnabled,
                buttonTooltip.getString()
        );
    }

    /**
     * @return The path to each Properties file contained by the Controls
     */
    public ArrayList<Path> getPropertiesFilesPaths()
    {
        return propertiesFilesPaths;
    }

    public Identifier getIdentifier()
    {
        return identifier;
    }

    /**
     * @param path The path to the properties file
     * @return A string containing the field {@code "matchBlocks"} or the field {@code "matchTiles"}
     * (ex: can be {@code "copper_block"} or {@code "copper_block exposed_copper weathered_copper oxidized_copper"},
     * where each block is separated by a space)
     */
    private @Nullable String getBlocksForImage(Path path)
    {
        if (!Files.exists(path))
        {
            return null;
        }

        Properties properties = new Properties();
        try
        {
            properties.load(new FileInputStream(path.toFile()));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        if (properties.containsKey("matchBlocks"))
        {
            return properties.getProperty("matchBlocks");
        }
        else if (properties.containsKey("matchTiles"))
        {
            return properties.getProperty("matchTiles");
        }
        else if (properties.containsKey("ctmDisabled"))
        {
            return properties.getProperty("ctmDisabled");
        }
        else if (properties.containsKey("ctmTilesDisabled"))
        {
            return properties.getProperty("ctmTilesDisabled");
        }
        return null;
    }

    /**
     * Searches the directory recursively to find every properties files inside it
     *
     * @param dir The directory
     */
    private void addPropertiesFilesRec(@NotNull File dir)
    {
        File[] files = dir.listFiles();
        if (files == null)
        {
            return;
        }

        for (File file : files)
        {
            if (file.isDirectory())
            {
                addPropertiesFilesRec(file);
            }
            if (file.isFile() && file.toString().endsWith(".properties"))
            {
                this.propertiesFilesPaths.add(Path.of(file.getAbsolutePath()));
            }
        }
    }

    public void addContainedBlock(CTMBlock ctmBlock)
    {
        containedBlocks.add(ctmBlock);
    }

    public ArrayList<CTMBlock> getContainedBlocksList()
    {
        return containedBlocks;
    }
}
