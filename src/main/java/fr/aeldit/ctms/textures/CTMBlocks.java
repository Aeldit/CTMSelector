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

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CTMBlocks
{
    public record CTMBlock(String blockName, Identifier identifier)
    {
        public Text getName()
        {
            return Text.of(blockName);
        }
    }

    public final static Map<String, CTMBlocks> CTM_BLOCKS_MAP = new HashMap<>();

    public static Set<CTMBlock> getAvailableCtmBlocks(String packName)
    {
        return CTM_BLOCKS_MAP.containsKey(packName)
                ? CTM_BLOCKS_MAP.get(packName).getAvailableCtmBlocks()
                : new HashSet<>();
    }

    public static boolean getOptionValue(String packName, String blockName)
    {
        System.out.println(getCTMBlocks(packName).getAvailableCtmBlocks());
        for (CTMBlock block : getCTMBlocks(packName).getAvailableCtmBlocks())
        {
            if (block.blockName.equals(blockName))
            {
                return getCTMBlocks(packName).contains(block);
            }
        }
        return false;
    }

    public static @Nullable CTMBlocks getCTMBlocks(String packName)
    {
        return CTM_BLOCKS_MAP.getOrDefault(packName, null);
    }

    @Contract(" -> new")
    public static @NotNull ArrayList<String> getEnabledPacks()
    {
        return new ArrayList<>(MinecraftClient.getInstance().getResourcePackManager().getEnabledNames());
    }

    public CTMBlocks(String packName)
    {
        CTM_BLOCKS_MAP.put(packName, this);
    }

    private final Set<CTMBlock> availableCtmBlocks = new HashSet<>();
    private final Set<CTMBlock> enabledCtmBlocks = new HashSet<>();
    private final Set<CTMBlock> unsavedOptions = new HashSet<>();

    public boolean contains(CTMBlock block)
    {
        return enabledCtmBlocks.contains(block);
    }

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

    public void addEnabled(CTMBlock block)
    {
        add(block);
        enabledCtmBlocks.add(block);
    }

    public void toggle(CTMBlock block)
    {
        if (enabledCtmBlocks.contains(block))
        {
            enabledCtmBlocks.remove(block);
        }
        else
        {
            enabledCtmBlocks.add(block);
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
        enabledCtmBlocks.clear();
        enabledCtmBlocks.addAll(availableCtmBlocks);
    }

    public void restoreUnsavedOptions()
    {
        for (CTMBlock block : unsavedOptions)
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
        unsavedOptions.clear();
    }

    private Set<CTMBlock> getAvailableCtmBlocks()
    {
        return availableCtmBlocks;
    }

    public Set<CTMBlock> getEnabledCtmBlocks()
    {
        return enabledCtmBlocks;
    }
}
