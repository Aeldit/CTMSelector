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

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static fr.aeldit.ctms.util.Utils.TEXTURES_HANDLING;

public class CTMBlocks
{
    public record CTMBlock(String blockName, Identifier identifier)
    {
        public Text getName()
        {
            return Text.of(blockName);
        }
    }

    public static Map<String, CTMBlocks> ctmBlocksMap = new HashMap<>();

    private final String packName;

    public CTMBlocks(String packName)
    {
        this.packName = packName;
        ctmBlocksMap.put(packName, this);
    }

    private final Set<CTMBlock> availableCtmBlocks = new HashSet<>();
    private final Set<CTMBlock> enabledCtmBlocks = new HashSet<>();

    public void add(CTMBlock block)
    {
        availableCtmBlocks.add(block);
    }

    public boolean contains(CTMBlock block)
    {
        return enabledCtmBlocks.contains(block);
    }

    public void toggle(CTMBlock block)
    {
        if (availableCtmBlocks.contains(block))
        {
            if (enabledCtmBlocks.contains(block))
            {
                enabledCtmBlocks.remove(block);
            }
            else
            {
                enabledCtmBlocks.add(block);
            }
            TEXTURES_HANDLING.updateUsedTextures(packName);
        }
    }

    public Set<CTMBlock> getAvailableCtmBlocks()
    {
        return availableCtmBlocks;
    }
}
