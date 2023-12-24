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
 * in the repo of this mod (https://github.com/Aeldit/CTMSelector)
 */

package fr.aeldit.ctms.gui.entryTypes;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class CTMPack
{
    private final String name;
    private final Identifier identifier;

    public CTMPack(@NotNull String name, Identifier identifier)
    {
        this.name = name;
        this.identifier = identifier;
    }

    public String getNameAsString()
    {
        return name;
    }

    public Text getName()
    {
        return Text.of(name);
    }

    public Identifier getIdentifier()
    {
        return identifier;
    }
}
