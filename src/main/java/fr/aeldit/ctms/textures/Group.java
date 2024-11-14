package fr.aeldit.ctms.textures;

import com.google.gson.annotations.SerializedName;
import fr.aeldit.ctms.textures.entryTypes.CTMBlock;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
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
     *                             These paths start from the namespace and go to the properties file (in the form
     *                             {@code "namespace:path/to/file.properties"}).
     *                             <p>
     *                             If the path points to a directory, all properties files found inside it
     *                             recursively will be included (ex: {@code "minecraft:optifine/ctm/connect/logs"}
     * @param iconPath             The path to the icon that will be displayed on the screen for this group (in the
     *                             form {@code "namespace:path/to/file.png"}).
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
    ) {}

    //===============================//
    //         Record fields         //
    //===============================//
    private final String type, groupName, iconPath;
    public boolean isEnabled;
    private final Text buttonTooltip;
    private final ArrayList<String> propertiesFilesPaths; // When the pack is a folder

    //===============================//
    //      Non-record fields        //
    //===============================//
    private final Identifier identifier;
    private final ArrayList<CTMBlock> containedBlocks = new ArrayList<>();

    public Group(
            @NotNull String type, @NotNull String groupName, @Nullable String buttonTooltip,
            @NotNull ArrayList<File> propertiesFilesPaths, @Nullable String iconPath,
            boolean isEnabled, @NotNull Path packPath
    )
    {
        this.type                 = type;
        this.groupName            = groupName;
        this.iconPath             = iconPath == null ? null : getStringInIdentifierForm(iconPath);
        this.isEnabled            = isEnabled;
        this.buttonTooltip        = buttonTooltip == null ? Text.empty() : Text.of(buttonTooltip);
        this.propertiesFilesPaths = getPropertiesFilesAsStrings(propertiesFilesPaths);
        this.identifier           = initializeIdentifier();
    }

    public Group(
            @NotNull String type, @NotNull String groupName, @Nullable String buttonTooltip,
            @NotNull ArrayList<FileHeader> fileHeaders, @Nullable String iconPath,
            boolean isEnabled, @NotNull ZipFile zipFile
    )
    {
        this.type                 = type;
        this.groupName            = groupName;
        this.iconPath             = iconPath == null ? null : getStringInIdentifierFormZip(iconPath);
        this.isEnabled            = isEnabled;
        this.buttonTooltip        = buttonTooltip == null ? Text.empty() : Text.of(buttonTooltip);
        this.propertiesFilesPaths = getPropertiesFileHeadersAsStrings(fileHeaders);
        this.identifier           = initializeIdentifier();
    }

    // Initialize from a SerializableGroup record (which was read from a ctm_selector.json file)
    public Group(@NotNull SerializableGroup serializableGroup, @NotNull String packPath)
    {
        this.type                 = serializableGroup.type;
        this.groupName            = serializableGroup.groupName;
        this.iconPath             = serializableGroup.iconPath;
        this.isEnabled            = serializableGroup.isEnabled;
        this.buttonTooltip        = Text.of(serializableGroup.buttonTooltip);
        this.propertiesFilesPaths = addPackPathToPath(serializableGroup.propertiesFilesPaths, packPath);
        this.identifier           = initializeIdentifier();
    }

    public Group(@NotNull SerializableGroup serializableGroup, @NotNull ZipFile zipFile)
    {
        this.type                 = serializableGroup.type;
        this.groupName            = serializableGroup.groupName;
        this.iconPath             = serializableGroup.iconPath;
        this.isEnabled            = serializableGroup.isEnabled;
        this.buttonTooltip        = Text.of(serializableGroup.buttonTooltip);
        this.propertiesFilesPaths = serializableGroup.propertiesFilesPaths;
        this.identifier           = initializeIdentifier();
    }

    private @NotNull ArrayList<String> getPropertiesFilesAsStrings(@NotNull ArrayList<File> files)
    {
        ArrayList<String> stringFiles = new ArrayList<>(files.size());
        files.forEach(file -> stringFiles.add(file.toString()));
        files.clear();
        return stringFiles;
    }

    private @NotNull ArrayList<String> getPropertiesFileHeadersAsStrings(@NotNull ArrayList<FileHeader> fileHeaders)
    {
        ArrayList<String> stringFiles = new ArrayList<>(fileHeaders.size());
        fileHeaders.forEach(file -> stringFiles.add(file.toString()));
        fileHeaders.clear();
        return stringFiles;
    }

    private static @NotNull String getStringInIdentifierForm(@NotNull String path)
    {
        String[] splitOnSlashes = path.split("/");
        int assetsIndex = List.of(splitOnSlashes).lastIndexOf("assets");
        if (assetsIndex == -1 || splitOnSlashes.length < assetsIndex + 3)
        {
            return "";
        }
        return "%s:%s".formatted(
                splitOnSlashes[assetsIndex + 1],
                StringUtils.join(splitOnSlashes, "/", assetsIndex + 2, splitOnSlashes.length)
        );
    }

    private static @NotNull String getStringInIdentifierFormZip(@NotNull String path)
    {
        String[] splitOnSlashes = path.split("/");
        if (splitOnSlashes.length == 0 || !splitOnSlashes[0].equals("assets") || splitOnSlashes.length < 3)
        {
            return "";
        }
        return "%s:%s".formatted(splitOnSlashes[1], StringUtils.join(splitOnSlashes, "/", 2, splitOnSlashes.length));
    }

    @Contract(" -> new")
    private @NotNull Identifier initializeIdentifier()
    {
        if (iconPath == null || !iconPath.contains(":"))
        {
            return new Identifier("textures/misc/unknown_pack.png");
        }

        String[] split = iconPath.split(":");
        if (split.length != 2)
        {
            return new Identifier("textures/misc/unknown_pack.png");
        }
        return new Identifier(split[0], split[1]);
    }

    //===============================//
    //             Record            //
    //===============================//
    public String getGroupName()
    {
        return groupName;
    }

    public void toggle()
    {
        this.isEnabled = !this.isEnabled;
    }

    public Text getButtonTooltip()
    {
        return buttonTooltip;
    }

    private static @NotNull ArrayList<String> addPackPathToPath(
            @NotNull ArrayList<String> paths, @NotNull String packPath
    )
    {
        ArrayList<String> newPaths = new ArrayList<>(paths.size());
        paths.forEach(path -> newPaths.add("%s/%s".formatted(packPath, path)));
        paths.clear();
        return newPaths;
    }

    private static @NotNull ArrayList<String> removePackPathFromPath(
            @NotNull ArrayList<String> paths, @NotNull String packPath
    )
    {
        ArrayList<String> newPaths = new ArrayList<>(paths.size());
        paths.forEach(path -> newPaths.add(path.replace(packPath, "").replaceFirst("/", "")));
        paths.clear();
        return newPaths;
    }

    public SerializableGroup getAsRecord(String packPath)
    {
        return new SerializableGroup(
                type, groupName, removePackPathFromPath(propertiesFilesPaths, packPath), iconPath, isEnabled,
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
    public @NotNull ArrayList<String> getPropertiesFilesPaths()
    {
        return propertiesFilesPaths;
    }
}
