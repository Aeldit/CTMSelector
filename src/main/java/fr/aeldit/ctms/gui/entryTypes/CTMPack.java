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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a CTM capable resource pack.
 * <p>
 * Contains methods to handle the options
 */
public class CTMPack
{
    private final String name;
    private final boolean isFolder;
    private Identifier identifier;
    private int iconId;

    private final List<CTMBlock> ctmBlocks = new ArrayList<>();
    private final List<CTMBlock> unsavedOptions = new ArrayList<>();

    public CTMPack(String name, boolean isFolder)
    {
        this.name = name;
        this.isFolder = isFolder;
    }

    public String getName()
    {
        return name;
    }

    public Text getNameAsText()
    {
        return isFolder ? Text.of(name + " (folder)") : Text.of(name);
    }

    public Identifier getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(Identifier identifier)
    {
        this.identifier = identifier;
    }

    public int getIconId()
    {
        return iconId;
    }

    public void setIconId(int iconId)
    {
        this.iconId = iconId;
    }

    public boolean isFolder()
    {
        return isFolder;
    }

    //=========================================================================
    // CTMBlocks
    //=========================================================================
    public void addBlock(CTMBlock ctmBlock)
    {
        ctmBlocks.add(ctmBlock);
    }

    public void addAllBlocks(@NotNull List<CTMBlock> ctmBlockList)
    {
        ctmBlockList.forEach(this::addBlock);
    }

    public List<CTMBlock> getCtmBlocks()
    {
        return ctmBlocks;
    }

    public void toggle(CTMBlock block)
    {
        if (ctmBlocks.contains(block))
        {
            ctmBlocks.get(ctmBlocks.indexOf(block)).toggle();
        }

        if (unsavedOptions.contains(block))
        {
            unsavedOptions.remove(block);
        }
        else
        {
            unsavedOptions.add(block);
        }
    }

    public void resetOptions()
    {
        ctmBlocks.forEach(ctmBlock -> ctmBlock.setEnabled(true));
    }

    public void restoreUnsavedOptions()
    {
        for (CTMBlock block : unsavedOptions)
        {
            ctmBlocks.get(ctmBlocks.indexOf(block)).setEnabled(!ctmBlocks.contains(block));
        }
        unsavedOptions.clear();
    }

    public void clearUnsavedOptions()
    {
        unsavedOptions.clear();
    }

    public boolean optionsChanged()
    {
        return !unsavedOptions.isEmpty();
    }

    public boolean getOptionValue(String blockName)
    {
        for (CTMBlock block : ctmBlocks)
        {
            if (block.getBlockName().equals(blockName))
            {
                return block.isEnabled();
            }
        }
        return false;
    }
}
