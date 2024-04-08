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
import java.util.Objects;
import java.util.Properties;

public class Controls
{
    // The record will be used to serialize and deserialize the controls
    public record SerializableControls(
            @SerializedName("type") @NotNull String type,
            @SerializedName("group_name") @NotNull String groupName,
            @SerializedName("properties_files") @NotNull ArrayList<String> propertiesFilesPaths,
            @SerializedName("enabled") boolean isEnabled,
            @SerializedName("button_tooltip") @Nullable String buttonTooltip,
            @SerializedName("screen_texture") @Nullable String screenTexture
    )
    {
    }

    private final String type;
    private final String groupName;
    private final ArrayList<String> propertiesFilesStrings;
    private boolean isEnabled;
    private final Text buttonTooltip;
    private final String texturePath;

    // The following fields are not in the file, and are used only in the code
    private final ArrayList<Path> propertiesFilesPaths = new ArrayList<>();
    private final Identifier identifier;

    private final ArrayList<CTMBlock> containedBLocksList = new ArrayList<>();

    public Controls(
            @NotNull String type, @NotNull String groupName, @Nullable String buttonTooltip,
            @NotNull ArrayList<String> propertiesFilesPaths, @Nullable String texturePath,
            boolean isEnabled, Path packPath
    )
    {
        this.type = type;
        this.groupName = groupName;
        this.buttonTooltip = buttonTooltip == null ? Text.empty() : Text.of(buttonTooltip);
        this.propertiesFilesStrings = propertiesFilesPaths;
        this.texturePath = texturePath;
        this.isEnabled = isEnabled;

        for (String s : propertiesFilesPaths)
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

        if (texturePath == null)
        {
            if (propertiesFilesPaths.isEmpty()) // Case where no files where specified (this is also an error)
            {
                this.identifier = new Identifier("textures/misc/unknown_pack.png");
            }
            else
            {
                String path = getBlocksForImage(Path.of(packPath + "/assets/" + propertiesFilesPaths.get(0).replace(
                        ":", "/")));
                if (path == null)
                {
                    this.identifier = new Identifier("textures/misc/unknown_pack.png");
                }
                else
                {
                    String pathFromFiles = propertiesFilesPaths.get(0);
                    String namespace = pathFromFiles.split(":")[0];
                    String newPath = "textures/block/" + (path.contains(" ") ? path.split(" ")[0] : path) + ".png";
                    this.identifier = new Identifier(namespace, newPath);
                }
            }
        }
        else if (!texturePath.contains(":")) // Case where the namespace is not specified
        {
            this.identifier = new Identifier("textures/misc/unknown_pack.png");
        }
        else
        {
            this.identifier = new Identifier(texturePath.split(":")[0], texturePath.split(":")[1]);
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

    public void setEnabled(boolean isEnabled)
    {
        this.isEnabled = isEnabled;
    }

    public void toggle()
    {
        this.isEnabled = !this.isEnabled;
    }

    public SerializableControls getAsRecord()
    {
        return new SerializableControls(type, groupName, propertiesFilesStrings,
                isEnabled, buttonTooltip.getString(), texturePath
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
            properties.load(new FileInputStream(String.valueOf(path)));
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
        for (File file : Objects.requireNonNull(dir.listFiles()))
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

    public void addContainedBLock(CTMBlock ctmBlock)
    {
        containedBLocksList.add(ctmBlock);
    }

    public ArrayList<CTMBlock> getContainedBLocksList()
    {
        return containedBLocksList;
    }
}
