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

import fr.aeldit.ctms.config.CTMSOptionsStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static fr.aeldit.ctms.util.Utils.CTMS_OPTIONS_STORAGE;

public class TexturesHandling
{
    private final Map<String, Boolean> tmpOptionsMap = new HashMap<>();

    public void init()
    {
        Path path = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");

        for (File resourcePackDir : path.toFile().listFiles())
        {
            if (resourcePackDir.isDirectory() && resourcePackDir.getName().startsWith("CTM"))
            {
                Path ctmPath = Path.of(resourcePackDir + "/assets/minecraft/optifine/ctm/connect");

                if (Files.exists(ctmPath))
                {
                    for (File categoryOrBlockDir : ctmPath.toFile().listFiles())
                    {
                        if (categoryOrBlockDir.isDirectory())
                        {
                            ArrayList<CTMSOptionsStorage.BooleanOption> tmpCtmBlocksList = new ArrayList<>();

                            if (categoryOrBlockDir.getName().startsWith("c_"))
                            {
                                for (File textureDir : categoryOrBlockDir.listFiles())
                                {
                                    if (textureDir.isDirectory())
                                    {
                                        tmpCtmBlocksList.addAll(getBlockInDir(resourcePackDir, textureDir));
                                    }
                                }
                            }
                            else
                            {
                                tmpCtmBlocksList.addAll(getBlockInDir(resourcePackDir, categoryOrBlockDir));
                            }

                            if (!tmpCtmBlocksList.isEmpty())
                            {
                                CTMS_OPTIONS_STORAGE.initPackOptions(resourcePackDir.getName(), tmpCtmBlocksList, tmpOptionsMap);
                            }
                        }
                    }
                }
            }
        }
    }

    public ArrayList<CTMSOptionsStorage.BooleanOption> getBlockInDir(File packDir, @NotNull File dir)
    {
        ArrayList<CTMSOptionsStorage.BooleanOption> allBlocksInDir = new ArrayList<>();

        for (File currentDir : dir.listFiles())
        {
            if (currentDir.getName().endsWith(".properties"))
            {
                String optionName = currentDir.getName()
                        .split("\\\\")[currentDir.getName().split("\\\\").length - 1]
                        .replace(".properties", "");
                allBlocksInDir.add(CTMS_OPTIONS_STORAGE.new BooleanOption(
                        packDir.getName(),
                        optionName,
                        true
                ));
                tmpOptionsMap.put(optionName, true);
            }
            else if (currentDir.getName().endsWith(".txt"))
            {
                String optionName = currentDir.getName()
                        .split("\\\\")[currentDir.getName().split("\\\\").length - 1]
                        .replace(".txt", "");
                allBlocksInDir.add(CTMS_OPTIONS_STORAGE.new BooleanOption(
                        packDir.getName(),
                        optionName,
                        true
                ));
                tmpOptionsMap.put(optionName, false);
            }
        }
        return allBlocksInDir;
    }

    public void toggleBlockConnection(String packName, @NotNull File propertiesOrTxtFile)
    {
        if (CTMS_OPTIONS_STORAGE.getBooleanOption(packName, propertiesOrTxtFile.getName()
                .replace(".properties", "")
                .replace(".txt", ""))
        )
        {
            if (propertiesOrTxtFile.getName().endsWith(".txt"))
            {
                propertiesOrTxtFile.renameTo(new File(
                        propertiesOrTxtFile.getPath().replace(propertiesOrTxtFile.getName(), "")
                                + propertiesOrTxtFile.getName().replace(".txt", ".properties"))
                );
            }
        }
        else
        {
            if (propertiesOrTxtFile.getName().endsWith(".properties"))
            {
                propertiesOrTxtFile.renameTo(new File(
                        propertiesOrTxtFile.getPath().replace(propertiesOrTxtFile.getName(), "")
                                + propertiesOrTxtFile.getName().replace(".properties", ".txt"))
                );
            }
        }
    }

    public void updateUsedTextures(String packName)
    {
        Path ctmPath = FabricLoader.getInstance().getGameDir().resolve("resourcepacks/" + packName + "/assets/minecraft/optifine/ctm/connect");

        if (Files.exists(ctmPath))
        {
            for (File categoryOrBlockDir : ctmPath.toFile().listFiles())
            {
                if (categoryOrBlockDir.isDirectory())
                {
                    if (categoryOrBlockDir.getName().startsWith("c_"))
                    {
                        for (File textureDir : categoryOrBlockDir.listFiles())
                        {
                            if (textureDir.isDirectory())
                            {
                                for (File currentDir : textureDir.listFiles())
                                {
                                    if (currentDir.isFile() && (currentDir.getName().endsWith(".properties") || currentDir.getName().endsWith(".txt")))
                                    {
                                        toggleBlockConnection(packName, currentDir);
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        for (File currentDir : categoryOrBlockDir.listFiles())
                        {
                            if (currentDir.isFile() && (currentDir.getName().endsWith(".properties") || currentDir.getName().endsWith(".txt")))
                            {
                                toggleBlockConnection(packName, currentDir);
                            }
                        }
                    }
                }
            }
        }
        MinecraftClient.getInstance().reloadResources();
    }
}
