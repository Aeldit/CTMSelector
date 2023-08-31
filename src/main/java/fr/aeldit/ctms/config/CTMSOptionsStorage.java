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

package fr.aeldit.ctms.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CTMSOptionsStorage
{
    private final Map<String, Map<String, Boolean>> optionsMaps = new HashMap<>();
    private final Map<String, Map<String, Boolean>> unsavedOptionsMaps = new HashMap<>();

    public void initPackOptions(String packName, Map<String, Boolean> options)
    {
        optionsMaps.put(packName, options);
    }

    public Map<String, Map<String, Boolean>> getOptionsMaps()
    {
        return optionsMaps;
    }

    public void clearOptionsMap()
    {
        optionsMaps.clear();
    }

    public boolean getOption(String packName, String optionName)
    {
        return optionsMaps.containsKey(packName) && optionsMaps.get(packName).containsKey(optionName) ? optionsMaps.get(packName).get(optionName) : false;
    }

    public void setOption(String packName)
    {
        if (unsavedOptionsMaps.containsKey(packName) && optionsMaps.containsKey(packName))
        {
            unsavedOptionsMaps.get(packName).forEach((optionName, optionValue) -> optionsMaps.get(packName).put(optionName, optionValue));
        }
    }

    public void setUnsavedOption(String packName, String optionName, boolean value)
    {
        if (unsavedOptionsMaps.containsKey(packName))
        {
            Map<String, Boolean> tmpMap = new HashMap<>(unsavedOptionsMaps.get(packName));
            tmpMap.put(optionName, value);
            unsavedOptionsMaps.put(packName, tmpMap);
        }
        else
        {
            unsavedOptionsMaps.put(packName, Collections.singletonMap(optionName, value));
        }
    }

    public void clearUnsavedOptionsMap(String packName)
    {
        if (unsavedOptionsMaps.containsKey(packName))
        {
            unsavedOptionsMaps.put(packName, new HashMap<>(0));
        }
    }

    public ArrayList<String> getEnabledPacks()
    {
        return new ArrayList<>(MinecraftClient.getInstance().getResourcePackManager().getEnabledNames());
    }

    public boolean optionsChanged(String packName)
    {
        if (optionsMaps.containsKey(packName) && unsavedOptionsMaps.containsKey(packName))
        {
            return unsavedOptionsMaps.get(packName).entrySet().stream()
                    .anyMatch(entry -> entry.getValue() != getOption(packName, entry.getKey()));
        }
        return false;
    }

    public void resetOptions(String packName)
    {
        if (optionsMaps.containsKey(packName))
        {
            optionsMaps.get(packName).forEach((optionName, optionValue) -> optionsMaps.get(packName).put(optionName, true));
        }
    }

    @Environment(EnvType.CLIENT)
    public SimpleOption<?> @NotNull [] asConfigOptions(String packName)
    {
        ArrayList<SimpleOption<?>> optionsList = new ArrayList<>();
        ArrayList<String> sortedNames = new ArrayList<>(optionsMaps.get(packName).keySet());
        Collections.sort(sortedNames);

        sortedNames.forEach(optionName -> {
            StringBuilder translation = new StringBuilder();

            for (String str : optionName.split("_"))
            {
                translation.append(str.substring(0, 1).toUpperCase()).append(str.substring(1));
                translation.append(" ");
            }
            optionsList.add(SimpleOption.ofBoolean(
                    translation.toString(),
                    getOption(packName, optionName),
                    optionValue -> setUnsavedOption(packName, optionName, optionValue))
            );
        });

        return optionsList.toArray(SimpleOption[]::new);
    }
}
