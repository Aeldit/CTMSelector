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

import fr.aeldit.ctms.gui.widgets.ResourcePacksListWidget;
import fr.aeldit.ctms.util.CTMResourcePack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.SimpleOption;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CTMSOptionsStorage
{
    private final Map<String, ArrayList<BooleanOption>> defaultBooleanOptions = new HashMap<>();
    private final Map<String, Map<String, Boolean>> booleanOptions = new HashMap<>();
    private final Map<String, Map<String, Boolean>> unsavedChangedOptions = new HashMap<>();
    private final ArrayList<ResourcePacksListWidget.ResourcePackListEntry> resourcePackListEntries = new ArrayList<>();

    public void initPackOptions(String packName, @NotNull ArrayList<BooleanOption> defaultOptions, Map<String, Boolean> options, ArrayList<CTMResourcePack> ctmResourcePacks)
    {
        if (defaultBooleanOptions.containsKey(packName))
        {
            defaultBooleanOptions.get(packName).addAll(defaultOptions);
        }
        else
        {
            defaultBooleanOptions.put(packName, new ArrayList<>(defaultOptions));
        }

        if (booleanOptions.containsKey(packName))
        {
            booleanOptions.get(packName).putAll(options);
        }
        else
        {
            booleanOptions.put(packName, options);
        }

        ctmResourcePacks.forEach(ctmResourcePack -> resourcePackListEntries.add(new ResourcePacksListWidget.ResourcePackListEntry(ctmResourcePack)));
    }

    public Map<String, Map<String, Boolean>> getUnsavedChangedOptions()
    {
        return unsavedChangedOptions;
    }

    public ArrayList<ResourcePacksListWidget.ResourcePackListEntry> getResourcePackListEntries()
    {
        return resourcePackListEntries;
    }

    public void clearUnsavedChangedOptions()
    {
        unsavedChangedOptions.clear();
    }

    public class BooleanOption
    {
        private final String packName;
        private final String optionName;
        private final boolean defaultValue;
        private final Path parentPath;

        public BooleanOption(String packName, String optionName, boolean defaultValue, Path parentPath)
        {
            this.packName = packName;
            this.optionName = optionName;
            this.defaultValue = defaultValue;
            this.parentPath = parentPath;
        }

        public boolean getValue()
        {
            return getBooleanOption(packName, optionName);
        }

        public void setValue(boolean value)
        {
            if (unsavedChangedOptions.containsKey(packName))
            {
                unsavedChangedOptions.get(packName).put(optionName, getBooleanOption(packName, optionName));
            }
            else
            {
                Map<String, Boolean> booleanMap = new HashMap<>();
                booleanMap.put(optionName, getBooleanOption(packName, optionName));
                unsavedChangedOptions.put(packName, booleanMap);
            }
            setBooleanOption(packName, optionName, value);
        }

        @Environment(EnvType.CLIENT)
        public SimpleOption<Boolean> asConfigOption()
        {
            String[] translations = optionName.split("_");
            StringBuilder translation = new StringBuilder();

            for (String str : translations)
            {
                translation.append(str.substring(0, 1).toUpperCase()).append(str.substring(1));
                translation.append(" ");
            }
            return SimpleOption.ofBoolean(translation.toString(), getValue(), this::setValue);
        }

        public Path getParentPath()
        {
            return parentPath;
        }
    }

    @Environment(EnvType.CLIENT)
    public static SimpleOption<?> @NotNull [] asConfigOptions(@NotNull ArrayList<BooleanOption> booleanOptions)
    {
        ArrayList<SimpleOption<?>> options = new ArrayList<>();
        booleanOptions.forEach(option -> options.add(option.asConfigOption()));

        return options.toArray(SimpleOption[]::new);
    }

    public Map<String, Map<String, Boolean>> getBooleanOptions()
    {
        return booleanOptions;
    }

    public Map<String, ArrayList<BooleanOption>> getDefaultBooleanOptions()
    {
        return defaultBooleanOptions;
    }

    public boolean getBooleanOption(String packName, String optionName)
    {
        if (booleanOptions.containsKey(packName) && booleanOptions.get(packName).containsKey(optionName))
        {
            return booleanOptions.get(packName).get(optionName);
        }
        return false;
    }

    public void setBooleanOption(String packName, String optionName, boolean value)
    {
        if (booleanOptions.containsKey(packName))
        {
            booleanOptions.get(packName).put(optionName, value);
        }
        else
        {
            Map<String, Boolean> tmpMap = new HashMap<>();
            tmpMap.put(optionName, value);
            booleanOptions.put(packName, tmpMap);
        }
    }

    public void resetOptions(String packName)
    {
        defaultBooleanOptions.get(packName).forEach(option -> booleanOptions.get(packName).put(option.optionName, option.defaultValue));
    }
}
