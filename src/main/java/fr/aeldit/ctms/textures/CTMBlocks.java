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

import net.minecraft.block.Block;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

public class CTMBlocks
{
    public record CTMBlock(String blockName, Identifier identifier)
    {
        public Text getName()
        {
            return Text.of(blockName);
        }
    }

    private static Set<Block> availableBlocks = new HashSet<>();
    private static Set<Block> enabledBlocks = new HashSet<>();

    private static Set<CTMBlock> availableCtmBlocks = new HashSet<>();
    private static Set<CTMBlock> enabledCtmBlocks = new HashSet<>();

    public static void add(Block block)
    {
        availableBlocks.add(block);
    }

    public static void add(CTMBlock block)
    {
        availableCtmBlocks.add(block);
        System.out.println(block.blockName);
        System.out.println(block.identifier);
    }

    public static boolean contains(Block block)
    {
        return availableBlocks.contains(block);
    }

    public static boolean contains(CTMBlock block)
    {
        return availableCtmBlocks.contains(block);
    }

    public static void toggle(Block block)
    {
        if (availableBlocks.contains(block))
        {
            if (enabledBlocks.contains(block))
            {
                enabledBlocks.remove(block);
            }
            else
            {
                enabledBlocks.add(block);
            }
        }
    }

    public static void toggle(CTMBlock block)
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
        }
    }

    public static Set<Block> getAvailableBlocks()
    {
        return availableBlocks;
    }

    public static Set<CTMBlock> getAvailableCtmBlocks()
    {
        return availableCtmBlocks;
    }
}
