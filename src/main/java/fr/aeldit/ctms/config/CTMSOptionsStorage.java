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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.aeldit.cyanlib.lib.config.SimpleOptionConverter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.SimpleOption;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static fr.aeldit.ctms.util.Utils.CTMS_MODID;

public class CTMSOptionsStorage
{
    private final Map<String, ArrayList<BooleanOption>> defaultBooleanOptions = new HashMap<>();
    private final Map<String, Map<String, Boolean>> booleanOptions = new HashMap<>();
    private final Map<String, Map<String, Boolean>> unsavedChangedOptions = new HashMap<>();

    public void init()
    {
        readConfig();
    }

    public void initPackOptions(String packName, @NotNull ArrayList<BooleanOption> options)
    {
        Map<String, Boolean> tmpMap = new HashMap<>();
        options.forEach(booleanOption -> tmpMap.put(booleanOption.optionName, booleanOption.defaultValue));
        System.out.println("tmpMap : " + tmpMap);

        if (booleanOptions.containsKey(packName))
        {
            booleanOptions.get(packName).putAll(tmpMap);
        }
        else
        {
            booleanOptions.put(packName, tmpMap);
        }
        if (defaultBooleanOptions.containsKey(packName))
        {
            defaultBooleanOptions.get(packName).addAll(options);
        }
        else
        {
            defaultBooleanOptions.put(packName, new ArrayList<>(options));
        }
    }

    public Map<String, Map<String, Boolean>> getUnsavedChangedOptions()
    {
        return unsavedChangedOptions;
    }

    public void clearUnsavedChangedOptions()
    {
        unsavedChangedOptions.clear();
    }

    public class BooleanOption implements SimpleOptionConverter
    {
        private final String packName;
        private final String optionName;
        private final boolean defaultValue;

        public BooleanOption(String packName, String optionName, boolean value)
        {
            this.packName = packName;
            this.optionName = optionName;
            this.defaultValue = value;
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
        @Override
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
    }

    @Environment(EnvType.CLIENT)
    public static SimpleOption<?> @NotNull [] asConfigOptions(@NotNull ArrayList<BooleanOption> booleanOptions)
    {
        ArrayList<SimpleOption<?>> options = new ArrayList<>();
        booleanOptions.forEach(option -> options.add(((SimpleOptionConverter) option).asConfigOption()));

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
        return booleanOptions.get(packName).get(optionName);
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

    private void readConfig()
    {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(CTMS_MODID + ".json");

        if (Files.exists(path))
        {
            try
            {
                Gson gson = new Gson();
                Reader reader = Files.newBufferedReader(path);
                TypeToken<Map<String, Map<String, Boolean>>> mapType = new TypeToken<>() {};
                Map<String, Map<String, Boolean>> fromGsonMap = new HashMap<>(gson.fromJson(reader, mapType));
                reader.close();

                for (Map.Entry<String, Map<String, Boolean>> entry : fromGsonMap.entrySet())
                {
                    if (booleanOptions.containsKey(entry.getKey()))
                    {
                        for (Map.Entry<String, Boolean> packEntry : entry.getValue().entrySet())
                        {
                            if (booleanOptions.get(entry.getKey()).containsKey(packEntry.getKey()))
                            {
                                if (booleanOptions.get(entry.getKey()).get(packEntry.getKey()) != packEntry.getValue())
                                {
                                    booleanOptions.get(entry.getKey()).put(packEntry.getKey(), packEntry.getValue());
                                }
                            }
                        }
                    }
                }
                System.out.println(booleanOptions);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public void writeConfig()
    {
        clearUnsavedChangedOptions();
        Path path = FabricLoader.getInstance().getConfigDir().resolve(CTMS_MODID + ".json");

        if (!Files.exists(path))
        {
            try
            {
                Files.createFile(path);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        try
        {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Writer writer = Files.newBufferedWriter(path);
            gson.toJson(booleanOptions, writer);
            writer.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
