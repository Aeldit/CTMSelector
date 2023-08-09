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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static fr.aeldit.ctms.util.Utils.CTMS_MODID;

public class CTMSOptionsStorage
{
    private final Map<String, ArrayList<BooleanOption>> booleanOptions = new HashMap<>();
    private final Map<String, Map<String, Boolean>> unsavedChangedOptions = new HashMap<>();

    public void init()
    {
        readConfig();
    }

    public void initOptions(String packName, ArrayList<BooleanOption> options)
    {
        booleanOptions.put(packName, options);
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
            if (!unsavedChangedOptions.containsKey(optionName) && booleanOptionExists(packName, optionName))
            {
                unsavedChangedOptions.get(packName).put(optionName, getBooleanOption(packName, optionName));
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

    public Map<String, ArrayList<BooleanOption>> getBooleanOptions()
    {
        return booleanOptions;
    }

    public boolean getBooleanOption(String packName, String blockName)
    {
        return booleanOptions.get(packName).get(getBlockIndex(packName, blockName)).getValue();
    }

    public void setBooleanOption(String packName, String optionName, boolean value)
    {
        if (booleanOptions.containsKey(packName))
        {
            booleanOptions.get(packName).get(getBlockIndex(packName, optionName)).setValue(value);
        }
        else
        {
            booleanOptions.put(packName, new ArrayList<>(Collections.singletonList(new BooleanOption(packName, optionName, value))));
        }
    }

    public boolean booleanOptionExists(String packName, String optionName)
    {
        for (BooleanOption option : booleanOptions.get(packName))
        {
            if (option.optionName.equals(optionName))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Can only be called if the result of {@link #booleanOptionExists(String, String)} is {@code true}
     * <p>
     * Should NEVER return {@code -1}. If it does, it means the result of {@link #booleanOptionExists(String, String)} is {@code false}
     */
    public int getBlockIndex(String packName, String blockName)
    {
        int i = 0;
        System.out.println(booleanOptions);

        for (BooleanOption option : booleanOptions.get(packName))
        {
            if (option.optionName.equals(blockName))
            {
                return i;
            }
            i++;
        }
        return -1;
    }

    public void resetOptions(String packName)
    {
        booleanOptions.get(packName).forEach(booleanOption -> booleanOption.setValue(booleanOption.defaultValue));
        //in the foreach : booleanOptions.get(packName).get(getBlockIndex(packName, booleanOption.optionName)).setValue(booleanOption.defaultValue)
    }

    private void readConfig()
    {
        booleanOptions.get("CTM OF Fabric").forEach(booleanOption -> System.out.println(booleanOption.optionName));
        Path path = FabricLoader.getInstance().getConfigDir().resolve(CTMS_MODID + ".json");

        if (Files.exists(path))
        {
            try
            {
                Gson gson = new Gson();
                Reader reader = Files.newBufferedReader(path);
                TypeToken<Map<String, ArrayList<BooleanOption>>> mapType = new TypeToken<>() {};
                booleanOptions.putAll(gson.fromJson(reader, mapType));
                reader.close();
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
