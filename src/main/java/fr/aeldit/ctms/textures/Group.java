package fr.aeldit.ctms.textures;

import com.google.gson.annotations.SerializedName;
import fr.aeldit.ctms.textures.entryTypes.CTMBlock;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


/**
 * Contains a list of blocks that can be toggled together using only one click
 */
public class Group
{
    /**
     * Holds the data of 1 group that needs to be written to the {@code "ctm_selector.json"} file (which contains a
     * list of groups)
     *
     * @param type                 The type of texture property that will be toggled. For now, this can only be set
     *                             to {@code "ctm"}
     * @param groupName            The name of the group that will be display on the screen
     * @param propertiesFilesPaths The path to each directory or properties files that will be included in the group.
     *                             These paths start from the namespace and go to the properties file. If the path
     *                             points to a directory, all properties files found inside it recursively will be
     *                             included (ex: {@code "minecraft:optifine/ctm/connect/logs"}
     * @param iconPath             The path to the icon that will be displayed on the screen for this group. Must be
     *                             formed in the same way as the {@link Group#identifierLikePropertiesPaths}, but
     *                             must be
     *                             a path to a single {@code .png} file
     * @param isEnabled            If the block is enabled, this part is changed by the user of the mod (optional)
     * @param buttonTooltip        The tooltip to display on the button of the group (optional)
     */
    public record SerializableGroup(
            @SerializedName("type") @NotNull String type,
            @SerializedName("group_name") @NotNull String groupName,
            @SerializedName("properties_files") @NotNull ArrayList<String> propertiesFilesPaths,
            @SerializedName("icon_path") @NotNull String iconPath,
            @SerializedName("enabled") boolean isEnabled,
            @SerializedName("button_tooltip") @Nullable String buttonTooltip
    )
    {
    }

    //=================================
    // Record fields
    //=================================
    private final String type, groupName, iconPath;
    private final ArrayList<String> identifierLikePropertiesPaths;
    private boolean isEnabled;
    private final Text buttonTooltip;

    //=================================
    // Non-record fields
    //=================================
    private final ArrayList<Path> propertiesFilesPaths; // When the pack is a folder
    private final ArrayList<FileHeader> propertiesFilesFileHeaders; // When the pack is a zip file
    private final Identifier identifier;
    private final ArrayList<CTMBlock> containedBlocks = new ArrayList<>();

    // Initialize from a SerializableGroup record (which was read from a ctm_selector.json file
    public Group(
            @NotNull SerializableGroup serializableGroup, @Nullable Path packPath, @Nullable String zipPackPath
    )
    {
        this.type          = serializableGroup.type;
        this.groupName     = serializableGroup.groupName;
        this.buttonTooltip = Text.of(serializableGroup.buttonTooltip);
        this.isEnabled     = serializableGroup.isEnabled;

        // Obtains the path to each block
        if (packPath != null)
        {
            this.propertiesFilesPaths       = new ArrayList<>();
            this.propertiesFilesFileHeaders = null;

            for (String propFile : serializableGroup.propertiesFilesPaths)
            {
                Path assetsInPackPath = Path.of("%s/assets/%s".formatted(packPath, propFile.replace(":", "/")));

                if (propFile.endsWith(".properties"))
                {
                    this.propertiesFilesPaths.add(assetsInPackPath);
                }
                else
                {
                    if (Files.isDirectory(assetsInPackPath))
                    {
                        addPropertiesFilesRec(assetsInPackPath.toFile());
                    }
                }
            }
        }
        else
        {
            this.propertiesFilesPaths       = null;
            this.propertiesFilesFileHeaders = new ArrayList<>();

            if (zipPackPath != null)
            {
                try (ZipFile zipFile = new ZipFile(zipPackPath))
                {
                    for (String s : serializableGroup.propertiesFilesPaths)
                    {
                        String pathInZip = "assets/%s".formatted(s.replace(":", "/"));

                        if (pathInZip.endsWith(".properties"))
                        {
                            FileHeader fh = getFileHeaderByName(zipFile.getFileHeaders(), pathInZip);
                            if (fh != null)
                            {
                                this.propertiesFilesFileHeaders.add(fh);
                            }
                        }
                        else
                        {
                            getPropertiesFilesInZipFolder(zipFile.getFileHeaders(), pathInZip);
                        }
                    }
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }

        this.identifierLikePropertiesPaths = serializableGroup.propertiesFilesPaths;
        this.iconPath                      = serializableGroup.iconPath;

        // If the namespace is not specified, we use the 'unknown pack' icon
        if (iconPath.contains(":"))
        {
            String[] split = iconPath.split(":");
            this.identifier = new Identifier(split[0], split[1]);
        }
        else
        {
            this.identifier = new Identifier("textures/misc/unknown_pack.png");
        }
    }

    //=================================
    // Record
    //=================================
    public String getGroupName()
    {
        return groupName;
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

    public Text getButtonTooltip()
    {
        return buttonTooltip;
    }

    public SerializableGroup getAsRecord()
    {
        return new SerializableGroup(
                type, groupName, identifierLikePropertiesPaths, iconPath, isEnabled,
                buttonTooltip.getString()
        );
    }

    //=================================
    // Non-record
    //=================================
    public Identifier getIdentifier()
    {
        return identifier;
    }

    public ArrayList<CTMBlock> getContainedBlocksList()
    {
        return containedBlocks;
    }

    public void addContainedBlock(CTMBlock ctmBlock)
    {
        containedBlocks.add(ctmBlock);
    }

    /**
     * @return The absolute path to each Properties file contained by the Group
     */
    public @Nullable ArrayList<Path> getPropertiesFilesPaths()
    {
        return propertiesFilesPaths;
    }

    /**
     * @return The fileHeaders of each properties file found in the zip pack
     */
    public @Nullable ArrayList<FileHeader> getPropertiesFilesFileHeaders()
    {
        return propertiesFilesFileHeaders;
    }

    //=================================
    // Other
    //=================================

    /**
     * Searches the directory recursively to find every properties files inside it
     *
     * @param dir The directory
     */
    private void addPropertiesFilesRec(@NotNull File dir)
    {
        File[] files = dir.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                if (file.isDirectory())
                {
                    addPropertiesFilesRec(file);
                }
                if (file.isFile() && file.toString().endsWith(".properties"))
                {
                    propertiesFilesPaths.add(Path.of(file.getAbsolutePath()));
                }
            }
        }
    }

    private void getPropertiesFilesInZipFolder(@NotNull List<FileHeader> fileHeaders, @NotNull String folder)
    {
        if (propertiesFilesFileHeaders != null)
        {
            fileHeaders.stream()
                       .filter(fileHeader -> fileHeader.toString().startsWith(folder))
                       .filter(fileHeader -> fileHeader.toString().endsWith(".properties"))
                       .forEach(propertiesFilesFileHeaders::add);
        }
    }

    private @Nullable FileHeader getFileHeaderByName(@NotNull List<FileHeader> fileHeaders, @NotNull String name)
    {
        return fileHeaders.stream().filter(fileHeader -> fileHeader.toString().equals(name)).findFirst().orElse(null);
    }
}