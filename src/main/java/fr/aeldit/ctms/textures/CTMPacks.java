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

package fr.aeldit.ctms.textures;

import fr.aeldit.ctms.gui.entryTypes.CTMPack;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a a block found in a {@link java.util.Properties Properties} file
 * that has the CTM method
 *
 * @apiNote The {@link #availableCTMPacks} ArrayList contains every
 *         {@link CTMPack} that was found during the packs loading
 */
public class CTMPacks
{
    private final List<CTMPack> availableCTMPacks = new ArrayList<>();
    private int icons_index = 0;

    public int getIcons_index()
    {
        return icons_index++;
    }

    public static @NotNull ArrayList<String> getEnabledPacks()
    {
        return new ArrayList<>(MinecraftClient.getInstance().getResourcePackManager().getEnabledNames());
    }

    public void add(@NotNull CTMPack ctmPack)
    {
        if (!availableCTMPacks.contains(ctmPack))
        {
            availableCTMPacks.add(ctmPack);
        }
    }

    public List<CTMPack> getAvailableCTMPacks()
    {
        return availableCTMPacks;
    }
}
