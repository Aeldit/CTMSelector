package fr.aeldit.ctms.textures;

import com.google.gson.annotations.SerializedName;
import fr.aeldit.ctms.textures.entryTypes.CTMBlock;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static fr.aeldit.ctms.VersionUtils.getIdentifier;


/**
 * Contains a list of blocks that can be toggled together using only one click
 */
public class Group
{
    /**
     * Holds the data of 1 group that needs to be written to the {@code "ctm_selector.json"} file (which contains a
     * list of groups)
     *
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
    public final String groupName;
    private final String iconPath;
    private final ArrayList<String> identifierLikePropertiesPaths;
    private boolean isEnabled;
    public final Text buttonTooltip;

    //=================================
    // Non-record fields
    //=================================
    private final List<Path> propertiesFilesPaths; // When the pack is a folder
    private final List<ZipEntry> propertiesFilesZipEntries; // When the pack is a zip file
    public final Identifier identifier;
    public final List<CTMBlock> containedBlocks = new ArrayList<>();

    // Initialize from a SerializableGroup record (which was read from a ctm_selector.json file
    public Group(@NotNull SerializableGroup serializableGroup, boolean isFolder, String packPath)
    {
        this.groupName     = serializableGroup.groupName;
        this.buttonTooltip = Text.of(serializableGroup.buttonTooltip);
        this.isEnabled     = serializableGroup.isEnabled;

        // Obtains the path to each block
        if (isFolder)
        {
            this.propertiesFilesPaths      = new ArrayList<>();
            this.propertiesFilesZipEntries = null;

            for (String path : serializableGroup.propertiesFilesPaths)
            {
                Path path1 = Path.of("%s/assets/%s".formatted(packPath, path.replace(":", "/")));
                if (Files.exists(path1) && path1.toFile().isDirectory())
                {
                    getPropertiesFilesPathsInDir(path1.toFile());
                }
                else
                {
                    if (path.endsWith(".properties"))
                    {
                        this.propertiesFilesPaths.add(path1);
                    }
                }
            }

        }
        else
        {
            this.propertiesFilesPaths      = null;
            this.propertiesFilesZipEntries = new ArrayList<>();

            try (ZipFile zipFile = new ZipFile(packPath))
            {
                for (String s : serializableGroup.propertiesFilesPaths)
                {
                    String pathInZip = "assets/%s".formatted(s.replace(":", "/"));

                    if (pathInZip.endsWith(".properties"))
                    {
                        ZipEntry entry = getZipEntryByName(zipFile.entries(), pathInZip);
                        if (entry != null)
                        {
                            this.propertiesFilesZipEntries.add(entry);
                        }
                    }
                    else
                    {
                        getPropertiesFilesInZipFolder(zipFile.entries(), pathInZip);
                    }
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        this.identifierLikePropertiesPaths = serializableGroup.propertiesFilesPaths;
        this.iconPath                      = serializableGroup.iconPath;

        // If the namespace is not specified, we use the 'unknown pack' icon
        if (iconPath.contains(":"))
        {
            String[] split = iconPath.split(":");
            this.identifier = getIdentifier(split[0], split[1]);
        }
        else
        {
            this.identifier = getIdentifier("textures/misc/unknown_pack.png");
        }
    }

    private void getPropertiesFilesPathsInDir(File dir)
    {
        Stack<File> fileStack = new Stack<>();
        fileStack.push(dir);

        while (!fileStack.empty())
        {
            File currentFile = fileStack.pop();
            if (!currentFile.isDirectory())
            {
                continue;
            }

            File[] files = currentFile.listFiles();
            if (files == null)
            {
                continue;
            }

            for (File file : files)
            {
                if (file.isDirectory())
                {
                    fileStack.push(file);
                }
                else if (file.isFile() && file.toString().endsWith(".properties"))
                {
                    this.propertiesFilesPaths.add(Path.of(file.toString()));
                }
            }
        }
    }

    //=================================
    // Record
    //=================================

    public boolean isEnabled()
    {
        return isEnabled;
    }

    public void enable()
    {
        this.isEnabled = true;
    }

    public void toggle()
    {
        this.isEnabled = !this.isEnabled;
    }

    public SerializableGroup getAsRecord()
    {
        return new SerializableGroup(
                groupName, identifierLikePropertiesPaths, iconPath, isEnabled, buttonTooltip.getString()
        );
    }

    //=================================
    // Non-record
    //=================================

    public void addContainedBlock(CTMBlock ctmBlock)
    {
        if (propertiesFilesPaths != null && propertiesFilesPaths.contains(Path.of(ctmBlock.propertiesPath)))
        {
            containedBlocks.add(ctmBlock);
        }
        else if (
                propertiesFilesZipEntries != null
                && propertiesFilesZipEntries.stream()
                                            .map(ZipEntry::getName)
                                            .toList()
                                            .contains(ctmBlock.propertiesPath)
        )
        {
            containedBlocks.add(ctmBlock);
        }
    }

    //=================================
    // Other
    //=================================

    private void getPropertiesFilesInZipFolder(@NotNull Enumeration<? extends ZipEntry> entries, @NotNull String folder)
    {
        if (propertiesFilesZipEntries != null)
        {
            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().startsWith(folder) && entry.getName().endsWith(".properties"))
                {
                    propertiesFilesZipEntries.add(entry);
                }
            }
        }
    }

    private @Nullable ZipEntry getZipEntryByName(
            @NotNull Enumeration<? extends ZipEntry> entries, @NotNull String name
    )
    {
        while (entries.hasMoreElements())
        {
            ZipEntry entry = entries.nextElement();
            if (name.equals(entry.getName()))
            {
                return entry;
            }
        }
        return null;
    }
}