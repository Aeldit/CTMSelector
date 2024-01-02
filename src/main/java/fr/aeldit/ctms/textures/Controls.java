/*
 * Copyright (c) 2023-2024  -  Made by Aeldit
 *
 *              GNU LESSER GENERAL PUBLIC LICENSE
 *                  Version 3, 29 June 2007
 *
 *  Copyright (C) 2007 Free Software Foundation, Inc. <https://fsf.org/>
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 *
 *
 * This version of the GNU Lesser General Public License incorporates
 * the terms and conditions of version 3 of the GNU General Public
 * License, supplemented by the additional permissions listed in the LICENSE.txt file
 * in the repo of this mod (https://github.com/Aeldit/CTMSelector)
 */

package fr.aeldit.ctms.textures;

import com.google.gson.annotations.SerializedName;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Properties;

public class Controls
{
    // The record will be used to serialize and deserialize the controls
    public record ControlsRecord(
            @SerializedName("type") @NotNull String type,
            @SerializedName("group_name") @NotNull String groupName,
            @SerializedName("button_tooltip") @Nullable String buttonTooltip,
            @SerializedName("properties_files") @NotNull ArrayList<String> propertiesFilesPaths, // TODO -> allow the user to put directories
            @SerializedName("screen_texture") @Nullable String screenTexture,
            @SerializedName("enabled") boolean isEnabled,
            @SerializedName("priority") @Nullable PRIORITY_LEVELS priority
    ) {}

    /**
     * Priorities work this way :
     * <ul>
     *     <li>If a group is disabled, the blocks in it are disabled</li>
     *     <li>If a group contains a block that is disabled but the group is
     *     enabled, the block is disabled</li>
     * </ul>
     *
     * @implNote We could use a boolean here, but if we later want to add
     *         more priority levels, this enum will be useful
     */
    public enum PRIORITY_LEVELS
    {
        LOW,
        HIGH
    }

    private final String type;
    private final String groupName;
    private final Text buttonTooltip;
    private final ArrayList<String> propertiesFilesStrings;
    private final String texturePath;
    private final PRIORITY_LEVELS priority;
    private boolean isEnabled;

    // The following fields are not in the file, and are used only in the code
    private final ArrayList<Path> propertiesFilesPaths = new ArrayList<>();
    private final Identifier identifier;

    public Controls(
            @NotNull String type, @NotNull String groupName, @Nullable String buttonTooltip,
            @NotNull ArrayList<String> propertiesFilesPaths, @Nullable String texturePath,
            boolean isEnabled, @Nullable PRIORITY_LEVELS priority, Path packPath
    )
    {
        this.type = type;
        this.groupName = groupName;
        this.buttonTooltip = buttonTooltip == null ? Text.empty() : Text.of(buttonTooltip);
        this.propertiesFilesStrings = propertiesFilesPaths;
        this.texturePath = texturePath;
        this.isEnabled = isEnabled;
        this.priority = priority == null ? PRIORITY_LEVELS.LOW : priority;

        for (String s : propertiesFilesPaths)
        {
            this.propertiesFilesPaths.add(Path.of(packPath + "/assets/" + s.replace(":", "/")));
        }

        if (texturePath == null)
        {
            if (propertiesFilesPaths.isEmpty())// Case where no files where specified (this is also an error)
            {
                this.identifier = new Identifier("textures/misc/unknown_pack.png");
            }
            else
            {
                String path = getBlocksForImage(Path.of(packPath + "/assets/" + propertiesFilesPaths.get(0).replace(":", "/")));
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
        isEnabled = !isEnabled;
    }

    public PRIORITY_LEVELS getPriority()
    {
        return priority;
    }

    public ControlsRecord getRecord()
    {
        return new Controls.ControlsRecord(type, groupName, buttonTooltip.getString(),
                propertiesFilesStrings, texturePath, isEnabled, priority
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
     *         (ex: can be {@code "copper_block"} or {@code "copper_block exposed_copper weathered_copper oxidized_copper"},
     *         where each block is separated by a space)
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
}
