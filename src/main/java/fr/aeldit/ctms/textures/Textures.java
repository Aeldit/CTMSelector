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

import fr.aeldit.cyanlib.lib.config.CyanLibOptionsStorage;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static fr.aeldit.ctms.util.Utils.CTMS_OPTIONS_STORAGE;

public class Textures
{
    private final ArrayList<Path> texturesPaths = new ArrayList<>();
    private final ArrayList<CyanLibOptionsStorage.BooleanOption> booleanOptions = new ArrayList<>();

    public void init() // TODO -> make options for each ctm resource pack
    {
        Path path = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");

        for (final File resourcePackDir : path.toFile().listFiles())
        {
            if (resourcePackDir.isDirectory())
            {
                Path tmpPath = Path.of(resourcePackDir + "/assets/minecraft/optifine/ctm/connect");

                if (Files.exists(tmpPath))
                {
                    for (final File file : tmpPath.toFile().listFiles())
                    {
                        if (file.getName().startsWith("c_"))
                        {
                            for (final File textureDir : file.listFiles())
                            {
                                if (textureDir.isDirectory())
                                {
                                    for (final File imagesOrProperties : textureDir.listFiles())
                                    {
                                        if (imagesOrProperties.getName().endsWith(".properties"))
                                        {
                                            texturesPaths.add(textureDir.toPath());
                                            System.out.println(imagesOrProperties.getName()
                                                    .split("\\\\")[imagesOrProperties.getName().split("\\\\").length - 1]
                                                    .replace(".properties", "")
                                            );
                                            booleanOptions.add(CTMS_OPTIONS_STORAGE.new BooleanOption(imagesOrProperties.getName()
                                                    .split("\\\\")[imagesOrProperties.getName().split("\\\\").length - 1]
                                                    .replace(".properties", ""),
                                                    true)
                                            );
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public ArrayList<CyanLibOptionsStorage.BooleanOption> getTextureOptions()
    {
        return booleanOptions;
    }

    public ArrayList<Path> getTexturesPaths()
    {
        return texturesPaths;
    }
}
