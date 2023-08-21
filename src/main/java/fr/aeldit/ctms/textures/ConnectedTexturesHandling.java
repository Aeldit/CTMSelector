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
import fr.aeldit.ctms.util.CTMResourcePack;
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

public class ConnectedTexturesHandling
{
    private final Map<String, Boolean> tmpOptionsMap = new HashMap<>();
    private static final ArrayList<CTMResourcePack> ctmResourcePacks = new ArrayList<>();
    private final Path resourcePacksDir = FabricLoader.getInstance().getGameDir().resolve("resourcepacks/");

    public void init()
    {
        Path path = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");

        for (File resourcePackDirOrZip : path.toFile().listFiles())
        {
            // Directory
            if (resourcePackDirOrZip.isDirectory() && resourcePackDirOrZip.getName().startsWith("CTM"))
            {
                //boolean ctmFound = false;
                Path ctmPath = Path.of(resourcePackDirOrZip + "/assets/minecraft/optifine/ctm/connect");

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
                                        tmpCtmBlocksList.addAll(getBlocksInDir(resourcePackDirOrZip, textureDir));
                                    }
                                }
                            }
                            else
                            {
                                tmpCtmBlocksList.addAll(getBlocksInDir(resourcePackDirOrZip, categoryOrBlockDir));
                            }

                            if (!tmpCtmBlocksList.isEmpty())
                            {
                                CTMS_OPTIONS_STORAGE.initPackOptions(resourcePackDirOrZip.getName(), tmpCtmBlocksList, tmpOptionsMap, ctmResourcePacks);
                                //ctmFound = true;
                            }
                        }
                    }
                }

                /*if (ctmFound)
                {
                    ctmResourcePacks.add(new CTMResourcePack(resourcePackDirOrZip.getName(), resourcePackDirOrZip.toPath()));
                }*/
            }

            // Zip File
            else if (resourcePackDirOrZip.isFile() && resourcePackDirOrZip.getName().startsWith("CTM"))
            {
                if (getFileExtension(resourcePackDirOrZip.getName()).equals("zip"))
                {
                    if (pathInZipExists(resourcePackDirOrZip.toString(), "assets/minecraft/optifine/ctm/connect/"))
                    {
                        ArrayList<CTMSOptionsStorage.BooleanOption> tmpCtmBlocksList = new ArrayList<>();

                        for (FileHeader fileHeader : listFilesInPack(resourcePackDirOrZip.toString()))
                        {
                            if (fileHeader.toString().endsWith(".properties"))
                            {
                                String optionName = fileHeader.toString()
                                        .split("/")[fileHeader.toString().split("/").length - 1]
                                        .replace(".properties", "");

                                // Removes the fileName from the path the get the parent path
                                String[] strippedFileHeader = fileHeader.toString().split("/");
                                StringBuilder parentPath = new StringBuilder();

                                for (int i = 0; i < strippedFileHeader.length - 1; i++)
                                {
                                    parentPath.append(strippedFileHeader[i]).append("/");
                                }

                                tmpCtmBlocksList.add(CTMS_OPTIONS_STORAGE.new BooleanOption(
                                        resourcePackDirOrZip.getName(),
                                        optionName,
                                        true,
                                        Path.of(parentPath.toString())
                                ));
                                tmpOptionsMap.put(optionName, true);
                            }
                            else if (fileHeader.toString().endsWith(".txt"))
                            {
                                String optionName = fileHeader.toString()
                                        .split("/")[fileHeader.toString().split("/").length - 1]
                                        .replace(".txt", "");

                                // Removes the fileName from the path the get the parent path
                                String[] strippedFileHeader = fileHeader.toString().split("/");
                                StringBuilder parentPath = new StringBuilder();

                                for (int i = 0; i < strippedFileHeader.length - 1; i++)
                                {
                                    parentPath.append(strippedFileHeader[i]).append("/");
                                }

                                tmpCtmBlocksList.add(CTMS_OPTIONS_STORAGE.new BooleanOption(
                                        resourcePackDirOrZip.getName(),
                                        optionName,
                                        true,
                                        Path.of(parentPath.toString())
                                ));
                                tmpOptionsMap.put(optionName, false);
                            }
                        }

                        if (!tmpCtmBlocksList.isEmpty())
                        {
                            CTMS_OPTIONS_STORAGE.initPackOptions(resourcePackDirOrZip.getName(), tmpCtmBlocksList, tmpOptionsMap, ctmResourcePacks);
                        }
                    }
                }
            }
        }
    }

    private String getFileExtension(@NotNull String fileName)
    {
        return fileName.split("\\.")[fileName.split("\\.").length - 1];
    }

    private boolean pathInZipExists(String packName, String path)
    {
        try
        {
            for (FileHeader fileHeader : listFilesInPack(packName))
            {
                if (fileHeader.toString().equals(path))
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

    public List<FileHeader> listFilesInPack(String packName)
    {
        ArrayList<FileHeader> fileHeaders;
        try (ZipFile tmpZipFile = new ZipFile(packName))
        {
            fileHeaders = new ArrayList<>(tmpZipFile.getFileHeaders());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return fileHeaders;
    }

    private @NotNull ArrayList<CTMSOptionsStorage.BooleanOption> getBlocksInDir(File packDir, @NotNull File dir)
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
                        true,
                        dir.toPath()
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
                        true,
                        dir.toPath()
                ));
                tmpOptionsMap.put(optionName, false);
            }
        }
        return allBlocksInDir;
    }

    private void toggleBlockConnection(String packName, @NotNull File propertiesOrTxtFile)
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

    public void updateUsedTextures(@NotNull String packName)
    {
        if (packName.endsWith(".zip"))
        {
            // We disable the pack but the resources need to be reloaded for the zip file to be accessible for writing
            MinecraftClient.getInstance().getResourcePackManager().disable("file/" + packName);
            MinecraftClient.getInstance().reloadResources();

            if (pathInZipExists(resourcePacksDir + "\\" + packName, "assets/minecraft/optifine/ctm/connect/"))
            {
                Map<String, String> fileNamesMap = new HashMap<>();

                for (FileHeader fileHeader : listFilesInPack(resourcePacksDir + "\\" + packName))
                {
                    boolean option = CTMS_OPTIONS_STORAGE.getBooleanOption(packName, fileHeader.toString()
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
        else
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
}
