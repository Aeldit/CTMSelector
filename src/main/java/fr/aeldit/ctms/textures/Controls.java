/*
 * Copyright (c) 2023  -  Made by Aeldit
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
 * in the repo of this mod (https://github.com/Aeldit/Cyan)
 */

package fr.aeldit.ctms.textures;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Controls
{
    @SerializedName("type")
    private String type;
    @SerializedName("block_group")
    private String blockGroup;
    @SerializedName("button_title")
    private String buttonTitle;
    @SerializedName("button_tooltip")
    private String buttonTooltip;
    @SerializedName("properties_files")
    private ArrayList<String> propertiesFilesPaths;

    public Controls(String blockGroup, String buttonTitle, String buttonTooltip, ArrayList<String> propertiesFilesPaths)
    {
        this.blockGroup = blockGroup;
        this.buttonTitle = buttonTitle;
        this.buttonTooltip = buttonTooltip;
        this.propertiesFilesPaths = propertiesFilesPaths;
    }

    public String getBlockGroup()
    {
        return blockGroup;
    }

    public String getButtonTitle()
    {
        return buttonTitle;
    }

    public String getButtonTooltip()
    {
        return buttonTooltip;
    }

    public ArrayList<String> getPropertiesFilesPaths()
    {
        return propertiesFilesPaths;
    }

    public Set<String> getFilesOrOptionsNames()
    {
        Set<String> paths = new HashSet<>();

        propertiesFilesPaths.forEach(filePath -> {
                    StringBuilder translation = new StringBuilder();

                    for (String str : filePath.split("_"))
                    {
                        translation.append(str.substring(0, 1).toUpperCase()).append(str.substring(1));
                        translation.append(" ");
                    }
                    paths.add(translation.toString());
                }
        );

        return paths;
    }
}
