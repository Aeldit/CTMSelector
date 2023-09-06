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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.aeldit.ctms.util.Utils.CTMS_OPTIONS_STORAGE;

public class FilesHandling
{
    private final Path resourcePacksDir = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");
    private final String ctmPath = "assets/minecraft/optifine/ctm/connect/";
    private final ArrayList<Path> folderPaths = new ArrayList<>();

    public void init()
    {
        CTMS_OPTIONS_STORAGE.clearOptionsMap();

        if (!Files.exists(resourcePacksDir))
        {
            try
            {
                Files.createDirectories(resourcePacksDir);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        for (File zipFileOrFolder : resourcePacksDir.toFile().listFiles())
        {
            if (zipFileOrFolder.isFile() && zipFileOrFolder.getName().endsWith(".zip")
            )
            {
                if (isZipCtmPack(zipFileOrFolder.toString()))
                {
                    Map<String, Boolean> currentPackOptions = new HashMap<>();

                    for (FileHeader fileHeader : listFilesInZipPack(zipFileOrFolder.toString()))
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

                    // If the map is not empty, we initialise the options for the current pack
                    if (!currentPackOptions.isEmpty())
                    {
                        CTMS_OPTIONS_STORAGE.initPackOptions(zipFileOrFolder.getName(), currentPackOptions);
                    }
                }
            }
            else if (zipFileOrFolder.isDirectory() && isFolderCtmPack(zipFileOrFolder.getName()))
            {
                Map<String, Boolean> currentFolderPackOptions = new HashMap<>();

                for (Path path : listFilesInFolderPack(zipFileOrFolder))
                {
                    if (path.toString().contains(ctmPath.replace("/", "\\")))
                    {
                        if (path.toString().endsWith(".properties"))
                        {
                            currentFolderPackOptions.put(path.getFileName().toString().replace(".properties", ""), true);
                        }
                        else if (path.toString().endsWith(".txt"))
                        {
                            currentFolderPackOptions.put(path.getFileName().toString().replace(".txt", ""), false);
                        }
                    }
                }
                folderPaths.clear();

                // If the map is not empty, we initialise the options for the current pack
                if (!currentFolderPackOptions.isEmpty())
                {
                    CTMS_OPTIONS_STORAGE.initPackOptions(zipFileOrFolder.getName(), currentFolderPackOptions);
                }
            }
        }
    }

    private boolean isZipCtmPack(String packPath)
    {
        try
        {
            for (FileHeader fileHeader : listFilesInZipPack(packPath))
            {
                if (fileHeader.toString().startsWith("assets") && fileHeader.toString().contains(ctmPath))
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

    private boolean isFolderCtmPack(String packPath)
    {
        return Files.exists(Path.of(resourcePacksDir + "\\" + packPath + "\\" + ctmPath));
    }

    private List<FileHeader> listFilesInZipPack(String packPath)
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

    private ArrayList<Path> listFilesInFolderPack(@NotNull File packFolder)
    {
        for (File file : packFolder.listFiles())
        {
            if (file.isFile() && (file.getName().endsWith(".properties") || file.getName().endsWith(".txt")))
            {
                folderPaths.add(file.toPath());
            }
            else if (file.isDirectory())
            {
                listFilesInFolderPack(file);
            }
        }
        return folderPaths;
    }

    public void updateUsedTextures(@NotNull String packName)
    {
        // We disable the pack and reload the resources because the reloading makes the zip file accessible for writing
        MinecraftClient.getInstance().getResourcePackManager().disable("file/" + packName);
        MinecraftClient.getInstance().reloadResources();

        if (isZipCtmPack(resourcePacksDir + "\\" + packName))
        {
            Map<String, String> fileNamesMap = new HashMap<>();

            for (FileHeader fileHeader : listFilesInZipPack(resourcePacksDir + "\\" + packName))
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
