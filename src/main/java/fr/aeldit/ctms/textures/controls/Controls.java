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

package fr.aeldit.ctms.textures.controls;

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
            @SerializedName("type") String type,
            @SerializedName("group_name") String groupName,
            @SerializedName("button_tooltip") String buttonTooltip,
            @SerializedName("properties_files") ArrayList<String> propertiesFilesPaths,
            @SerializedName("screen_texture") String screenTexture,
            @SerializedName("enabled") boolean isEnabled //,
            //@SerializedName("priority") int priority // TODO -> implement the priority
    ) {}

    private final String type;
    private final String groupName;
    private final String buttonTooltip;
    private final ArrayList<String> propertiesFilesPaths = new ArrayList<>();
    private final Identifier identifier;
    private final Path packPath;
    //private final int priority;
    private boolean isEnabled;

    public Controls(
            @NotNull String type, @NotNull String groupName, @Nullable String buttonTooltip,
            @NotNull ArrayList<String> propertiesFilesPaths, @Nullable String texturePath,
            boolean isEnabled, Path packPath
    )
    {
        this.type = type;
        this.groupName = groupName;
        this.buttonTooltip = buttonTooltip;
        this.packPath = packPath;
        this.propertiesFilesPaths.addAll(propertiesFilesPaths);

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

        this.isEnabled = isEnabled;
    }

    public String getType()
    {
        return type;
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
        return Text.of(buttonTooltip);
    }

    /**
     * @return The path to each Properties file contained by the Controls
     */
    public ArrayList<Path> getPropertiesFilesPaths()
    {
        if (propertiesFilesPaths.isEmpty())
        {
            return new ArrayList<>();
        }

        ArrayList<Path> paths = new ArrayList<>();
        Path resourcePackPath = Path.of(packPath + "/assets");

        for (String s : propertiesFilesPaths)
        {
            paths.add(Path.of(resourcePackPath + "/" + s.replace(":", "/")));
        }
        return paths;
    }

    public Identifier getIdentifier()
    {
        return identifier;
    }

    public boolean isEnabled()
    {
        return isEnabled;
    }

    public void setEnabled(boolean enabled)
    {
        isEnabled = enabled;
    }

    public void toggle()
    {
        this.isEnabled = !this.isEnabled;
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
