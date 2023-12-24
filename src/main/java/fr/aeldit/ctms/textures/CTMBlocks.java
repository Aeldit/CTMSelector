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

package fr.aeldit.ctms.textures;

import fr.aeldit.ctms.gui.entryTypes.CTMBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CTMBlocks
{
    public final static Map<String, CTMBlocks> CTM_BLOCKS_MAP = new HashMap<>();

    public static List<CTMBlock> getAvailableCtmBlocks(String packName)
    {
        return CTM_BLOCKS_MAP.containsKey(packName)
                ? CTM_BLOCKS_MAP.get(packName).getAvailableCtmBlocks()
                : new ArrayList<>();
    }

    public static boolean getOptionValue(String packName, String blockName)
    {
        for (CTMBlock block : getCTMBlocks(packName).getAvailableCtmBlocks())
        {
            if (block.getBlockName().equals(blockName))
            {
                return block.isEnabled();
            }
        }
        return false;
    }

    public static @Nullable CTMBlocks getCTMBlocks(String packName)
    {
        return CTM_BLOCKS_MAP.getOrDefault(packName, null);
    }

    public CTMBlocks(String packName)
    {
        CTM_BLOCKS_MAP.put(packName, this);
    }

    // List of all the CTM blocks found in the pack
    private final List<CTMBlock> availableCtmBlocks = new ArrayList<>();
    // Used for the cancel button
    private final Set<CTMBlock> unsavedOptions = new HashSet<>();

    public boolean optionsChanged()
    {
        return !unsavedOptions.isEmpty();
    }

    public void clearUnsavedOptions()
    {
        unsavedOptions.clear();
    }

    public void add(CTMBlock block)
    {
        availableCtmBlocks.add(block);
    }

    public void addAll(@NotNull ArrayList<CTMBlock> ctmBlockArrayList)
    {
        ctmBlockArrayList.forEach(this::add);
    }

    public void toggle(CTMBlock block)
    {
        if (availableCtmBlocks.contains(block))
        {
            availableCtmBlocks.get(availableCtmBlocks.indexOf(block)).toggle();
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
        for (CTMBlock block : availableCtmBlocks)
        {
            block.setEnabled(true);
        }
    }

    public void restoreUnsavedOptions()
    {
        for (CTMBlock block : unsavedOptions)
        {
            availableCtmBlocks.get(availableCtmBlocks.indexOf(block)).setEnabled(!availableCtmBlocks.contains(block));
        }
        unsavedOptions.clear();
    }

    private List<CTMBlock> getAvailableCtmBlocks()
    {
        return availableCtmBlocks;
    }
}
