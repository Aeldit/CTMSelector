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

package fr.aeldit.ctms.gui.entryTypes;

import fr.aeldit.ctms.textures.controls.Controls;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Represents a a block found in a {@link java.util.Properties Properties} file
 * that has the CTM method
 *
 * @apiNote The {@link #containingGroups} ArrayList contains every
 *         {@link Controls} that contains this block
 *         <p>
 *         {@link #blockName} is in the form {@code "block_name"}
 *         <p>
 *         {@link #prettyName} is in the form {@code "Block Name"}
 *         <p>
 *         A block being enabled or disabled depends only on the state of
 *         the field {@link #enabled} if the block is not contained
 *         by any {@link Controls}. If the block is contained by at least 1
 *         {@link Controls},
 *         it depends on whether this group is activated or not
 */
public class CTMBlock
{
    private final String blockName;
    private final Text prettyName;
    private final Identifier identifier;
    private final ArrayList<Controls> containingGroups = new ArrayList<>();
    private boolean enabled;

    public CTMBlock(@NotNull String blockName, Identifier identifier, boolean enabled)
    {
        this.blockName = blockName;
        this.identifier = identifier;
        this.enabled = enabled;

        // Changes the lowercase and underscore separated string by replacing each '_' by a space
        // and by capitalizing the first letter of each word
        String[] tmp = blockName.split("_");
        StringBuilder stringBuilder = new StringBuilder();
        int index = 0;

        for (String str : tmp)
        {
            stringBuilder.append(str.substring(0, 1).toUpperCase()).append(str.substring(1));

            if (index < tmp.length - 1)
            {
                stringBuilder.append(" ");
            }
            index++;
        }
        this.prettyName = Text.of(stringBuilder.toString());
    }

    public String getBlockName()
    {
        return blockName;
    }

    public Text getPrettyName()
    {
        return prettyName;
    }

    public Identifier getIdentifier()
    {
        return identifier;
    }

    public void addContainingGroup(Controls containingGroup)
    {
        containingGroups.add(containingGroup);
    }

    public void addAllContainingGroups(@NotNull ArrayList<Controls> containingGroups)
    {
        containingGroups.forEach(this::addContainingGroup);
    }

    public boolean isEnabled() // TODO -> handle priorities with Controls
    {
        for (Controls controls : containingGroups)
        {
            if (!controls.isEnabled())
            {
                return false;
            }
        }
        return enabled;
    }

    public void setEnabled(boolean value)
    {
        this.enabled = value;
    }

    public void toggle()
    {
        this.enabled = !this.enabled;
    }
}
