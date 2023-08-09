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
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static fr.aeldit.ctms.util.Utils.CTMS_OPTIONS_STORAGE;

public class TexturesHandling
{
    public void init() // TODO -> make options for each ctm resource pack
    {
        Path path = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");

        for (File resourcePackDir : path.toFile().listFiles())
        {
            if (resourcePackDir.isDirectory() && resourcePackDir.getName().startsWith("CTM"))
            {
                Path tmpPath = Path.of(resourcePackDir + "/assets/minecraft/optifine/ctm/connect");

                if (Files.exists(tmpPath))
                {
                    for (File categoryOrBlockDir : tmpPath.toFile().listFiles())
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
                                        System.out.println("Texture dir : " + textureDir.getName());
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
                                CTMS_OPTIONS_STORAGE.initPackOptions(resourcePackDir.getName(), tmpCtmBlocksList);
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
                allBlocksInDir.add(CTMS_OPTIONS_STORAGE.new BooleanOption(
                        packDir.getName(),
                        currentDir.getName()
                                .split("\\\\")[currentDir.getName().split("\\\\").length - 1]
                                .replace(".properties", ""),
                        true
                ));
            }
        }
        return allBlocksInDir;
    }

    public void updateUsedTextures()
    {

    }
}
