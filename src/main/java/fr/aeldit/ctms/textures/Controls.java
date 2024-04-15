package fr.aeldit.ctms.textures;

import com.google.gson.annotations.SerializedName;
import fr.aeldit.ctms.gui.entryTypes.CTMBlock;
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

public class Controls
{
    public record SerializableControls(
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
    private final String type;
    private final String groupName;
    private final ArrayList<String> propertiesFilesStrings;
    private final String iconPath;
    private boolean isEnabled;
    private final Text buttonTooltip;

    //=================================
    // Non-record fields
    //=================================
    private final ArrayList<Path> propertiesFilesPaths; // When the pack is a folder
    private final ArrayList<FileHeader> propertiesFilesFileHeaders; // When the pack is a zip file
    private final Identifier identifier;
    private final ArrayList<CTMBlock> containedBlocks = new ArrayList<>();

    public Controls(
            @NotNull String type, @NotNull String groupName, @Nullable String buttonTooltip,
            @NotNull ArrayList<String> propertiesFilesStrings, @NotNull String iconPath,
            boolean isEnabled, Path packPath, boolean isInFolder, @Nullable String zipPackPath
    )
    {
        this.type = type;
        this.groupName = groupName;
        this.buttonTooltip = buttonTooltip == null ? Text.empty() : Text.of(buttonTooltip);
        this.propertiesFilesStrings = propertiesFilesStrings;
        this.iconPath = iconPath;
        this.isEnabled = isEnabled;

        // Obtains the path to each block
        if (isInFolder)
        {
            this.propertiesFilesPaths = new ArrayList<>();
            this.propertiesFilesFileHeaders = null;

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
        }
        else
        {
            this.propertiesFilesPaths = null;
            this.propertiesFilesFileHeaders = new ArrayList<>();

            if (zipPackPath != null)
            {
                try (ZipFile zipFile = new ZipFile(zipPackPath))
                {
                    for (String s : propertiesFilesStrings)
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

        // Case where the namespace is not specified
        if (!iconPath.contains(":"))
        {
            this.identifier = new Identifier("textures/misc/unknown_pack.png");
        }
        else
        {
            this.identifier = new Identifier(iconPath.split(":")[0], iconPath.split(":")[1]);
        }
    }

    //=================================
    // Record
    //=================================
    public String getGroupName()
    {
        return groupName;
    }

    public Text getGroupNameAsText()
    {
        return Text.of(groupName);
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

    public SerializableControls getAsRecord()
    {
        return new SerializableControls(type, groupName, propertiesFilesStrings, iconPath, isEnabled,
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
     * @return The path to each Properties file contained by the Controls
     */
    public ArrayList<Path> getPropertiesFilesPaths()
    {
        return propertiesFilesPaths == null ? new ArrayList<>() : propertiesFilesPaths;
    }

    public ArrayList<FileHeader> getPropertiesFilesFileHeaders()
    {
        return propertiesFilesFileHeaders == null ? new ArrayList<>() : propertiesFilesFileHeaders;
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

    private void getPropertiesFilesInZipFolder(@NotNull List<FileHeader> fileHeaders, String folder)
    {
        for (FileHeader fileHeader : fileHeaders)
        {
            if (fileHeader.toString().startsWith(folder))
            {
                if (fileHeader.toString().endsWith(".properties"))
                {
                    this.propertiesFilesFileHeaders.add(fileHeader);
                }
            }
        }
    }

    private @Nullable FileHeader getFileHeaderByName(@NotNull List<FileHeader> fileHeaders, String name)
    {
        for (FileHeader fileHeader : fileHeaders)
        {
            if (fileHeader.toString().equals(name))
            {
                return fileHeader;
            }
        }
        return null;
    }
}
