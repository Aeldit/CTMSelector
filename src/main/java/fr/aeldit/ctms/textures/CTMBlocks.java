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
    public static class CTMBlock
    {
        private final String blockName;
        private final String prettyName;
        private final Identifier identifier;
        private boolean enabled;

        public CTMBlock(@NotNull String blockName, Identifier identifier, boolean enabled)
        {
            this.blockName = blockName;
            this.identifier = identifier;
            this.enabled = enabled;

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
            this.prettyName = stringBuilder.toString();
        }

        public Text getName()
        {
            return Text.of(prettyName);
        }

        public String getBlockName()
        {
            return blockName;
        }

        public Identifier getIdentifier()
        {
            return identifier;
        }

        public boolean getEnabled()
        {
            return enabled;
        }

        public void toggle()
        {
            this.enabled = !this.enabled;
        }

        public void setEnabled(boolean value)
        {
            this.enabled = value;
        }
    }

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
                return block.getEnabled();
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

    private final List<CTMBlock> availableCtmBlocks = new ArrayList<>();
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
