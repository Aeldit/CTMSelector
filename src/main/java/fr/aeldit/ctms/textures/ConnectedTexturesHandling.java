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

import net.fabricmc.loader.api.FabricLoader;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.aeldit.ctms.util.Utils.CTMS_OPTIONS_STORAGE;

public class ConnectedTexturesHandling
{
    private final Path resourcePacksDir = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");
    private final String ctmPath = "assets/minecraft/optifine/ctm/connect/";

    public void init()
    {
        CTMS_OPTIONS_STORAGE.clearOptionsMap();

        for (File zipFileOrFolder : resourcePacksDir.toFile().listFiles())
        {
            // Zip File
            if (zipFileOrFolder.isFile()
                    && zipFileOrFolder.getName().startsWith("CTM")
                    && zipFileOrFolder.getName().endsWith(".zip")
            )
            {
                if (isCtmPack(zipFileOrFolder.toString()))
                {
                    Map<String, Boolean> currentPackOptions = new HashMap<>();

                    for (FileHeader fileHeader : listFilesInPack(zipFileOrFolder.toString()))
                    {
                        if (fileHeader.toString().contains(ctmPath))
                        {
                            if (fileHeader.toString().endsWith(".properties"))
                            {
                                currentPackOptions.put(fileHeader.toString()
                                        .split("/")[fileHeader.toString().split("/").length - 1]
                                        .replace(".properties", ""), true);
                            }
                            else if (fileHeader.toString().endsWith(".txt"))
                            {
                                currentPackOptions.put(fileHeader.toString()
                                        .split("/")[fileHeader.toString().split("/").length - 1]
                                        .replace(".txt", ""), false);
                            }
                        }
                    }

                    // If the array and the map are not empty, we initialise the options for the current pack
                    if (!currentPackOptions.isEmpty())
                    {
                        CTMS_OPTIONS_STORAGE.initPackOptions(zipFileOrFolder.getName(), currentPackOptions);
                    }
                }
            }
        }
    }

    private boolean isCtmPack(String packPath)
    {
        try
        {
            for (FileHeader fileHeader : listFilesInPack(packPath))
            {
                if (fileHeader.toString().contains(ctmPath))
                {
                    return true;
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        return false;
    }

    private List<FileHeader> listFilesInPack(String packPath)
    {
        ArrayList<FileHeader> fileHeaders;

        try (ZipFile tmpZipFile = new ZipFile(packPath))
        {
            fileHeaders = new ArrayList<>(tmpZipFile.getFileHeaders());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return fileHeaders;
    }

    public void updateUsedTextures(@NotNull String packName)
    {
        // We disable the pack and reload the resources because the reloading makes the zip file accessible for writing
        MinecraftClient.getInstance().getResourcePackManager().disable("file/" + packName);
        MinecraftClient.getInstance().reloadResources();

        if (isCtmPack(resourcePacksDir + "\\" + packName))
        {
            Map<String, String> fileNamesMap = new HashMap<>();

            for (FileHeader fileHeader : listFilesInPack(resourcePacksDir + "\\" + packName))
            {
                boolean option = CTMS_OPTIONS_STORAGE.getOption(packName, fileHeader.toString()
                        .split("/")[fileHeader.toString().split("/").length - 1]
                        .replace(".properties", "")
                        .replace(".txt", ""));

                if (fileHeader.toString().endsWith(".properties") && !option)
                {
                    fileNamesMap.put(fileHeader.toString(), fileHeader.toString().replace(".properties", ".txt"));
                }
                else if (fileHeader.toString().endsWith(".txt") && option)
                {
                    fileNamesMap.put(fileHeader.toString(), fileHeader.toString().replace(".txt", ".properties"));
                }
            }

            if (!fileNamesMap.isEmpty())
            {
                try (ZipFile zipFile = new ZipFile(resourcePacksDir + "\\" + packName))
                {
                    zipFile.renameFiles(fileNamesMap);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
        MinecraftClient.getInstance().getResourcePackManager().enable("file/" + packName);
        MinecraftClient.getInstance().reloadResources();
    }
}
