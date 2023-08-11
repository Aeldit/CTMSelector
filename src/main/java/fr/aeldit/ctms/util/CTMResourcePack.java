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

package fr.aeldit.ctms.util;

import fr.aeldit.ctms.gui.widgets.ResourcePacksListWidget;

import java.nio.file.Path;

public class CTMResourcePack
{
    private final String name;
    private final Path path;
    private final Path iconPath;

    public CTMResourcePack(String name, Path path)
    {
        this.name = name;
        this.path = path;
        this.iconPath = Path.of(path + "/pack.png");
    }

    public ResourcePacksListWidget.ResourcePackListEntry asResourcePackListEntry()
    {
        return new ResourcePacksListWidget.ResourcePackListEntry(this);
    }

    public String getName()
    {
        return name;
    }

    public Path getIconPath()
    {
        return iconPath;
    }
}
